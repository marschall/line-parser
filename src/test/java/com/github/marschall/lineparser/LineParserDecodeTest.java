package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

public class LineParserDecodeTest {

  @Test
  public void decodeReuseBuffer() throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbccc".getBytes(US_ASCII));
    DecodingLineReader reader = new DecodingLineReader(US_ASCII, 3);
    CharBuffer charBuffer = reader.getOut();

    assertEquals("aaa", reader.readLine(byteBuffer, 0, 3).toString());
    assertSame(charBuffer, reader.getOut());

    assertEquals("bbb", reader.readLine(byteBuffer, 3, 3).toString());
    assertSame(charBuffer, reader.getOut());

    assertEquals("ccc", reader.readLine(byteBuffer, 6, 3).toString());
    assertSame(charBuffer, reader.getOut());
  }

  @Test
  public void decodeNewBuffer() throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbbcccc".getBytes(US_ASCII));
    DecodingLineReader reader = new DecodingLineReader(US_ASCII, 3);
    CharBuffer charBuffer = reader.getOut();

    assertEquals("aaa", reader.readLine(byteBuffer, 0, 3).toString());
    assertSame(charBuffer, reader.getOut());

    assertEquals("bbbb", reader.readLine(byteBuffer, 3, 4).toString());
    assertNotSame(charBuffer, reader.getOut());

    charBuffer = reader.getOut();
    assertEquals("cccc", reader.readLine(byteBuffer, 7, 4).toString());
    assertSame(charBuffer, reader.getOut());
  }

  @Test
  public void invalidUtf8() throws IOException {
    DecodingLineReader reader = new DecodingLineReader(UTF_8, 3);

    byte[] bytes = "\u00E4".getBytes(UTF_8);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length - 1);

    try {
      reader.readLine(byteBuffer, 0, bytes.length - 1);
      fail("invalid input");
    } catch (IOException e) {
      // should reach here
    }

    bytes = "\u1F60".getBytes(UTF_8);
    byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length - 1);
    bytes[bytes.length - 1] = (byte) 0b11000000;
    try {
      reader.readLine(byteBuffer, 0, bytes.length);
      fail("invalid input");
    } catch (IOException e) {
      // should reach here
    }
  }

}
