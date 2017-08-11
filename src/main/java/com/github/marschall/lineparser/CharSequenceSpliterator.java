package com.github.marschall.lineparser;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;

/**
 * An int spliterator on the characters of a CharSequence.
 *
 */
final class CharSequenceSpliterator implements OfInt {

  static final int CHARACTERISTICS = Spliterator.SUBSIZED
          | Spliterator.SIZED
          | Spliterator.IMMUTABLE
          | Spliterator.NONNULL
          | Spliterator.ORDERED;

  private final CharSequence sequence;
  private int position;

  CharSequenceSpliterator(CharSequence buffer) {
    Objects.requireNonNull(buffer);
    this.sequence = buffer;
    this.position = 0;
  }

  @Override
  public long estimateSize() {
    return this.remaining();
  }

  int remaining() {
    return this.sequence.length() - this.position;
  }

  @Override
  public int characteristics() {
    return CHARACTERISTICS;
  }

  @Override
  public OfInt trySplit() {
    int remaining = this.remaining();
    if (remaining <= 1) {
      // too small to split
      return null;
    }
    int half = remaining / 2;

    // we could avoid having to create a new CharSequence by adding
    // another instance variable, however this is only used for
    // parallel streams
    OfInt result = new CharSequenceSpliterator(this.sequence.subSequence(this.position, this.position + half));
    this.position += half;
    return result;
  }

  @Override
  public boolean tryAdvance(IntConsumer action) {
    if (this.remaining() >= 0) {
      action.accept(this.sequence.charAt(this.position));
      this.position += 1;
      return true;
    }
    return false;
  }

  @Override
  public void forEachRemaining(IntConsumer action) {
    int length = this.sequence.length();
    for (int i = this.position; i < length; i++) {
      action.accept(this.sequence.charAt(i));
    }
    this.position = length;
  }

}
