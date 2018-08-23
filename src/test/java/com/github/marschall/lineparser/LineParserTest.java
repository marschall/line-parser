package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class LineParserTest {

  public static Stream<Object[]> data() {
      return Stream.of(
                new Object[] { StandardCharsets.UTF_8, "\r\n" },
                new Object[] { StandardCharsets.UTF_8, "\r" },
                new Object[] { StandardCharsets.UTF_8, "\n" },
                new Object[] { StandardCharsets.ISO_8859_1, "\r\n" },
                new Object[] { StandardCharsets.ISO_8859_1, "\r" },
                new Object[] { StandardCharsets.ISO_8859_1, "\n" },
                new Object[] { StandardCharsets.UTF_16LE, "\r\n" },
                new Object[] { StandardCharsets.UTF_16LE, "\r" },
                new Object[] { StandardCharsets.UTF_16LE, "\n" }
              );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void emptyLines(Charset cs, String newline) throws IOException {
    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, cs)) {
      writer.append(newline);

      writer.append("a");

      writer.append(newline);
      writer.append(newline);

      writer.append("bc");

      writer.append(newline);

      writer.append("d");

      writer.append(newline);
    }
    try {
      List<String> expected = readLinesBuffered(tempFile, cs);
      List<String> acutal = readLinesMapped(tempFile, cs);

      assertEquals(expected, acutal);

    } finally {
      Files.delete(tempFile);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public void noEmptyLines(Charset cs, String newline) throws IOException {
    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, cs)) {
      writer.append("a");
      writer.append(newline);
      writer.append("bc");
      writer.append(newline);
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

  @ParameterizedTest
  @MethodSource("data")
  public void fileLargerThanMapped(Charset cs, String newline) throws IOException {
    int lineLength = 100;
    int lineCount = 10;

    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, cs)) {
      for (int i = 0; i < lineCount; i++) {
        char c = (char) ('A' + i);
        for (int j = 0; j < lineLength; j++) {
          writer.write(c);
        }
        writer.append(newline);
      }
    }
    try {
      List<String> expected = readLinesBuffered(tempFile, cs);
      List<String> acutal = readLinesMapped(tempFile, cs, lineLength * 4); // utf-16 is two bytes

      assertEquals(expected, acutal);

    } finally {
      Files.delete(tempFile);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public void longLine(Charset cs, String newline) throws IOException {
    Path tempFile = Files.createTempFile("LineParserTest", null);
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, cs)) {
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

  private static List<String> readLinesMapped(Path path, Charset cs, int maxBufferSize) throws IOException {
    List<String> lines = new ArrayList<>();
    LineParser parser = new LineParser(maxBufferSize);
    parser.forEach(path, cs, line ->
    lines.add(line.getContent().toString()));
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
