package com.github.marschall.lineparser;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertEquals;
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

    assertSame(charBuffer, reader.readLine(byteBuffer, 0, 3));
    assertEquals("aaa", reader.readLine(byteBuffer, 0, 3).toString());

    assertSame(charBuffer, reader.readLine(byteBuffer, 3, 3));
    assertEquals("bbb", reader.readLine(byteBuffer, 3, 3).toString());

    assertSame(charBuffer, reader.readLine(byteBuffer, 6, 3));
    assertEquals("ccc", reader.readLine(byteBuffer, 6, 3).toString());
  }

  @Test
  public void decodeNewBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.wrap("aaabbbbcccc".getBytes(US_ASCII));
    DecodingLineReader reader = new DecodingLineReader(US_ASCII, 3);
    CharBuffer charBuffer = reader.getOut();

    assertSame(charBuffer, reader.readLine(byteBuffer, 0, 3));
    assertEquals("aaa", reader.readLine(byteBuffer, 0, 3).toString());

    CharBuffer bResult = (CharBuffer) reader.readLine(byteBuffer, 3, 4);
    assertNotSame(charBuffer, bResult);
    assertEquals("bbbb", reader.readLine(byteBuffer, 3, 4).toString());

    assertSame(bResult, reader.readLine(byteBuffer, 7, 4));
    assertEquals("cccc", reader.readLine(byteBuffer, 7, 4).toString());
  }

}
