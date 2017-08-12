package com.github.marschall.lineparser;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openjdk.jol.vm.VM;
import org.openjdk.jol.vm.VirtualMachine;

public class SingleCharSequenceSizeTest {

  private VirtualMachine vm;

  @Before
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

}
