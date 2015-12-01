package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import org.junit.Test;

public class LineParserDecodeTest {

  @Test
  public void decodeReuseBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbccc".getBytes(US_ASCII));
    CharBuffer charBuffer = CharBuffer.allocate(3);

    byteBuffer.position(0).limit(3);
    ByteBuffer aBuffer = byteBuffer.slice();

    byteBuffer.position(3).limit(6);
    ByteBuffer bBuffer = byteBuffer.slice();

    byteBuffer.position(6).limit(9);
    ByteBuffer cBuffer = byteBuffer.slice();

    CharsetDecoder decoder = US_ASCII.newDecoder();
    assertSame(charBuffer, LineParser.decode(aBuffer, charBuffer, decoder));
    assertSame(charBuffer, LineParser.decode(bBuffer, charBuffer, decoder));
    assertSame(charBuffer, LineParser.decode(cBuffer, charBuffer, decoder));
  }

  @Test
  public void decodeNewBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbbcccc".getBytes(US_ASCII));
    CharBuffer charBuffer = CharBuffer.allocate(3);

    byteBuffer.position(0).limit(3);
    ByteBuffer aBuffer = byteBuffer.slice();

    byteBuffer.position(3).limit(7);
    ByteBuffer bBuffer = byteBuffer.slice();

    byteBuffer.position(7).limit(11);
    ByteBuffer cBuffer = byteBuffer.slice();

    CharsetDecoder decoder = US_ASCII.newDecoder();
    assertSame(charBuffer, LineParser.decode(aBuffer, charBuffer, decoder));
    CharBuffer bResult = LineParser.decode(bBuffer, charBuffer, decoder);
    assertNotSame(charBuffer, bResult);
    assertSame(bResult, LineParser.decode(cBuffer, bResult, decoder));
  }

}
