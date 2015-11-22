package com.github.marschall.lineparser;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LineParserTest {

  private final Charset cs;
  private final String newline;

  public LineParserTest(Charset cs, String newline) {
    this.cs = cs;
    this.newline = newline;
  }

  @Parameters
  public static Iterable<Object[]> data() {
      return Arrays.asList(
              new Object[][] {
                { StandardCharsets.UTF_8, "\r\n" },
                { StandardCharsets.UTF_8, "\r" },
                { StandardCharsets.UTF_8, "\n" },
                { StandardCharsets.ISO_8859_1, "\r\n" },
                { StandardCharsets.ISO_8859_1, "\r" },
                { StandardCharsets.ISO_8859_1, "\n" },
                { StandardCharsets.UTF_16LE, "\r\n" },
                { StandardCharsets.UTF_16LE, "\r" },
                { StandardCharsets.UTF_16LE, "\n" }
              });
  }

  @Test
  public void emptyLines() throws IOException {
    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, this.cs)) {
      writer.append(this.newline);

      writer.append("a");

      writer.append(this.newline);
      writer.append(this.newline);

      writer.append("bc");

      writer.append(this.newline);

      writer.append("d");

      writer.append(this.newline);
    }
    try {
      List<String> expected = readLinesBuffered(tempFile, cs);
      List<String> acutal = readLinesMapped(tempFile, cs);

      assertEquals(expected, acutal);

    } finally {
      Files.delete(tempFile);
    }
  }

  @Test
  public void noEmptyLines() throws IOException {
    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, this.cs)) {
      writer.append("a");
      writer.append(this.newline);
      writer.append("bc");
      writer.append(this.newline);
      writer.append("d");
    }
    try {
      List<String> expected = readLinesBuffered(tempFile, cs);
      List<String> acutal = readLinesMapped(tempFile, cs);

      assertEquals(expected, acutal);

    } finally {
      Files.delete(tempFile);
    }
  }

  @Test
  public void longLine() throws IOException {
    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, this.cs)) {
      for (int i = 0; i < 8192; i++) {
        writer.append("a");
      }
    }
    try {
      List<String> expected = readLinesBuffered(tempFile, cs);
      List<String> acutal = readLinesMapped(tempFile, cs);

      assertEquals(expected, acutal);

    } finally {
      Files.delete(tempFile);
    }
  }

  private static List<String> readLinesBuffered(Path path, Charset cs) throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(path, cs)) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }

  private static List<String> readLinesMapped(Path path, Charset cs) throws IOException {
    List<String> lines = new ArrayList<>();
    LineParser parser = new LineParser();
    parser.forEach(path, cs, line ->
      lines.add(line.getContent().toString()));
    return lines;
  }

}
