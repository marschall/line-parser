package com.github.marschall.lineparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.file.Path;
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
 *  <li>single byte character sets</li>
 *  <li><a href="https://en.wikipedia.org/wiki/ISO/IEC_8859-1">ISO 8859-1</a> compatible character sets</li>
 * </ul>
 * <p>They can be combined for maximum performance.</p>
 *
 * <h2>Algorithm</h2>
 * The following algorithm is used to parse files:
 * <ol>
 *  <li>map the file into memory</li>
 *  <li>encode <code>CR</code>, <code>LF</code> and <code>CR LF</code> to bytes using the give character set</li>
 *  <li>while not done
 *    <ol>
 *      <li>scan the file byte by byte for cr of lf</li>
 *      <li>create a line object and invoke the callback</li>
 *    </ol>
 *  </li>
 *  <li>unmap the file from memory</li>
 * </ol>
 * <p>If the file is larger can 2GB then it is mapped into memory multiple times.
 * This has to be done because Java does not support file mappings larger than 2GB.</p>
 *
 * <p>Unmapping the file from memory is controversial and can only be done using semi-official
 * APIs. The alternative would be to rely on finalization to close file handles.</p>
 *
 * <p>This class is thread safe.</p>
 *
 * @see Line
 */
public final class LineParser {

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
   */
  public void forEach(Path path, Charset cs, Consumer<Line> lineCallback) throws IOException {
    try (FileInputStream stream = new FileInputStream(path.toFile());
            FileChannel channel = stream.getChannel()) {
      long fileSize = channel.size();
      LineReader reader = LineReader.forCharset(cs);
      byte[] cr = "\r".getBytes(cs);
      byte[] lf = "\n".getBytes(cs);
      byte[] crlf = "\r\n".getBytes(cs);
      boolean useFastPath = cr.length == 1 && lf.length == 1;
      if (useFastPath) {
        forEachFast(channel, cr[0], lf[0], fileSize, 0L, reader, lineCallback);
      } else {
        forEach(channel, cr, lf, crlf, fileSize, 0L, reader, lineCallback);
      }
    }
  }

  private void forEach(FileChannel channel, byte[] cr, byte[] lf, byte[] crlf,
          long fileSize, long mapStart, LineReader reader, Consumer<Line> lineCallback) throws IOException {
    int mapSize = (int) Math.min(fileSize - mapStart, maxMapSize);
    MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, mapStart, mapSize);
    try {

      int lineStart = 0; // in buffer
      int crLength = cr.length;
      int lfLength = lf.length;

      int mapIndex = 0;
      while (mapIndex < mapSize) {
        byte value = buffer.get(mapIndex);

        // mapSize - mapIndex == buffer.remaining() + 1
        if (startsWithArray(value, cr, crLength, mapIndex, mapSize, buffer)) {

          int newlineLength = crLength;
          if (continuesWithArray(lf, lfLength, crLength, mapIndex, mapSize, buffer)) {
            newlineLength += lfLength;
          }

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up loop variable for the next iteration
          mapIndex = lineStart = mapIndex + newlineLength;

        } else if (startsWithArray(value, lf, lfLength, mapIndex, mapSize, buffer)) {

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up the loop variable for the next iteration
          mapIndex = lineStart = mapIndex + lfLength;
        } else {
          mapIndex += 1;
        }

      }

      if (mapSize + mapStart < fileSize) {
        // we could not map the entire file
        // map from the start of the last line
        // and continue reading from there
        // TODO we should unmap now
        forEach(channel, cr, lf, crlf, fileSize, mapStart + lineStart, reader, lineCallback); // may result in overlapping mapping
      } else if (lineStart < mapSize) {
        // if the last line didn't end in a newline read it now
        readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);
      }

    } finally {
      unmap(buffer);
    }
  }

  private static boolean startsWithArray(byte value, byte[] newLine, int newLineLength,
          int mapIndex, int mapSize, MappedByteBuffer buffer) {
    if (value == newLine[0] && newLineLength - 1 < (mapSize - mapIndex)) {
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
  private void forEachFast(FileChannel channel, byte cr, byte lf,
          long fileSize, long mapStart, LineReader reader, Consumer<Line> lineCallback) throws IOException {
    int mapSize = (int) Math.min(fileSize - mapStart, maxMapSize);
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
          if ((mapSize - mapIndex) > 1 && buffer.get(mapIndex + 1) == lf) {
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

      if (mapSize + mapStart < fileSize) {
        // we could not map the entire file
        // map from the start of the last line
        // and continue reading from there
        // TODO we should unmap now
        forEachFast(channel, cr, lf, fileSize, mapStart + lineStart, reader, lineCallback); // may result in overlapping mapping
      } else if (lineStart < mapSize) {
        // if the last line didn't end in a newline read it now
        readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);
      }

    } finally {
      unmap(buffer);
    }
  }

  private static void readLine(int lineStart, long mapStart, int mapIndex,
          MappedByteBuffer buffer, LineReader reader, Consumer<Line> lineCallback) {

    // read the current line into a CharSequence
    // create a Line object
    // call the callback
    int length = mapIndex - lineStart;
    CharSequence sequence = reader.readLine(buffer, lineStart, length);

    Line line = new Line(lineStart + mapStart, length, sequence);
    lineCallback.accept(line);
  }

  private static void unmap(MappedByteBuffer buffer) {
    try {
      Method cleanerMethod = buffer.getClass().getMethod("cleaner");
      if (!cleanerMethod.isAccessible()) {
        cleanerMethod.setAccessible(true);
      }
      Object cleaner = cleanerMethod.invoke(buffer);
      if (cleaner instanceof Runnable) {
        // Java 9 branch
        // jdk.internal.ref.Cleaner cleaner = ((java.nio.DirectByteBufferR) buffer).cleaner();
        // cleaner.run();
        ((Runnable) cleaner).run();
      } else {
        // Java 8 branch
        // sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
        // cleaner.clean();
        Method cleanMethod = cleaner.getClass().getMethod("clean");
        if (!cleanMethod.isAccessible()) {
          cleanMethod.setAccessible(true);
        }
        cleanMethod.invoke(cleaner);
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not unmap buffer", e);
    }
  }

}
