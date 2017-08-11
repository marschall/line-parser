package com.github.marschall.lineparser;

import java.io.IOException;
import java.nio.ByteBuffer;

final class Utf16LeLineReader implements LineReader {

  @Override
  public CharSequence readLine(ByteBuffer buffer, int start, int length) throws IOException {
    return new Utf16LeCharSequence(buffer, start, length);
  }

}
