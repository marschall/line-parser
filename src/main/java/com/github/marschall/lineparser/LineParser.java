package com.github.marschall.lineparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Parses a file into multiple lines.
 *
 * <p>Intended for cases where:</p>
 * <ul>
 *  <li>the start position in the file of a line is required</li>
 *  <li>the length in bytes of a line is required</li>
 *  <li>only a few character of every line is required</li>
 * </ul>
 *
 * <p>Offers a fast paths for:</p>
 * <ul>
 *  <li>character sets in which CR an LF only take up a single byte</li>
 *  <li><a href="https://en.wikipedia.org/wiki/ISO/IEC_8859-1">ISO 8859-1</a>
 *  compatible character sets, including <a href="https://en.wikipedia.org/wiki/ASCII">ASCII</a></li>
 *  <li><a href="https://en.wikipedia.org/wiki/UTF-16">UTF-16</a></li>
 * </ul>
 * <p>They can be combined for maximum performance.</p>
 *
 * <h2>Algorithm</h2>
 * The following algorithm is used to parse files:
 * <ol>
 *  <li>map the file into memory</li>
 *  <li>encode <code>CR</code>, <code>LF</code> and <code>CR LF</code> to bytes using the given character set</li>
 *  <li>while not done
 *    <ol>
 *      <li>scan the file byte by byte for cr, crlf or lf</li>
 *      <li>create a line object and invoke the callback</li>
 *    </ol>
 *  </li>
 *  <li>unmap the file from memory</li>
 * </ol>
 * <p>If the file is larger can 2GB then it is mapped into memory multiple times.
 * This has to be done because Java does not support file mappings larger than 2GB.</p>
 *
 * <p>Unmapping the file from memory is <a href="http://bugs.java.com/view_bug.do?bug_id=4724038">controversial</a>
 * and can only be done using semi-official APIs. The alternative would
 * be to rely on finalization to close file handles. However this is
 * not recommended, unreliable, unpredictable, deprecated and can
 * introduce performance issues and issues with file locks.</p>
 *
 * <p>This class is thread safe.</p>
 *
 * @see Line
 */
public final class LineParser {

  private static final MethodHandle UNSAFE_INVOKE_CLEANER;
  private static final MethodHandle DIRECT_BYTE_BUFFER_CLEANER;
  private static final MethodHandle CLEANER_CLEAN;
  // null if no VM support so make sure it's the argument to .equals()
  private static final Charset UTF_32;
  // null if no VM support so make sure it's the argument to .equals()
  private static final Charset UTF_32BE;
  // null if no VM support so make sure it's the argument to .equals()
  private static final Charset UTF_32LE;

  private static final long FILE_END = -1;

  static {
    Lookup lookup = MethodHandles.publicLookup();
    if (isJava9OrLater()) {
      // Java 9 branch
      // Unsafe.theUnsafe.invokeCleaner(byteBuffer)
      try {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field singleoneInstanceField = unsafeClass.getDeclaredField("theUnsafe");
        if (!singleoneInstanceField.isAccessible()) {
          singleoneInstanceField.setAccessible(true);
        }
        Object unsafe = singleoneInstanceField.get(null);

        Method invokeCleaner = unsafeClass.getDeclaredMethod("invokeCleaner", ByteBuffer.class);
        UNSAFE_INVOKE_CLEANER = lookup.unreflect(invokeCleaner).bindTo(unsafe);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("could not get invokeCleaner method handle", e);
      }
      DIRECT_BYTE_BUFFER_CLEANER = null;
      CLEANER_CLEAN = null;
    } else {
      // Java 8 branch
      // sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
      // cleaner.clean();
      try {
        Class<?> bufferInterface = Class.forName("java.nio.DirectByteBuffer");
        Method cleanerMethod = bufferInterface.getDeclaredMethod("cleaner");
        if (!cleanerMethod.isAccessible()) {
          cleanerMethod.setAccessible(true);
        }
        Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
        Method cleanMethod = cleanerClass.getDeclaredMethod("clean"); // should be accessible
        DIRECT_BYTE_BUFFER_CLEANER = lookup.unreflect(cleanerMethod);
        CLEANER_CLEAN = lookup.unreflect(cleanMethod);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("could not get cleaner clean method handle", e);
      }
      UNSAFE_INVOKE_CLEANER = null;
    }

    UTF_32 = safeLoadCharset("UTF-32");
    UTF_32BE = safeLoadCharset("UTF-32BE");
    UTF_32LE = safeLoadCharset("UTF-32LE");
  }

