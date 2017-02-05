package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.nio.ByteBuffer;

/**
 * {@link CharSequence} for Latin-1 compatible input that needs no decoding.
 *
 * <p>Avoid {@link ByteBuffer#slice()} because it allocates quite a lot.
 */
final class ByteBufferCharSequence implements CharSequence {

  private final ByteBuffer buffer;
  private final int offset;
  private final int length;
  private String stringValue;

  ByteBufferCharSequence(ByteBuffer buffer) {
    this(buffer, 0, buffer.capacity());
  }

  ByteBufferCharSequence(ByteBuffer buffer, int offset, int length) {
    this.buffer = buffer;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public String toString() {
    if (this.stringValue == null) {
      if (this.buffer.hasArray()) {
        byte[] array = this.buffer.array();
        int arrayOffset = this.buffer.arrayOffset() + this.offset;
        this.stringValue = new String(array, arrayOffset, this.length, ISO_8859_1);
      } else {
        byte[] array = new byte[this.length];
        for (int i = 0; i < array.length; i++) {
          array[i] = this.buffer.get(i + this.offset);
        }
        this.stringValue = new String(array, ISO_8859_1);
      }
    }
    return this.stringValue;
  }

  @Override
  public int length() {
    return this.length;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index >= this.length) {
      throw new IndexOutOfBoundsException();
    }
    return (char) (this.buffer.get(this.offset + index) & 0xFF);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if (start < 0 || start > this.length || start > end || end > this.length) {
      throw new IndexOutOfBoundsException();
    }
    return new ByteBufferCharSequence(this.buffer, this.offset + start, end - start);
  }

}
