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
      byte[] lf = "\n".getBytes(cs);
      byte[] cr = "\r".getBytes(cs);
      byte[] crlf = "\r\n".getBytes(cs);
      forEach(channel, cr, lf, crlf, fileSize, 0L, reader, lineCallback);
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
      scanloop: while (mapIndex < mapSize) {
        byte value = buffer.get(mapIndex);

        // mapSize - mapIndex == buffer.remaining() + 1
        if (value == cr[0] && crLength - 1 < (mapSize - mapIndex)) {
          // input starts with the first byte of a cr, but cr may be multiple bytes
          // check if the input starts with all bytes of a cr
          for (int i = 1; i < crLength; i++) {
            if (buffer.get(mapIndex + i) != cr[i]) {
              // wasn't a cr after all
              // the the buffer state and loop variable
              mapIndex += 1;
              continue scanloop;
            }
          }

          int newlineLength = crLength;
          // check if lf follows the cr
          crlftest: if (lfLength < (mapSize - mapIndex)) {
            for (int i = 0; i < lfLength; i++) {
              if (buffer.get(mapIndex + crLength + i) != lf[i]) {
                // not a lf
                // be don't need to fix the buffer state here
                // having the information that the newline is just a cr is enough
                // to make the read
                break crlftest;
              }
            }
            newlineLength += lfLength;
          }

          // we found the end, read the line
          readLine(lineStart, mapStart, mapIndex, buffer, reader, lineCallback);

          // fix up loop variable for the next iteration
          mapIndex = lineStart = mapIndex + newlineLength;

        } else if (value == lf[0] && lfLength - 1 < (mapSize - mapIndex)) {
          // input starts with the first byte of a lf, but lf may be multiple bytes
          // check if the input starts with all bytes of a lf
          for (int i = 1; i < lfLength; i++) {
            if (buffer.get(mapIndex + i) != lf[i]) {
              // wasn't a lf after all
              // the the buffer state and loop variable
              mapIndex += 1;
              continue scanloop;
            }
          }

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

  private static void readLine(int lineStart, long mapStart, int mapIndex,
          MappedByteBuffer buffer, LineReader reader, Consumer<Line> lineCallback) {

    // read the current line into a CharSequence
    // create a Line object
    // call the callback
    // reset the buffer limit
    buffer.position(lineStart).limit(mapIndex);
    CharSequence sequence = reader.readLine(buffer.slice());
    // undo buffer limit, position doesn't matter because we only do absolute gets
    buffer.limit(buffer.capacity());

    Line line = new Line(lineStart + mapStart, mapIndex - lineStart, sequence);
    lineCallback.accept(line);
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
