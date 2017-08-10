package com.github.marschall.lineparser;

import java.nio.ByteBuffer;

final class Utf16LeLineReader implements LineReader {

  @Override
  public CharSequence readLine(ByteBuffer buffer, int start, int length) {
    return new Utf16LeCharSequence(buffer, start, length);
  }

}
