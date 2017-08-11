package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Reads a line into a {@link CharSequence}.
 */
interface LineReader {

  /**
   * Reads a line.
   *
   * <p>This is a view in a mutable buffer that is reused. Any content that
   * is used after the callback has to be copied with
   * {@link CharSequence#toString()} ideally calling
   * {@link CharSequence#subSequence(int, int)} first.</p>
   *
   * @param buffer contains the line
   * @param start of the line in {@code buffer}
   * @param length the length of the line in bytes
   * @return the line in characters
   */
  CharSequence readLine(ByteBuffer buffer, int start, int length) throws IOException;


  /**
   * Creates a new instance for the given character set.
   *
   * @param charset the character set
   * @return the line reader instance for the given character set
   */
  static LineReader forCharset(Charset charset) {
    if (isLatin1Compatible(charset)) {
      return new NonDecodingLineReader();
    } else if (charset.equals(UTF_16BE)) {
      return new Utf16BeLineReader();
    } else if (charset.equals(UTF_16LE)) {
      return new Utf16LeLineReader();
    } else {
      return new DecodingLineReader(charset);
    }
  }

  static boolean isLatin1Compatible(Charset charset) {
    return US_ASCII.equals(charset) || ISO_8859_1.equals(charset);
  }

}
