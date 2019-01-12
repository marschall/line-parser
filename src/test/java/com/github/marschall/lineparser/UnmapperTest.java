package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UnmapperTest {

  @Test
  void getTheUnsafe() throws ReflectiveOperationException {
    assertNotNull(Unmapper.getTheUnsafe());
  }

}
