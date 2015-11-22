package com.github.marschall.lineparser;

public final class Line {

  private final long offset;
  private final int length;
  private final CharSequence line;

  Line(long offset, int length, CharSequence line) {
    this.offset = offset;
    this.length = length;
    this.line = line;
  }

  public long getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public CharSequence getContent() {
    return line;
  }

}
