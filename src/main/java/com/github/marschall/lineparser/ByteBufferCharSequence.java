package com.github.marschall.lineparser;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * {@link CharSequence} for Latin-1 compatible input that needs no decoding.
 */
final class ByteBufferCharSequence implements CharSequence {

  private final ByteBuffer buffer;

  ByteBufferCharSequence(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public String toString() {
    if (this.buffer.hasArray()) {
      byte[] array = this.buffer.array();
      int offset = this.buffer.arrayOffset();
      int length = this.buffer.capacity();
      return new String(array, offset, length, StandardCharsets.ISO_8859_1);
    } else {
      byte[] array = new byte[this.buffer.capacity()];
      this.buffer.get(array);
      return new String(array, StandardCharsets.ISO_8859_1);
    }
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
    this.buffer.position(start).limit(end - start);
    ByteBufferCharSequence subSequence = new ByteBufferCharSequence(buffer.slice());
    this.buffer.position(0).limit(this.buffer.capacity());
    return subSequence;
  }

}
