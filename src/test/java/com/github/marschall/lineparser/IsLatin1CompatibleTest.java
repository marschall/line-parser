package com.github.marschall.lineparser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class IsLatin1CompatibleTest {

  @ParameterizedTest
  @ValueSource(strings = {"ISO-8859-1", "iso-8859-1", "ascii", "us-ascii"})
  public void isLatin1Compatible(String charsetName) {
    Charset charset = Charset.forName(charsetName);
    assertTrue(LineReader.isLatin1Compatible(charset));
  }

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "utf8", "UTF-8"})
  public void notLatin1Compatible(String charsetName) {
    Charset charset = Charset.forName(charsetName);
    assertFalse(LineReader.isLatin1Compatible(charset));
  }

}
