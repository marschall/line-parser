package com.github.marschall.lineparser;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

public class ByteBufferCharSequenceTest {

  private String string;
  private CharSequence charSequence;

  @Before
  public void setUp() {
    this.string = "\u00DCber";

    byte[] exactBytes = this.string.getBytes(StandardCharsets.ISO_8859_1);
    byte[] enlargedBytes = new byte[exactBytes.length + 2];
    System.arraycopy(exactBytes, 0, enlargedBytes, 1, exactBytes.length);
    ByteBuffer enlargedBuffer = ByteBuffer.wrap(enlargedBytes);
    enlargedBuffer.position(1).limit(1 + exactBytes.length);
    ByteBuffer buffer = enlargedBuffer.slice();
    enlargedBuffer.position(0).limit(enlargedBuffer.capacity());

    this.charSequence = new ByteBufferCharSequence(buffer);
  }

  @Test
  public void length() {
    assertEquals(this.string.length(), this.charSequence.length());
  }

  @Test
  public void charAt() {
    for (int i = 0; i < this.string.length(); ++i) {
      assertEquals(this.string.charAt(i), this.charSequence.charAt(i));
    }
  }

  @Test
  public void testToString() {
    assertEquals(this.string, this.charSequence.toString());
  }

  @Test
  public void subSequence() {
    int length = this.string.length();
    for (int i = 0; i <= length; ++i) {
      assertEquals(this.string.substring(i, length), this.charSequence.subSequence(i, length).toString());
    }
    for (int i = 0; i <= length; ++i) {
      assertEquals(this.string.substring(0, length - i), this.charSequence.subSequence(0, length - i).toString());
    }
  }

  @Test
  public void natioveBuffer() {

    byte[] bytes = this.string.getBytes(StandardCharsets.ISO_8859_1);

    ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length + 2);
    directBuffer.position(1);
    directBuffer.put(bytes);

    directBuffer.position(1).limit(1 + bytes.length);
    ByteBuffer buffer = directBuffer.slice();

    directBuffer.position(0).limit(directBuffer.capacity());

    CharSequence directSequence = new ByteBufferCharSequence(buffer);

    assertEquals(this.string, directSequence.toString());
  }

}
