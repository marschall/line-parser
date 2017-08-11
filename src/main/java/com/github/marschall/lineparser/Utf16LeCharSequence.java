package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

final class Utf16LeCharSequence extends Utf16CharSequence {

  Utf16LeCharSequence(ByteBuffer buffer) throws IOException {
    super(buffer);
  }

  Utf16LeCharSequence(ByteBuffer buffer, int offset, int byteLength) throws IOException {
    super(buffer, offset, byteLength);
  }

  @Override
  Charset getCharset() {
    return UTF_16LE;
  }

  @Override
  public char charAt(int index) {
    if ((index < 0) || (index >= this.length())) {
      throw new IndexOutOfBoundsException();
    }

    int position = this.offset + (index * 2);
    return (char) ((this.buffer.get(position) & 0xFF)| ((this.buffer.get(position + 1) & 0xFF)  << 8));
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if ((start < 0) || (start > this.length()) || (start > end) || (end > this.length())) {
      throw new IndexOutOfBoundsException();
    }
    return new ByteBufferCharSequence(this.buffer, this.offset + (start * 2), (end - start) * 2);
  }

}
