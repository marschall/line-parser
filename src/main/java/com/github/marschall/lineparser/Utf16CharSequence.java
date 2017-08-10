package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.UTF_16BE;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

abstract class Utf16CharSequence implements CharSequence {

  final ByteBuffer buffer;
  final int offset;
  final int byteLength;
  String stringValue;

  Utf16CharSequence(ByteBuffer buffer) {
    this(buffer, 0, buffer.capacity());
  }

  Utf16CharSequence(ByteBuffer buffer, int offset, int byteLength) {
    Objects.requireNonNull(buffer);
    if ((byteLength & 0b1) == 1) {
      throw new IllegalArgumentException("malformed UTF-16");
    }
    this.buffer = buffer;
    this.offset = offset;
    this.byteLength = byteLength;
  }

  @Override
  public String toString() {
    if (this.stringValue == null) {
      if (this.buffer.hasArray()) {
        byte[] array = this.buffer.array();
        int arrayOffset = this.buffer.arrayOffset() + this.offset;
        this.stringValue = new String(array, arrayOffset, this.byteLength, this.getCharset());
      } else {
        byte[] array = new byte[this.byteLength];
        for (int i = 0; i < array.length; i++) {
          array[i] = this.buffer.get(i + this.offset);
        }
        this.stringValue = new String(array, this.getCharset());
      }
    }
    return this.stringValue;
  }

  Charset getCharset() {
    return UTF_16BE;
  }

  @Override
  public int length() {
    return this.byteLength * 2;
  }

}
