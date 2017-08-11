package com.github.marschall.lineparser;

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
  public void subsequenceSubsequence() {
    assertEquals("b", this.prefixSubSequence.subSequence(1, 2).toString());
    assertEquals("b", this.subSequence.subSequence(0, 1).toString());
  }

}
