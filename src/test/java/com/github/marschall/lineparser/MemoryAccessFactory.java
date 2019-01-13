package com.github.marschall.lineparser;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.github.marschall.lineparser.LineParser.FileInfo;

final class MemoryAccessFactory {

  MemoryAccess map(FileInfo fileInfo, long mapStart, int mapSize) throws IOException {
    FileChannel channel = fileInfo.channel;
    MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, mapStart, mapSize);
    if (UnsafeMemoryAccess.isSupported()) {
      return new UnsafeMemoryAccess(buffer, fileInfo);
    } else {
      return new MappedByteBufferMemoryAccess(buffer, fileInfo);
    }
  }

}
