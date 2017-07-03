package com.github.marschall.lineparser;

import java.nio.ByteBuffer;

final class Utf8Decoder {

  int decode(ByteBuffer buffer, char[] target) {
    int position = buffer.position();
    int limit = buffer.limit();
    int outPosition = 0;

    int inPosition = position;
    while (inPosition < limit && outPosition < target.length) {
      int b = Byte.toUnsignedInt(buffer.get(inPosition));
      if (b < 0b10000000) {
        target[outPosition++] = (char) b;
      } else if (b < 0b11000000) {

      } else if (b < 0b11100000) {
        // split to surogate pairs
      } else {
        // split to surogate pairs

      }
    }
    return -1;
  }

  static int determineLength(ByteBuffer buffer) {
    return 0;
  }

}
