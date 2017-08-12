package com.github.marschall.lineparser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CharArrayCharSequenceTest {

  private CharSequence sequence;
  private CharSequence prefixSubSequence;
  private CharSequence subSequence;

  @Before
  public void setUp() {
    this.sequence = new CharArrayCharSequence(new char[] {'a', 'b', 'c'});
    this.prefixSubSequence = this.sequence.subSequence(0, 2);
    this.subSequence = this.sequence.subSequence(1, 3);
  }

  @Test
  public void testToString() {
    assertEquals("abc", this.sequence.toString());
    assertEquals("abc", this.sequence.toString());
  }

  @Test
  public void charAt() {
    assertEquals('a', this.sequence.charAt(0));
    assertEquals('b', this.sequence.charAt(1));
    assertEquals('c', this.sequence.charAt(2));
  }

  @Test
  public void length() {
    assertEquals(3, this.sequence.length());
  }

  @Test
  public void testPrefixToString() {
    assertEquals("ab", this.prefixSubSequence.toString());
    assertEquals("ab", this.prefixSubSequence.toString());
  }

  @Test
  public void prefixCharAt() {
    assertEquals('a', this.prefixSubSequence.charAt(0));
    assertEquals('b', this.prefixSubSequence.charAt(1));
  }

  @Test
  public void prefixLength() {
    assertEquals(2, this.prefixSubSequence.length());
  }

  @Test
  public void prefixChars() {
    assertArrayEquals("ab".chars().toArray(), this.prefixSubSequence.chars().toArray());
  }

  @Test
  public void prefixCodePoints() {
    assertArrayEquals("ab".codePoints().toArray(), this.prefixSubSequence.codePoints().toArray());
  }

  @Test
  public void testSubSequenceToString() {
    assertEquals("bc", this.subSequence.toString());
    assertEquals("bc", this.subSequence.toString());
  }

  @Test
  public void subSequenceCharAt() {
    assertEquals('b', this.subSequence.charAt(0));
    assertEquals('c', this.subSequence.charAt(1));
  }

  @Test
  public void subSequenceLength() {
    assertEquals(2, this.subSequence.length());
  }

  @Test
  public void subSequenceChars() {
    assertArrayEquals("bc".chars().toArray(), this.subSequence.chars().toArray());
  }

  @Test
  public void subSequencePoints() {
    assertArrayEquals("bc".codePoints().toArray(), this.subSequence.codePoints().toArray());
  }

  @Test
  public void subSequenceSubSequence() {
    assertEquals("b", this.prefixSubSequence.subSequence(1, 2).toString());
    assertEquals("b", this.subSequence.subSequence(0, 1).toString());
  }

  @Test
  public void chars() {
    assertArrayEquals("abc".chars().toArray(), this.sequence.chars().toArray());
  }

  @Test
  public void codePoints() {
    assertArrayEquals("abc".codePoints().toArray(), this.sequence.codePoints().toArray());
  }

  @Test
  public void emptySubSequence() {
    assertEquals("", this.sequence.subSequence(0, 0).toString());
    assertEquals("", this.subSequence.subSequence(0, 0).toString());
    assertEquals("", this.prefixSubSequence.subSequence(0, 0).toString());

    assertEquals("", this.sequence.subSequence(1, 1).toString());
    assertEquals("", this.subSequence.subSequence(1, 1).toString());
    assertEquals("", this.prefixSubSequence.subSequence(1, 1).toString());
  }

}
