package com.github.marschall.lineparser;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.github.marschall.lineparser.LineParser.FileInfo;

/**
 * Utility class to unmap a {@link MappedByteBuffer}.
 */
final class Unmapper {

  private static final MethodHandle UNSAFE_INVOKE_CLEANER;
  private static final MethodHandle DIRECT_BYTE_BUFFER_CLEANER;
  private static final MethodHandle CLEANER_CLEAN;

  static {
    Lookup lookup = MethodHandles.publicLookup();
    if (isJava9OrLater()) {
      // Java 9 branch
      // Unsafe.theUnsafe.invokeCleaner(byteBuffer)
      try {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field singleoneInstanceField = unsafeClass.getDeclaredField("theUnsafe");
        if (!singleoneInstanceField.isAccessible()) {
          singleoneInstanceField.setAccessible(true);
        }
        Object unsafe = singleoneInstanceField.get(null);

        Method invokeCleaner = unsafeClass.getDeclaredMethod("invokeCleaner", ByteBuffer.class);
        UNSAFE_INVOKE_CLEANER = lookup.unreflect(invokeCleaner).bindTo(unsafe);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("could not get invokeCleaner method handle", e);
      }
      DIRECT_BYTE_BUFFER_CLEANER = null;
      CLEANER_CLEAN = null;
    } else {
      // Java 8 branch
      // sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
      // cleaner.clean();
      try {
        Class<?> bufferInterface = Class.forName("java.nio.DirectByteBuffer");
        Method cleanerMethod = bufferInterface.getDeclaredMethod("cleaner");
        if (!cleanerMethod.isAccessible()) {
          cleanerMethod.setAccessible(true);
        }
        Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
        Method cleanMethod = cleanerClass.getDeclaredMethod("clean"); // should be accessible
        DIRECT_BYTE_BUFFER_CLEANER = lookup.unreflect(cleanerMethod);
        CLEANER_CLEAN = lookup.unreflect(cleanMethod);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("could not get cleaner clean method handle", e);
      }
      UNSAFE_INVOKE_CLEANER = null;
    }
  }

  private static boolean isJava9OrLater() {
    try {
      Class.forName("java.lang.Runtime$Version");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  static void unmap(MappedByteBuffer buffer, FileInfo fileInfo) throws IOException {
    if (UNSAFE_INVOKE_CLEANER != null) {
      // Java 9
      try {
        UNSAFE_INVOKE_CLEANER.invokeExact((ByteBuffer) buffer);
      } catch (RuntimeException e) {
        throw e;
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        throw new UnmapFailedException(fileInfo.path.toString(), "could not unmap", e);
      }
    } else {
      // Java 8
      try {
        Object cleaner = DIRECT_BYTE_BUFFER_CLEANER.invoke(buffer);
        CLEANER_CLEAN.invoke(cleaner);
      } catch (RuntimeException e) {
        throw e;
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        throw new UnmapFailedException(fileInfo.path.toString(), "could not unmap", e);
      }
    }
  }

}
