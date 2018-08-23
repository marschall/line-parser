package com.github.marschall.lineparser;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

public class MemoryAccessTest {

  @Test
  @Ignore
  public void address() throws IOException, ReflectiveOperationException {
    Path tempFile = Files.createTempFile("MemoryAccessTest", null);
    Files.write(tempFile, new byte[] {1, 2, 3});

    try (FileInputStream stream = new FileInputStream(tempFile.toFile());
            FileChannel channel = stream.getChannel()) {
      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, 3);
      //      MethodHandle methodHandle = MethodHandles.publicLookup()
      //        .findVirtual(buffer.getClass(), "address", MethodType.methodType(long.class));
      //      Object address;
      //      try {
      //        address = methodHandle.invoke(buffer);
      //      } catch (RuntimeException | Error e) {
      //        throw e;
      //      } catch (Throwable e) {
      //        throw new UndeclaredThrowableException(e);
      //      }
      //      assertNotNull(address);
      Method method = buffer.getClass().getMethod("address");
      Object address;
      try {
        address = method.invoke(buffer);
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable e) {
        throw new UndeclaredThrowableException(e);
      }
      assertNotNull(address);

    }
  }

  @Test
  public void directAddress() throws IOException, ReflectiveOperationException {
    Path tempFile = Files.createTempFile("MemoryAccessTest", null);
    Files.write(tempFile, new byte[] {1, 2, 3});

    try (FileInputStream stream = new FileInputStream(tempFile.toFile());
            FileChannel channel = stream.getChannel()) {
      MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, 3);
      sun.nio.ch.DirectBuffer direct = (DirectBuffer) buffer;
      long address = direct.address();
      System.out.println(address);


      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field singleoneInstanceField = unsafeClass.getDeclaredField("theUnsafe");
      if (!singleoneInstanceField.isAccessible()) {
        singleoneInstanceField.setAccessible(true);
      }

      sun.misc.Unsafe unsafe = (Unsafe) singleoneInstanceField.get(null);
      System.out.println(unsafe.getByte(address + 0));
      System.out.println(unsafe.getByte(address + 1));
      System.out.println(unsafe.getByte(address + 2));

    }
  }

}
