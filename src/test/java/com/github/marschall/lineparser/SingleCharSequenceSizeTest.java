package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.vm.VM;
import org.openjdk.jol.vm.VirtualMachine;

public class SingleCharSequenceSizeTest {

  private VirtualMachine vm;

  @BeforeEach
  public void setUp() {
    this.vm = VM.current();
  }

  @Test
  public void size() {
    CharSequence sequence = new SingleCharSequence('a');
    assertEquals(16L, this.vm.sizeOf(sequence));

    sequence = new CharArrayCharSequence(new char[] {'a'});
    assertEquals(24L, this.vm.sizeOf(sequence));
  }

  @Test
  public void testToString() {
    CharSequence sequence = new SingleCharSequence('a');
    assertEquals("a", sequence.toString());
  }
  
  @Test
  public void length() {
    CharSequence sequence = new SingleCharSequence('a');
    assertEquals(1, sequence.length());
  }

}
