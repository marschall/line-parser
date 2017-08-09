package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EncodingTest {

  private static final List<String> MESSAGE_WITHOUT_BOM = Arrays.asList("a", "\u00E4", "\u1F600");
  private static final List<String> MESSAGE_WITH_BOM = Arrays.asList("\uFEFFa", "\u00E4", "\u1F600");

  private LineParser parser;

  @Before
  public void setUp() {
    this.parser = new LineParser();
  }

  @Test
  public void bomDecoding() throws IOException {
    assertEquals(MESSAGE_WITH_BOM, this.parse("utf8-with-bom.txt", UTF_8));
  }

  @Test
  public void noBom() throws IOException {
    assertEquals(MESSAGE_WITHOUT_BOM, this.parse("utf8-no-bom.txt", UTF_8));
  }

  @Test
  public void bomInFileBomInJava() throws IOException {
    assertEquals(MESSAGE_WITH_BOM, this.parse("utf16be-with-bom.txt", UTF_16));
    assertEquals(MESSAGE_WITH_BOM, this.parse("utf16le-with-bom.txt", UTF_16));
  }

  @Test
  public void bomInFileNoBomInJava() throws IOException {
    assertEquals(MESSAGE_WITHOUT_BOM, this.parse("utf32be-with-bom.txt", Charset.forName("UTF-32")));
    assertEquals(MESSAGE_WITHOUT_BOM, this.parse("utf32le-with-bom.txt", Charset.forName("UTF-32")));
  }

  private List<String> parse(String fileName, Charset cs) throws IOException {
    Path base = Paths.get("src/test/resources/examples");
    List<String> lines  = new ArrayList<>(2);
    this.parser.forEach(base.resolve(fileName), cs, line ->
      lines.add(line.getContent().toString()));
    return lines;
  }

}
