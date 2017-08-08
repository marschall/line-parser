package com.github.marschall.lineparser;

import java.nio.CharBuffer;

public class CharBufferCharSequence implements CharSequence {

  private final CharBuffer buffer;
  private final int offset;
  private final int length;
  private String stringValue;

  CharBufferCharSequence(CharBuffer buffer) {
    this(buffer, 0, buffer.capacity());
  }

  CharBufferCharSequence(CharBuffer buffer, int offset, int length) {
    this.buffer = buffer;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public String toString() {
    if (this.stringValue == null) {
      if (this.buffer.hasArray()) {
        char[] array = this.buffer.array();
        int arrayOffset = this.buffer.arrayOffset() + this.offset;
        this.stringValue = new String(array, arrayOffset, this.length);
      } else {
        char[] array = new char[this.length];
        for (int i = 0; i < array.length; i++) {
          array[i] = this.buffer.get(i + this.offset);
        }
        this.stringValue = new String(array);
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
    if ((index < 0) || (index >= this.length)) {
      throw new IndexOutOfBoundsException();
    }
    return (char) (this.buffer.get(this.offset + index) & 0xFF);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if ((start < 0) || (start > this.length) || (start > end) || (end > this.length)) {
      throw new IndexOutOfBoundsException();
    }
    return new CharBufferCharSequence(this.buffer, this.offset + start, end - start);
  }

}
