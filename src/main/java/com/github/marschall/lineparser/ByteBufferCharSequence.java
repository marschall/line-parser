package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.nio.ByteBuffer;

/**
 * {@link CharSequence} for Latin-1 compatible input that needs no decoding.
 */
final class ByteBufferCharSequence implements CharSequence {

  private final ByteBuffer buffer;
  private String stringValue;

  ByteBufferCharSequence(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public String toString() {
    if (this.stringValue == null) {
      if (this.buffer.hasArray()) {
        byte[] array = this.buffer.array();
        int offset = this.buffer.arrayOffset();
        int length = this.buffer.capacity();
        this.stringValue = new String(array, offset, length, ISO_8859_1);
      } else {
        byte[] array = new byte[this.buffer.capacity()];
        this.buffer.get(array);
        this.stringValue = new String(array, ISO_8859_1);
      }
    }
    return this.stringValue;
  }

  @Override
  public int length() {
    return this.buffer.capacity();
  }

  @Override
  public char charAt(int index) {
    return (char) (this.buffer.get(index) & 0xFF);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    this.buffer.position(start).limit(end);
    ByteBufferCharSequence subSequence = new ByteBufferCharSequence(buffer.slice());
    this.buffer.position(0).limit(this.buffer.capacity());
    return subSequence;
  }

}
