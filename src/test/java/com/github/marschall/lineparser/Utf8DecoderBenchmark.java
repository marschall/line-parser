package com.github.marschall.lineparser;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Utf8DecoderBenchmark {

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
            .include(".*Utf8DecoderBenchmark.*")
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(10)
            .build();
    new Runner(options).run();
  }

  private CharsetDecoder decoder;

  private final String shortAsciiString = "a";

  private String longAsciiString;

  private final String shortLatinString = "\u00E4";

  private String longLatinString;

  private ByteBuffer shortAsciiBuffer;

  private ByteBuffer longAsciiBuffer;

  private ByteBuffer shortLatinBuffer;

  private ByteBuffer longLatinBuffer;

  private char[] target;

  private CharBuffer outBuffer;

  @Setup
  public void setup() {
    this.decoder = StandardCharsets.UTF_8.newDecoder();
    this.longAsciiString = repeat(this.shortAsciiString, 100);
    this.longLatinString = repeat(this.shortLatinString, 100);

    this.shortAsciiBuffer = this.asNativeBuffer(this.shortAsciiString);
    this.longAsciiBuffer = this.asNativeBuffer(this.longAsciiString);
    this.shortLatinBuffer = this.asNativeBuffer(this.shortLatinString);
    this.longLatinBuffer = this.asNativeBuffer(this.longLatinString);

    this.target = new char[100];
    this.outBuffer = CharBuffer.wrap(this.target);
  }

  private ByteBuffer asNativeBuffer(String s) {
    // we need to make sure Buffer.hasArray() returns false so we get the same
    // code path has with MappedByteBuffer
    ByteBuffer heapBuffer = ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
    ByteBuffer nativeBuffer = ByteBuffer.allocate(heapBuffer.capacity());
    nativeBuffer.put(heapBuffer);
    nativeBuffer.flip();
    return nativeBuffer;
  }

  private static String repeat(String s, int times) {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < times; i++) {
      buffer.append(s);
    }
    return buffer.toString();
  }

  @Benchmark
  public CoderResult shortAsciiJdk() {
    return this.decoder.decode(this.shortAsciiBuffer, this.outBuffer, true);
  }

  @Benchmark
  public int shortAsciiLineParser() {
    return Utf8Decoder.decode(this.shortAsciiBuffer, this.target);
  }

  @Benchmark
  public CoderResult longAsciiJdk() {
    return this.decoder.decode(this.longAsciiBuffer, this.outBuffer, true);
  }

  @Benchmark
  public int longAsciiLineParser() {
    return Utf8Decoder.decode(this.longAsciiBuffer, this.target);
  }

  @Benchmark
  public CoderResult shortLatinJdk() {
    return this.decoder.decode(this.shortLatinBuffer, this.outBuffer, true);
  }

  @Benchmark
  public int shortLatinLineParser() {
    return Utf8Decoder.decode(this.shortLatinBuffer, this.target);
  }

  @Benchmark
  public CoderResult longLatinJdk() {
    return this.decoder.decode(this.longLatinBuffer, this.outBuffer, true);
  }

  @Benchmark
  public int longLatinLineParser() {
    return Utf8Decoder.decode(this.longLatinBuffer, this.target);
  }

}
