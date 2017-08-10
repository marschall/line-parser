package com.github.marschall.lineparser;

import java.nio.ByteBuffer;

final class Utf8Decoder {

  static final int MALFORMED = -2;

  static final int TARGET_UNDERFLOW = -3;

  static int decode(ByteBuffer buffer, char[] target) {
    int position = buffer.position();
    int limit = buffer.limit();
    int outPosition = 0;

    int targetCapacity = target.length;

    int inPosition = position;
    while (inPosition < limit) {
      if (outPosition >= targetCapacity) {
        return TARGET_UNDERFLOW;
      }

      int firstByte = buffer.get(inPosition) & 0xFF;
      inPosition += 1;
      if (firstByte < 0b10000000) {
        // 0xxxxxxx
        // no need to mask high bit since it's 0 anyway
        target[outPosition++] = (char) firstByte;
      } else { // more than one byte

        int inRemaining = limit - inPosition;
        if (inRemaining == 0) {
          return MALFORMED;
        }

        int secondByte = buffer.get(inPosition++) & 0xFF;
        // second byte has to be 10xxxxxx
        if  (secondByte >= 0b11000000) {
          return MALFORMED;
        }

        if (firstByte < 0b11100000) { // two bytes
          // 110xxxxx
          char c = (char) (((firstByte & 0b00011111) << 6) | (secondByte & 0b00111111));
          target[outPosition++] = c;

        } else { // more than two bytes

          inRemaining = limit - inPosition;
          if (inRemaining == 0) {
            return MALFORMED;
          }

          int thirdByte = buffer.get(inPosition++) & 0xFF;
          // second byte has to be 10xxxxxx
          if  (thirdByte >= 0b11000000) {
            return MALFORMED;
          }

          if (firstByte < 0b11110000) { // three bytes
            // 1110xxxx
            char c = (char) (((firstByte & 0b00001111) << 12)
                    | ((secondByte & 0b00111111) << 6)
                    | (thirdByte & 0b00111111));
            target[outPosition++] = c;

          } else if (firstByte < 0b11111000) { // four bytes
            // 11110xxx

            inRemaining = limit - inPosition;
            if (inRemaining == 0) {
              return MALFORMED;
            }

            int fourthByte = buffer.get(inPosition++) & 0xFF;
            // second byte has to be 10xxxxxx
            if  (fourthByte >= 0b11000000) {
              return MALFORMED;
            }

            int codePoint = ((firstByte & 0b00000111) << 18)
                    | ((secondByte & 0b00111111) << 12)
                    | ((thirdByte & 0b00111111) << 6)
                    | (fourthByte & 0b00111111);

            // split to surrogate pairs

            target[outPosition++] = Character.highSurrogate(codePoint);

            if (outPosition >= targetCapacity) {
              return TARGET_UNDERFLOW;
            }

            target[outPosition++] = Character.lowSurrogate(codePoint);


          } else {
            return MALFORMED;
          }
        }
      }
    }
    return outPosition;
  }

  static int determineLength(ByteBuffer buffer) {
    return 0;
  }

}
