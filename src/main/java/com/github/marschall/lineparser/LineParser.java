package com.github.marschall.lineparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
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
 * @see Line
 */
public final class LineParser {

  private static final byte[] CR_LF = {'\r', '\n'};

  private static final byte[] LF = {'\n'};

  /**
   * Internal iterator over every line in a file.
   *
   * @param path the file to parse
   * @param cs the character set to use
   * @param lineCallback callback executed for every line
   * @throws IOException if an exception happens when reading
   */
  public void forEach(Path path, Charset cs, Consumer<Line> lineCallback) throws IOException {
    try (FileInputStream stream = new FileInputStream(path.toFile());
            FileChannel channel = stream.getChannel()) {
      long size = channel.size();
      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, size);
      try {
        CharBuffer charBuffer = CharBuffer.allocate(2048);
        CharsetDecoder decoder = cs.newDecoder();

        int start = 0;
        byte[] lf = "\n".getBytes(cs);
        byte[] cr = "\r".getBytes(cs);
        byte[] crlf = "\r\n".getBytes(cs);

        for (int i = start; i < size; ++i) {
          byte value = buffer.get();

          if (value == cr[0] && cr.length - 1 <= buffer.remaining()) {
            for (int j = 1; j < cr.length; j++) {
              if (buffer.get() != cr[j]) {
                buffer.position(i + 1);
                break;
              }
            }

            byte[] newline = cr;
            crlftest: if (lf.length <= buffer.remaining()) {
              for (int j = 0; j < lf.length; j++) {
                if (buffer.get() != lf[j]) {
                  break crlftest;
                }
              }
              newline = crlf;
            }

            buffer.position(start).limit(i);
            charBuffer = decode(buffer.slice(), charBuffer, decoder);
            Line line = new Line(start, i - start, charBuffer);
            lineCallback.accept(line);
            buffer.limit(buffer.capacity());
            int newineLength = newline.length;
            buffer.position(i + newineLength);
            start = i + newineLength;
            i += newineLength -1;
          } else if (value == lf[0]) {
            for (int j = 1; j < lf.length; j++) {
              if (buffer.get() != lf[j]) {
                buffer.position(i + 1);
                break;
              }
            }
            buffer.position(start).limit(i);
            charBuffer = decode(buffer.slice(), charBuffer, decoder);
            Line line = new Line(start, i - start, charBuffer);
            lineCallback.accept(line);
            buffer.limit(buffer.capacity());
            buffer.position(i + lf.length);
            start = i + lf.length;
            i += lf.length -1;
          }
        }

        if (start < size) {
          buffer.position(start);
          charBuffer = decode(buffer.slice(), charBuffer, decoder);
          Line line = new Line(start, (int) (size - start), charBuffer);
          lineCallback.accept(line);
        }

      } finally {
        unmap(buffer);
      }

    }
  }

  private static CharBuffer decode(ByteBuffer in, CharBuffer out, CharsetDecoder decoder) {
    in.rewind();
    out.rewind();
    CoderResult result = decoder.decode(in, out, true);
    if (result.isOverflow()) {
      int newCapacity = out.capacity() * 2;
      return decode(in, CharBuffer.allocate(newCapacity), decoder);
    } else {
      out.flip();
      return out;
    }
  }

  private static void unmap(MappedByteBuffer buffer) {
    try {
      Method cleanerMethod = buffer.getClass().getMethod("cleaner");
      if (!cleanerMethod.isAccessible()) {
        cleanerMethod.setAccessible(true);
      }
      Object cleaner = cleanerMethod.invoke(buffer);
      Method cleanMethod = cleaner.getClass().getMethod("clean");
      if (!cleanMethod.isAccessible()) {
        cleanMethod.setAccessible(true);
      }
      cleanMethod.invoke(cleaner);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not unmap buffer", e);
    }
//    sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
//    cleaner.clean();
  }

}
