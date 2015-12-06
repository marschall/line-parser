package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Reads a line into a {@link CharSequence}.
 */
interface LineReader {

  /**
   * Reads a line.
   *
   * @param buffer the line in bytes
   * @return the line in characters
   */
  CharSequence readLine(ByteBuffer buffer);


  static LineReader forCharset(Charset charset) {
    if (isLatin1Compatible(charset)) {
      return new NonDecodingLineReader();
    } else {
      return new DecodingLineReader(charset);
    }
  }



  static boolean isLatin1Compatible(Charset charset) {
    return US_ASCII.equals(charset) || ISO_8859_1.equals(charset);
  }

}
