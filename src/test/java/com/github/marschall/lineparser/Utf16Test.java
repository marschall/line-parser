package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class Utf16Test {

  private static final String S = "a\u00E4\u1F60\uD83D\uDE02";

  public static Stream<CharSequence> data() throws IOException {
      return Stream.of(
                new Utf16LeCharSequence(asHeapBuffer(StandardCharsets.UTF_16LE)),
                new Utf16LeCharSequence(asNative(StandardCharsets.UTF_16LE)),
                new Utf16BeCharSequence(asHeapBuffer(StandardCharsets.UTF_16BE)),
                new Utf16BeCharSequence(asNative(StandardCharsets.UTF_16BE))
              );
  }

  private static ByteBuffer asHeapBuffer(Charset cs) {
    return ByteBuffer.wrap(S.getBytes(cs));
  }

  private static ByteBuffer asNative(Charset cs) {
    ByteBuffer heapBuffer = asHeapBuffer(cs);
    ByteBuffer nativeBuffer = ByteBuffer.allocateDirect(heapBuffer.capacity());
    nativeBuffer.put(heapBuffer);
    nativeBuffer.flip();
    return nativeBuffer;
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testToString(CharSequence sequence) {
    assertEquals(S, sequence.toString());
  }


  @ParameterizedTest
  @MethodSource("data")
  public void length(CharSequence sequence) {
    assertEquals(S.length(), sequence.length());
  }

  @ParameterizedTest
  @MethodSource("data")
  public void charAt(CharSequence sequence) {
    for (int i = 0; i < S.length(); i++) {
      assertEquals(S.charAt(i), sequence.charAt(i));
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public void subSequence(CharSequence sequence) {
    assertEquals(S.substring(1, S.length() - 2), sequence.subSequence(1, sequence.length() - 2).toString());
    assertEquals("", sequence.subSequence(0, 0).toString());
  }

  @ParameterizedTest
  @MethodSource("data")
  public void chars(CharSequence sequence) {
    assertArrayEquals(S.chars().toArray(), sequence.chars().toArray());
  }

  @ParameterizedTest
  @MethodSource("data")
  public void codePoints(CharSequence sequence) {
    assertArrayEquals(S.codePoints().toArray(), sequence.codePoints().toArray());
  }

}
