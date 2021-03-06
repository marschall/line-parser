package com.github.marschall.lineparser;

import java.nio.ByteBuffer;

/**
 * Does not decode lines, works only for Latin-1 compatible encodings.
 */
final class NonDecodingLineReader implements LineReader {

  /**
   * {@inheritDoc}
   */
  @Override
  public CharSequence readLine(ByteBuffer buffer, int start, int length) {
    return new ByteBufferCharSequence(buffer, start, length);
  }

}
