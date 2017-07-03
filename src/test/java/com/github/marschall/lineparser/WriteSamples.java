package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class WriteSamples {

  public static void main(String[] args) throws IOException {
    Path base = Paths.get("src/test/resources/examples");
    String message = "a\r\n\u00E4\r\n\u1F600";
//    String message = "a";

    write(message, UTF_8, base.resolve("utf8-no-bom.txt"), false);
    write(message, UTF_8, base.resolve("utf8-with-bom.txt"), true);
    write(message, UTF_16BE, base.resolve("utf16be-with-bom.txt"), true);
    write(message, UTF_16LE, base.resolve("utf16le-with-bom.txt"), true);
    write(message, Charset.forName("UTF-32BE"), base.resolve("utf32be-with-bom.txt"), true);
    write(message, Charset.forName("UTF-32LE"), base.resolve("utf32le-with-bom.txt"), true);
  }

  private static void write(String s, Charset cs, Path p, boolean writeBom) throws IOException {
    if (writeBom) {
      s = "\uFEFF" + s;
    }
    ByteBuffer encoded = cs.encode(s);
    try (SeekableByteChannel channel = Files.newByteChannel(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
      channel.write(encoded);
    }
  }

}
