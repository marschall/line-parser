package com.github.marschall.lineparser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class Utf16Test {

  private static final String S = "a\u00E4\u1F60\uD83D\uDE02";

  private final CharSequence sequence;

  public Utf16Test(CharSequence sequence) {
    this.sequence = sequence;
  }

  @Parameters
  public static Iterable<Object[]> data() throws IOException {
      return Arrays.asList(
              new Object[][] {
                { new Utf16LeCharSequence(asHeapBuffer(StandardCharsets.UTF_16LE)) },
                { new Utf16LeCharSequence(asNative(StandardCharsets.UTF_16LE)) },
                { new Utf16BeCharSequence(asHeapBuffer(StandardCharsets.UTF_16BE)) },
                { new Utf16BeCharSequence(asNative(StandardCharsets.UTF_16BE)) }
              });
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

  @Test
  public void testToString() {
    assertEquals(S, this.sequence.toString());
  }


  @Test
  public void length() {
    assertEquals(S.length(), this.sequence.length());
  }

  @Test
  public void charAt() {
    for (int i = 0; i < S.length(); i++) {
      assertEquals(S.charAt(i), this.sequence.charAt(i));
    }
  }
  @Test
  public void subSequence() {
    assertEquals(S.substring(1, S.length() - 2), this.sequence.subSequence(1, this.sequence.length() - 2).toString());
    assertEquals("", this.sequence.subSequence(0, 0).toString());
  }

  @Test
  public void chars() {
    assertArrayEquals(S.chars().toArray(), this.sequence.chars().toArray());
  }

  @Test
  public void codePoints() {
    assertArrayEquals(S.codePoints().toArray(), this.sequence.codePoints().toArray());
  }

}
