package com.github.marschall.lineparser;

/**
 * A parsed line.
 */
public final class Line {

  private final long offset;
  private final int length;
  private final CharSequence line;

  Line(long offset, int length, CharSequence line) {
    this.offset = offset;
    this.length = length;
    this.line = line;
  }

  /**
   * The byte offset of the first character of this line into the parsed
   * file.
   *
   * @return the byte offset of the line
   */
  public long getOffset() {
    return this.offset;
  }

  /**
   * The length in bytes of this line.
   *
   * @return the length in bytes
   */
  public int getLength() {
    return this.length;
  }

  /**
   * The content of this line.
   *
   * <p>This is a view in a mutable buffer that is reused. Any content that
   * is used after the callback has to be copied with
   * {@link CharSequence#toString()} ideally calling
   * {@link CharSequence#subSequence(int, int)} first.</p>
   *
   * @return the content of this line
   */
  public CharSequence getContent() {
    return this.line;
  }

}
