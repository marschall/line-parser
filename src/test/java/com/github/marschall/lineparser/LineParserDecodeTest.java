package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.junit.Test;

public class LineParserDecodeTest {

  @Test
  public void decodeReuseBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbccc".getBytes(US_ASCII));
    DecodingLineReader reader = new DecodingLineReader(US_ASCII, 3);
    CharBuffer charBuffer = reader.getOut();

    byteBuffer.position(0).limit(3);
    ByteBuffer aBuffer = byteBuffer.slice();

    byteBuffer.position(3).limit(6);
    ByteBuffer bBuffer = byteBuffer.slice();

    byteBuffer.position(6).limit(9);
    ByteBuffer cBuffer = byteBuffer.slice();

    assertSame(charBuffer, reader.readLine(aBuffer));
    assertSame(charBuffer, reader.readLine(bBuffer));
    assertSame(charBuffer, reader.readLine(cBuffer));
  }

  @Test
  public void decodeNewBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbbcccc".getBytes(US_ASCII));
    DecodingLineReader reader = new DecodingLineReader(US_ASCII, 3);
    CharBuffer charBuffer = reader.getOut();

    byteBuffer.position(0).limit(3);
    ByteBuffer aBuffer = byteBuffer.slice();

    byteBuffer.position(3).limit(7);
    ByteBuffer bBuffer = byteBuffer.slice();

    byteBuffer.position(7).limit(11);
    ByteBuffer cBuffer = byteBuffer.slice();

    assertSame(charBuffer, reader.readLine(aBuffer));
    CharBuffer bResult = (CharBuffer) reader.readLine(bBuffer);
    assertNotSame(charBuffer, bResult);
    assertSame(bResult, reader.readLine(cBuffer));
  }

}