  private static Charset safeLoadCharset(String name) {
    try {
      return Charset.forName(name);
    } catch (UnsupportedCharsetException e) {
      // minimal vm without extended charsets
      return null;
    }
  }

  private static boolean isJava9OrLater() {
    try {
      Class.forName("java.lang.Runtime$Version");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private final int maxMapSize;

  public LineParser() {
    this(Integer.MAX_VALUE);
  }

  LineParser(int maxBufferSize) {
    this.maxMapSize = maxBufferSize;
  }

  /**
   * Internal iterator over every line in a file.
   *
   * <p>This method is thread safe.</p>
   *
   * @param path the file to parse
   * @param cs the character set to use
   * @param lineCallback callback executed for every line
   * @throws IOException if an exception happens when reading
   * @throws UnmapFailedException if unampping fails, this can happen on non OpenJDK
   *   JREs or JREs that are newer than expected or security managers
   */
  public void forEach(Path path, Charset cs, Consumer<Line> lineCallback) throws IOException {
    try (FileInputStream stream = new FileInputStream(path.toFile());
         FileChannel channel = stream.getChannel()) {
      long fileSize = channel.size();
      LineReader reader = LineReader.forCharset(cs);
      byte[] cr = "\r".getBytes(cs);
      byte[] lf = "\n".getBytes(cs);
      boolean useFastPath = (cr.length == 1) && (lf.length == 1);
      FileInfo fileInfo = new FileInfo(path, channel, fileSize, reader, lineCallback);
      if (useFastPath) {
        FastEncodingInfo encodingInfo = new FastEncodingInfo(cr[0], lf[0]);
        this.forEachFast(fileInfo, encodingInfo);
      } else {
        EncodingInfo encodingInfo = new EncodingInfo(cs, cr, lf);
        this.forEach(fileInfo, encodingInfo);
      }
    }
  }

  private void forEachFast(FileInfo fileInfo, FastEncodingInfo encodingInfo) throws IOException {
    long mapInfo = 0L;
    while (mapInfo != FILE_END) {
      mapInfo = this.forEachFast(fileInfo, encodingInfo, mapInfo);
    }
  }

  private void forEach(FileInfo fileInfo, EncodingInfo encodingInfo) throws IOException {
    long mapStart = 0L;
    FileInfo actualFileInfo;
    EncodingInfo actualEncodingInfo;
    MappedByteBuffer buffer;
    if (isAmbiguous(encodingInfo.cs)) {
      buffer = this.map(fileInfo, mapStart);
      BomResolutionResult result;
      try {
        result = this.resolveBom(encodingInfo.cs, buffer);
        // normally unmapping happens in forEach() we when we get
        // an unchecked exceptio or error we need to do it here
      } catch (Error | RuntimeException e) {
        unmap(buffer, fileInfo);
        throw e;
      }
      Charset actualCharset = result.cs;
//      mapStart = result.mapStart;
      LineReader actualReader = LineReader.forCharset(actualCharset);
      byte[] actualCr = "\r".getBytes(actualCharset);
      byte[] actualLf = "\n".getBytes(actualCharset);
      actualEncodingInfo = new EncodingInfo(actualCharset, actualCr, actualLf);
      actualFileInfo = new FileInfo(fileInfo.path, fileInfo.channel, fileInfo.fileSize, actualReader, fileInfo.lineCallback);
    } else {
      actualEncodingInfo = encodingInfo;
      actualFileInfo = fileInfo;
      buffer = null;
    }
    while (mapStart != FILE_END) {
      mapStart = this.forEach(buffer, actualFileInfo, actualEncodingInfo, mapStart);
    }
  }

  private MappedByteBuffer map(FileInfo fileInfo,  long mapStart) throws IOException {
    FileChannel channel = fileInfo.channel;
    int mapSize = this.mapSize(fileInfo, mapStart);
    return channel.map(MapMode.READ_ONLY, mapStart, mapSize);
  }

  private int mapSize(FileInfo fileInfo, long mapStart) {
    return Math.min(Math.toIntExact(fileInfo.fileSize - mapStart), this.maxMapSize);
  }

  /**
   * Checks if the character set is ambiguous and therefore needs a BOM
   * in order to decode.
   */
  private static boolean isAmbiguous(Charset cs) {
    return cs.equals(StandardCharsets.UTF_16) || cs.equals(UTF_32);
  }

  /**
   * Takes a character set that is ambiguous and tries to make it
   * unambiguous by resolving the BOM.
   */
  private BomResolutionResult resolveBom(Charset cs, MappedByteBuffer buffer) {
    // https://en.wikipedia.org/wiki/Byte_order_mark
    if (cs.equals(StandardCharsets.UTF_16)) {
      if (buffer.capacity() >= 2) {
        int firstByte = Byte.toUnsignedInt(buffer.get(0));
        int secondByte = Byte.toUnsignedInt(buffer.get(1));
        if ((firstByte == 0xFE) && (secondByte == 0xFF)) {
          return new BomResolutionResult(StandardCharsets.UTF_16BE, 2);
        } else if ((firstByte == 0xFF) && (secondByte == 0xFE)) {
          return new BomResolutionResult(StandardCharsets.UTF_16LE, 2);
        }
      }
      // no bom
      return new BomResolutionResult(cs, 0);
    } else if (cs.equals(UTF_32)) {
      if (buffer.capacity() >= 4) {
        int firstByte = Byte.toUnsignedInt(buffer.get(0));
        int secondByte = Byte.toUnsignedInt(buffer.get(1));
        int thirdByte = Byte.toUnsignedInt(buffer.get(2));
        int fourthByte = Byte.toUnsignedInt(buffer.get(3));
        if ((firstByte == 0x00) && (secondByte == 0x00) && (thirdByte == 0xFE) && (fourthByte == 0xFF)) {
          return new BomResolutionResult(Objects.requireNonNull(UTF_32BE), 4);
        } else if ((firstByte == 0xFF) && (secondByte == 0xFE) && (thirdByte == 0x00) && (fourthByte == 0x00)) {
          return new BomResolutionResult(Objects.requireNonNull(UTF_32LE), 4);
        }
      }
      // no bom
      return new BomResolutionResult(cs, 0);
    } else {
      throw new IllegalArgumentException("BOM resolution not yet supported for " + cs.name());
    }
  }

  private long forEach(MappedByteBuffer buffer, FileInfo fileInfo, EncodingInfo encodingInfo, long mapStart) throws IOException {
    byte[] cr = encodingInfo.cr;
    byte[] lf = encodingInfo.lf;
    long fileSize = fileInfo.fileSize;
    LineReader reader = fileInfo.reader;
    Consumer<Line> lineCallback =  fileInfo.lineCallback;
    int mapSize = this.mapSize(fileInfo, mapStart);
    if (buffer == null) {
      // in case of a multi byte encoding we may have to have a look at
      // the file first in order to read the BOM in order to determine
      // the actual encoding
      // we don't want to map and unmap just in order to read 2 to 4 bytes
      // so we pass the buffer to this method
      // in this case we already have a MappedByteBuffer for the first 2 GB
      // in all other cases we don't so we map now
      buffer = this.map(fileInfo, mapStart);
    }
    try {

      int lineStart = 0; // in buffer
      int crLength = cr.length;
      int lfLength = lf.length;

      int mapIndex = 0;
      while (mapIndex < mapSize) {
        byte value = buffer.get(mapIndex);

        // if (buffer[mapIndex] == CR)
        if (startsWithArray(value, cr, crLength, mapIndex, mapSize, buffer)) {

          // if (buffer[mapIndex] == LF)
          int newlineLength = crLength;
          if (continuesWithArray(lf, lfLength, crLength, mapIndex, mapSize, buffer)) {
            newlineLength += lfLength;
          }

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up loop variable for the next iteration
          mapIndex = lineStart = mapIndex + newlineLength;


        // else if (buffer[mapIndex] == LF)
        } else if (startsWithArray(value, lf, lfLength, mapIndex, mapSize, buffer)) {

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up the loop variable for the next iteration
          mapIndex = lineStart = mapIndex + lfLength;
        } else {
          mapIndex += 1;
        }

      }

      if ((mapSize + mapStart) < fileSize) {
        // we could not map the entire file
        // map from the start of the last line
        // and continue reading from there
        return mapStart + lineStart;
      } else if (lineStart < mapSize) {
        // we're at the end of the file
        // if the last line didn't end in a newline read it now
        readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);
      }

    } finally {
      unmap(buffer, fileInfo);
    }
    return FILE_END;
  }

  private static boolean startsWithArray(byte value, byte[] newLine, int newLineLength,
          int mapIndex, int mapSize, MappedByteBuffer buffer) {
    // mapSize - mapIndex == buffer.remaining() + 1
    if ((value == newLine[0]) && ((newLineLength - 1) < (mapSize - mapIndex))) {
      // input starts with the first byte of a newline, but newline may be multiple bytes
      // check if the input starts with all bytes of a newline
      for (int i = 1; i < newLineLength; i++) {
        if (buffer.get(mapIndex + i) != newLine[i]) {
          // wasn't a newline after all
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static boolean continuesWithArray(byte[] lf, int lfLength, int offset, int mapIndex, int mapSize, MappedByteBuffer buffer) {
    if (lfLength < (mapSize - mapIndex)) {
      for (int i = 0; i < lfLength; i++) {
        if (buffer.get(mapIndex + offset + i) != lf[i]) {
          // not a lf
          // be don't need to fix the buffer state here
          // having the information that the newline is just a cr is enough
          // to make the read
          return false;
        }
      }
      return true;
    }
    return false;
  }

  // fast path version
  // much simpler and inlines
  private long forEachFast(FileInfo fileInfo, FastEncodingInfo encodingInfo, long mapStart) throws IOException {
    FileChannel channel = fileInfo.channel;
    byte cr = encodingInfo.cr;
    byte lf = encodingInfo.lf;
    long fileSize = fileInfo.fileSize;
    LineReader reader = fileInfo.reader;
    Consumer<Line> lineCallback =  fileInfo.lineCallback;
    int mapSize = this.mapSize(fileInfo, mapStart);
    MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, mapStart, mapSize);
    try {

      int lineStart = 0; // in buffer

      int mapIndex = 0;
      while (mapIndex < mapSize) {
        byte value = buffer.get(mapIndex);

        if (value == cr) {
          int newlineLength;
          // check if lf follows the cr
          // mapSize - mapIndex == buffer.remaining() + 1
          if (((mapSize - mapIndex) > 1) && (buffer.get(mapIndex + 1) == lf)) {
            newlineLength = 2;
          } else {
            newlineLength = 1;
          }

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up loop variable for the next iteration
          mapIndex = lineStart = mapIndex + newlineLength;

        } else if (value == lf) {

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up the loop variable for the next iteration
          mapIndex = lineStart = mapIndex + 1;
        } else {
          mapIndex += 1;
        }

      }

      if ((mapSize + mapStart) < fileSize) {
        // we could not map the entire file
        // map from the start of the last line
        // and continue reading from there
        return mapStart + lineStart; // may result in overlapping mapping
      } else if (lineStart < mapSize) {
        // we're at the end of the file
        // if the last line didn't end in a newline read it now
        readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);
      }

    } finally {
      unmap(buffer, fileInfo);
    }
    return FILE_END;
  }

  private static void readLine(int lineStart, long mapStart, int mapIndex,
          MappedByteBuffer buffer, LineReader reader, Consumer<Line> lineCallback) throws IOException {

    // read the current line into a CharSequence
    // create a Line object
    // call the callback
    int length = mapIndex - lineStart;
    CharSequence sequence = reader.readLine(buffer, lineStart, length);

    Line line = new Line(lineStart + mapStart, length, sequence);
    lineCallback.accept(line);
  }

  static final class FileInfo {

    final FileChannel channel;
    final long fileSize;
    final LineReader reader;
    final Consumer<Line> lineCallback;
    final Path path;

    FileInfo(Path path, FileChannel channel, long fileSize, LineReader reader, Consumer<Line> lineCallback) {
      this.path = path;
      this.channel = channel;
      this.fileSize = fileSize;
      this.reader = reader;
      this.lineCallback = lineCallback;
    }

  }

  static final class FastEncodingInfo {

    final byte cr;
    final byte lf;

    FastEncodingInfo(byte cr, byte lf) {
      this.cr = cr;
      this.lf = lf;
    }

  }

  static final class EncodingInfo {

    final byte[] cr;
    final byte[] lf;
    final Charset cs;

    EncodingInfo(Charset cs, byte[] cr, byte[] lf) {
      this.cs = cs;
      this.cr = cr;
      this.lf = lf;
    }

  }

  static final class BomResolutionResult {

    final Charset cs;
    final long mapStart;

    BomResolutionResult(Charset cs, long mapStart) {
      this.cs = cs;
      this.mapStart = mapStart;
    }

  }

  static void unmap(MappedByteBuffer buffer, FileInfo fileInfo) throws IOException {
    if (UNSAFE_INVOKE_CLEANER != null) {
      // Java 9
      try {
        UNSAFE_INVOKE_CLEANER.invokeExact((ByteBuffer) buffer);
      } catch (RuntimeException e) {
        throw e;
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        throw new UnmapFailedException(fileInfo.path.toString(), "could not unmap", e);
      }
    } else {
      // Java 8
      try {
        Object cleaner = DIRECT_BYTE_BUFFER_CLEANER.invoke(buffer);
        CLEANER_CLEAN.invoke(cleaner);
      } catch (RuntimeException e) {
        throw e;
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        throw new UnmapFailedException(fileInfo.path.toString(), "could not unmap", e);
      }
    }
  }

}
