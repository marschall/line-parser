package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class MemoryAccessTest {

  @Test
  public void addressMethodLookup() throws IOException, ReflectiveOperationException {
    Path tempFile = Files.createTempFile("MemoryAccessTest", null);
    Files.write(tempFile, new byte[] {1, 2, 3});

    try (FileInputStream stream = new FileInputStream(tempFile.toFile());
            FileChannel channel = stream.getChannel()) {
      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, 3);

      assertThrows(IllegalAccessException.class, () -> {
        MethodHandles.publicLookup()
          .findVirtual(buffer.getClass(), "address", MethodType.methodType(long.class));
      });
    }
  }

  // does not compile on 11
//  @Test
//  public void directAddress() throws IOException, ReflectiveOperationException {
//    Path tempFile = Files.createTempFile("MemoryAccessTest", null);
//    Files.write(tempFile, new byte[] {1, 2, 3});
//
//    try (FileInputStream stream = new FileInputStream(tempFile.toFile());
//            FileChannel channel = stream.getChannel()) {
//      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, 3);
//      sun.nio.ch.DirectBuffer direct = (sun.nio.ch.DirectBuffer) buffer;
//      long address = direct.address();
//      System.out.println(address);
//
//      sun.misc.Unsafe unsafe = (sun.misc.Unsafe) Unmapper.getTheUnsafe();
//      System.out.println(unsafe.getByte(address + 0));
//      System.out.println(unsafe.getByte(address + 1));
//      System.out.println(unsafe.getByte(address + 2));
//
//    }
//  }

  @Test
  public void addressMethod() throws ReflectiveOperationException, IOException {
    Path tempFile = Files.createTempFile("MemoryAccessTest", null);
    Files.write(tempFile, new byte[] {1, 2, 3});
    try {
      try (FileInputStream stream = new FileInputStream(tempFile.toFile());
              FileChannel channel = stream.getChannel()) {
        MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, 3);

        Method addressMethod = buffer.getClass().getMethod("address");

        assertThrows(IllegalAccessException.class, () -> addressMethod.invoke(buffer));

      }
    } finally {
      Files.delete(tempFile);
    }
  }

  @Test
  public void addressField() throws ReflectiveOperationException, IOException {
    Field addressFeild = java.nio.Buffer.class.getDeclaredField("address");
    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) Unmapper.getTheUnsafe();
    long fieldOffset = unsafe.objectFieldOffset(addressFeild);


    Path tempFile = Files.createTempFile("MemoryAccessTest", null);
    Files.write(tempFile, new byte[] {1, 2, 3});
    try {
      try (FileInputStream stream = new FileInputStream(tempFile.toFile());
              FileChannel channel = stream.getChannel()) {
        MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, 3);

        long address = unsafe.getLong(buffer, fieldOffset);
        System.out.println(address);
        assertNotEquals(address, 0);

      }
    } finally {
      Files.delete(tempFile);
    }
  }

}
