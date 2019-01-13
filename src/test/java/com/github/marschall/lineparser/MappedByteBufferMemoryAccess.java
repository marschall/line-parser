package com.github.marschall.lineparser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.github.marschall.lineparser.LineParser.FileInfo;

/**
 * Implementation of {@link MemoryAccess} using {@link MappedByteBuffer}.
 */
final class MappedByteBufferMemoryAccess implements MemoryAccess {

  private final MappedByteBuffer buffer;
  private final FileInfo fileInfo;

  MappedByteBufferMemoryAccess(MappedByteBuffer buffer, FileInfo fileInfo) {
    this.buffer = buffer;
    this.fileInfo = fileInfo;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    return this.buffer;
  }

  @Override
  public int capacity() {
    return this.buffer.capacity();
  }

  @Override
  public byte get(int index) {
    return this.buffer.get(index);
  }

  @Override
  public void close() throws IOException {
    Unmapper.unmap(this.buffer, this.fileInfo);
  }

}
