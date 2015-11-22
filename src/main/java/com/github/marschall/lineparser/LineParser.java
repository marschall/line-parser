package com.github.marschall.lineparser;

import java.io.FileInputStream;
import java.io.IOException;
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

public final class LineParser {

  private static final byte[] CR_LF = {'\r', '\n'};

  private static final byte[] LF = {'\n'};

  public void forEach(Path path, Charset charset, Consumer<Line> consumer) throws IOException {
    try (FileInputStream stream = new FileInputStream(path.toFile());
            FileChannel channel = stream.getChannel()) {
      long size = channel.size();
      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, size);
      try {
        CharBuffer charBuffer = CharBuffer.allocate(2048);
        CharsetDecoder decoder = charset.newDecoder();

        int start = 0;
        byte[] lf = "\n".getBytes(charset);
        byte[] crlf = "\r\n".getBytes(charset);

        for (int i = start; i < size; ++i) {
          byte value = buffer.get();

          if (value == crlf[0] && crlf.length - 1 <= buffer.remaining()) {
            for (int j = 1; j < crlf.length; j++) {
              if (buffer.get() != crlf[j]) {
                buffer.position(i + 1);
                break;
              }
            }
            buffer.position(start).limit(i);
            charBuffer = decode(buffer, charBuffer, decoder);
            Line line = new Line(start, i - start, charBuffer);
            consumer.accept(line);
            buffer.position(i + crlf.length);
          } else if (value == lf[0]) {
            for (int j = 1; j < lf.length; j++) {
              if (buffer.get() != lf[j]) {
                buffer.position(i + 1);
                break;
              }
            }
            buffer.position(start).limit(i);
            charBuffer = decode(buffer, charBuffer, decoder);
            Line line = new Line(start, i - start, charBuffer);
            consumer.accept(line);
            buffer.position(i + lf.length);
          }
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
    if (result.isUnderflow()) {
      int newCapacity = out.capacity() * 2;
      return decode(in, CharBuffer.allocate(newCapacity), decoder);
    } else {
      out.flip();
      return out;
    }
  }

  private static void unmap(MappedByteBuffer buffer) {
    try {
      Object cleaner = buffer.getClass().getMethod("cleaner").invoke(buffer);
      cleaner.getClass().getMethod("clean").invoke(cleaner);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not unmap buffer", e);
    }
//    sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
//    cleaner.clean();
  }

}
