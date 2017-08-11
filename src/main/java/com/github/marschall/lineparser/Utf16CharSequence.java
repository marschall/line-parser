package com.github.marschall.lineparser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

abstract class Utf16CharSequence implements CharSequence {

  protected final ByteBuffer buffer;
  protected final int offset;
  protected final int byteLength;
  private String stringValue;

  Utf16CharSequence(ByteBuffer buffer) throws IOException {
    this(buffer, 0, buffer.capacity());
  }

  Utf16CharSequence(ByteBuffer buffer, int offset, int byteLength) throws IOException {
    Objects.requireNonNull(buffer);
    if ((byteLength & 0b1) == 1) {
      throw new MalformedInputException(byteLength);
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

  abstract Charset getCharset();

  @Override
  public int length() {
    // divide by 2
    return this.byteLength >> 1;
  }

  @Override
  public IntStream chars() {
    return StreamSupport.intStream(new CharSequenceSpliterator(this), false);
  }

  // we use the default implementation for #codePoints because some chars
  // may be surrogates

}
