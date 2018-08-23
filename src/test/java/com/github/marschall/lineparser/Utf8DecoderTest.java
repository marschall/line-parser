package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class Utf8DecoderTest {

  private static final List<String> SINGLE_CODEPOINT_STRINGS = Arrays.asList("a", "\u00E4", "\u1F60", "\uD83D\uDE02");

  @Test
  public void validBytes() {
    for (String s : SINGLE_CODEPOINT_STRINGS) {
      assertEquals(1, s.codePointCount(0, s.length()));
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      char[] array = new char[s.length()];
      assertEquals(s.length(), Utf8Decoder.decode(buffer, array));
      assertEquals(s, new String(array));
    }
  }

  @Test
  public void missingBytes() {
    for (String s : SINGLE_CODEPOINT_STRINGS) {
      assertEquals(1, s.codePointCount(0, s.length()));
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      if (bytes.length > 1) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.limit(buffer.limit() - 1);
        char[] array = new char[s.length()];
        assertEquals(Utf8Decoder.MALFORMED, Utf8Decoder.decode(buffer, array));
      }
    }
  }

  @Test
  public void invalidBytes() {
    for (String s : SINGLE_CODEPOINT_STRINGS) {
      assertEquals(1, s.codePointCount(0, s.length()));
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      if (bytes.length > 1) {
        bytes[bytes.length - 1] = (byte) 0b11000000;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        char[] array = new char[s.length()];
        assertEquals(Utf8Decoder.MALFORMED, Utf8Decoder.decode(buffer, array));
      }
    }
  }

  @Test
  public void targetTooSmall() {
    for (String s : SINGLE_CODEPOINT_STRINGS) {
      assertEquals(1, s.codePointCount(0, s.length()));
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      char[] array = new char[s.length() - 1];
      assertEquals(Utf8Decoder.TARGET_UNDERFLOW, Utf8Decoder.decode(buffer, array));
    }
  }

}
