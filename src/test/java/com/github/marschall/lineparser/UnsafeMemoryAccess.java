package com.github.marschall.lineparser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.github.marschall.lineparser.LineParser.FileInfo;

/**
 * Implementation of {@link MemoryAccess} using {@link sun.misc.Unsafe}.
 */
final class UnsafeMemoryAccess implements MemoryAccess {

  private static final sun.misc.Unsafe UNSAFE;

  private static long ADDRESS_FIELD_OFFSET;

  static {
    sun.misc.Unsafe unsafe;
    long addressFieldOffset;
    try {
      Field addressField = java.nio.Buffer.class.getDeclaredField("address");
      unsafe = (sun.misc.Unsafe) Unmapper.getTheUnsafe();
      addressFieldOffset = unsafe.objectFieldOffset(addressField);
    } catch (ReflectiveOperationException e) {
      unsafe = null;
      addressFieldOffset = -1L;
    }
    UNSAFE = unsafe;
    ADDRESS_FIELD_OFFSET = addressFieldOffset;
  }

  private final MappedByteBuffer buffer;
  private final FileInfo fileInfo;
  private final long baseAddress;

  UnsafeMemoryAccess(MappedByteBuffer buffer, FileInfo fileInfo) {
    this.buffer = buffer;
    this.fileInfo = fileInfo;
    this.baseAddress = UNSAFE.getLong(buffer, ADDRESS_FIELD_OFFSET);
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
    return UNSAFE.getByte(this.baseAddress + index);
  }

  @Override
  public void close() throws IOException {
    Unmapper.unmap(this.buffer, this.fileInfo);
  }

  static boolean isSupported() {
    return (UNSAFE != null) && (ADDRESS_FIELD_OFFSET >= 0L);
  }

}
