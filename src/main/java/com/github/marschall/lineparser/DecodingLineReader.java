package com.github.marschall.lineparser;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Decodes lines into a {@link CharBuffer}.
 */
final class DecodingLineReader implements LineReader {

  private final CharsetDecoder decoder;
  private CharBuffer out;

  DecodingLineReader(Charset charset, int bufferSize) {
    this.decoder = charset.newDecoder();
    this.out = CharBuffer.allocate(bufferSize);
  }

  DecodingLineReader(Charset charset) {
    this(charset, 2048);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CharSequence readLine(ByteBuffer buffer) {
    this.decode(buffer);
    return this.out;
  }

  /**
   * Gets the current buffer. Only for testing.
   *
   * @return the current buffer
   */
  CharBuffer getOut() {
    return out;
  }

  private void decode(ByteBuffer in) {
    in.rewind();
    this.out.clear();
    CoderResult result = decoder.decode(in, this.out, true);
    if (result.isOverflow()) {
      int newCapacity = this.out.capacity() * 2;
      // double until it fits
      // this is not ideal be there isn't a good general way to estimate the required buffer size
      // using the line length in bytes would wasteful for UTF-16 or UTF-32
      // we could get creative with #averageCharsPerByte and #maxCharsPerByte
      // but would have to track if we got it wrong
      this.out = CharBuffer.allocate(newCapacity);
      decode(in);
    } else {
      out.flip();
    }
  }

}
