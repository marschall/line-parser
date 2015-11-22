package com.github.marschall.lineparser;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class OffsetAndLengthTest {

  @Test
  public void noEmptyLines() throws IOException {
    Path tempFile = Files.createTempFile("OffsetAndLengthTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.ISO_8859_1)) {
      writer.append("a");
      writer.append("\r\n");
      writer.append("bc");
      writer.append("\n");
      writer.append("d");
    }
    try {
      List<OffsetAndLength> expected = Arrays.asList(
              new OffsetAndLength(0, 1),
              new OffsetAndLength(3, 2),
              new OffsetAndLength(6, 1)
              );
      List<OffsetAndLength> acutal = readLinesMapped(tempFile, StandardCharsets.ISO_8859_1);

      assertEquals(expected, acutal);

    } finally {
      Files.delete(tempFile);
    }
  }

  private static List<OffsetAndLength> readLinesMapped(Path path, Charset cs) throws IOException {
    List<OffsetAndLength> lines = new ArrayList<>();
    LineParser parser = new LineParser();
    parser.forEach(path, cs, line ->
      lines.add(new OffsetAndLength(line.getOffset(), line.getLength())));
    return lines;
  }

  static final class OffsetAndLength {

    final long offset;
    final int length;

    OffsetAndLength(long offset, int length) {
      this.offset = offset;
      this.length = length;
    }

    @Override
    public int hashCode() {
      return 31 * (31 + this.length) + (int) (this.offset ^ (this.offset >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof OffsetAndLength)) {
        return false;
      }
      OffsetAndLength other = (OffsetAndLength) obj;
      return this.length == other.length
              && this.offset == other.offset;
    }

  }

}
