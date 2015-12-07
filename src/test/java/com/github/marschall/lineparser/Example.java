package com.github.marschall.lineparser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Example {

  public static void main(String[] args) throws IOException {
    Path path = Paths.get("pom.xml");
    Charset cs = StandardCharsets.UTF_8;
    LineParser parser = new LineParser();
    parser.forEach(path, cs, (line) -> {
      System.out.printf("[%d,%d]%s%n", line.getOffset(), line.getLength(), line.getContent());
    });
  }

}
