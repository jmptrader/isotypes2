package org.nulleins.formats.iso8583.types;

import org.junit.Test;
import org.nulleins.formats.iso8583.MessageException;
import org.nulleins.formats.iso8583.formatters.AlphaFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
public class TestAlphaTypes {
  private static final TypeFormatter<String> formatter = new AlphaFormatter(CharEncoder.ASCII);

  @Test
  public void testParseAlpha()
      throws ParseException {
    byte[] testData = "123456Hello7ZXY".getBytes();
    ParsePosition pos = new ParsePosition(6);
    byte[] data = FieldParser.getBytes(testData, pos, 5);
    Object value = formatter.parse(FieldType.ALPHA, Dimension.parse("FIXED(5)"), 5, data);

    assertThat((String)value, is("Hello"));
    assertThat(pos.getIndex(), is(11));
  }


  @Test(expected = ParseException.class)
  public void testParseAlphaExhausted()
      throws ParseException {
    byte[] testData = "123456Hell".getBytes();
    ParsePosition pos = new ParsePosition(6);

    try {
      byte[] data = FieldParser.getBytes(testData, pos, 5);
      formatter.parse(FieldType.ALPHA, Dimension.parse("FIXED(5)"), 5, data);
    } catch (ParseException e) {
      assertThat(e.getMessage(), is("Data exhausted"));
      assertThat(pos.getErrorIndex(), is(6));
      throw e;
    }
  }

  @Test(expected = ParseException.class)
  public void testParseAlphaInvalid()
      throws ParseException {
    byte[] testData = "123456H31109188".getBytes();
    ParsePosition pos = new ParsePosition(6);

    try {
      byte[] data = FieldParser.getBytes(testData, pos, 5);
      formatter.parse(FieldType.ALPHA, Dimension.parse("FIXED(5)"), 5, data);
    } catch (ParseException e) {
      assertThat(e.getMessage().startsWith("Invalid data parsed"), is(true));
      throw e;
    }
  }

  @Test
  public void testFormatAlphaVar() {
    byte[] testData = "StringData".getBytes();
    Dimension dim = Dimension.parse("llvar(10)");

    byte[] result = formatter.format(FieldType.ALPHA, testData, dim);
    assertThat(Arrays.equals(testData, result), is(true));
  }

  @Test
  public void testFormatAlphaFix() {
    String testData = "StringData";
    Dimension dim = Dimension.parse("fixed(12)");

    String result = new String(formatter.format(FieldType.ALPHA, testData, dim));
    assertThat(result, is(testData + "  "));
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testFormatAlphaInvalid() {
    formatter.format(FieldType.ALPHA, "1234", Dimension.parse("fixed(12)"));
  }

  @Test(expected = MessageException.class)
  public void
  testFormatAlphaFixTooLong() {
    formatter.format(FieldType.ALPHA, "StringData", Dimension.parse("fixed(2)"));
  }

  @Test(expected = MessageException.class)
  public void
  testFormatAlphaVarTooLong() {
    formatter.format(FieldType.ALPHA, "TooLong".getBytes(), Dimension.parse("llvar(2)"));
  }


}
