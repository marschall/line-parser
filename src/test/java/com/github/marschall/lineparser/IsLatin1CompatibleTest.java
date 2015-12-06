package com.github.marschall.lineparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IsLatin1CompatibleTest {

  private final String charsetName;
  private final boolean compatible;

  public IsLatin1CompatibleTest(String charsetName, boolean compatible) {
    this.charsetName = charsetName;
    this.compatible = compatible;
  }

  @Parameters
  public static Iterable<Object[]> data() {
      return Arrays.asList(
              new Object[][] {
                { "utf-8", false },
                { "utf8", false },
                { "ISO-8859-1", true },
                { "iso-8859-1", true },
                { "ascii", true },
                { "us-ascii", true }
              });
  }

  @Test
  public void isLatin1Compatible() {
    Charset charset = Charset.forName(this.charsetName);
    if (this.compatible) {
      assertTrue(LineReader.isLatin1Compatible(charset));
    } else {
      assertFalse(LineReader.isLatin1Compatible(charset));
    }
  }

}
