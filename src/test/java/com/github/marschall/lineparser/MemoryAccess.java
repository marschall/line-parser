package com.github.marschall.lineparser;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Abstraction for accessing a memory location.
 */
interface MemoryAccess extends AutoCloseable {

  byte get(int index);

  @Override
  void close() throws IOException;

  ByteBuffer getByteBuffer();

  int capacity();

}
