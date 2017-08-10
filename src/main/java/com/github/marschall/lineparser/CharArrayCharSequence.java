package com.github.marschall.lineparser;

import java.util.Objects;

final class CharArrayCharSequence implements CharSequence {

  private final char[] array;
  private String stringValue;

  CharArrayCharSequence(char[] array) {
    Objects.requireNonNull(array);
    this.array = array;
  }

  @Override
  public String toString() {
    if (this.stringValue == null) {
      this.stringValue = new String(this.array);
    }
    return this.stringValue;
  }

  @Override
  public int length() {
    return this.array.length;
  }

  @Override
  public char charAt(int index) {
    return this.array[index];
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if ((start < 0) || (start > this.array.length) || (start > end) || (end > this.array.length)) {
      throw new IndexOutOfBoundsException();
    }
    return new CharArrayCharSubSequence(this.array, start, end - start);
  }

  final class CharArrayCharSubSequence implements CharSequence {

    private final char[] array;
    private final int offset;
    private final int count;
    private String stringValue;

    CharArrayCharSubSequence(char[] array, int offset, int length) {
      Objects.requireNonNull(array);
      this.array = array;
      this.offset = offset;
      this.count = length;
    }

    @Override
    public String toString() {
      if (this.stringValue == null) {
        this.stringValue = new String(this.array, this.offset, this.count);
      }
      return this.stringValue;
    }

    @Override
    public int length() {
      return this.count;
    }

    @Override
    public char charAt(int index) {
      if ((index < 0) || (index >= this.count)) {
      throw new IndexOutOfBoundsException();
    }
      return this.array[this.offset + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      if ((start < 0) || (start > this.count) || (start > end) || (end > this.count)) {
        throw new IndexOutOfBoundsException();
      }
      return new CharArrayCharSubSequence(this.array, start, end - start);
    }

  }


}
