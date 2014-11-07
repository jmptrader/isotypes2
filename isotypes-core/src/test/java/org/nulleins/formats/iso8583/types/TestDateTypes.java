package org.nulleins.formats.iso8583.types;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.nulleins.formats.iso8583.formatters.DateFormatter;
import org.nulleins.formats.iso8583.formatters.TimeFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


/**
 * @author phillipsr
 */
public class TestDateTypes {
  private static final TypeFormatter<DateTime> dateFormatter = new DateFormatter(CharEncoder.ASCII);
  private static final TypeFormatter<LocalTime> timeFormatter = new TimeFormatter(CharEncoder.ASCII);

  @Test
  public void testParseDate()
      throws ParseException {
    byte[] testData = "XXXX0304054133ZXY".getBytes();
    ParsePosition pos = new ParsePosition(4);

    byte[] data = FieldParser.getBytes(testData, pos, 10);
    DateTime value = dateFormatter.parse(FieldType.DATE, Dimension.parse("FIXED(10)"), 10, data);

    assertThat(value.toString(), is("2000-03-04T05:41:33.000Z"));
    assertThat(pos.getIndex(), is(14));
  }

  @Test(expected = ParseException.class)
  public void testParseDateBad()
      throws ParseException {
    byte[] testData = "XXXX1234567ZXY".getBytes();
    ParsePosition pos = new ParsePosition(4);

    try {
      byte[] data = FieldParser.getBytes(testData, pos, 10);
      dateFormatter.parse(FieldType.DATE, Dimension.parse("FIXED(10)"), 10, data);
    } catch (ParseException e) {
      assertThat(e.getMessage(), startsWith("Cannot parse date field value"));
      assertThat(pos.getIndex(), is(14));

      throw e;
    }
  }

  @Test
  public void testParseTime()
      throws ParseException {
    byte[] testData = "XXXX0304054133ZXY".getBytes();
    ParsePosition pos = new ParsePosition(4);

    byte[] data = FieldParser.getBytes(testData, pos, 6);
    LocalTime value = timeFormatter.parse(FieldType.TIME, Dimension.parse("FIXED(6)"), 6, data);

    assertThat(value.toString(), is("03:04:05.000"));
    assertThat(pos.getIndex(), is(10));
  }

  @Test(expected = ParseException.class)
  public void testParseBadTime()
      throws ParseException {
    byte[] testData = "XXXXO3O4O54133ZXY".getBytes();
    ParsePosition pos = new ParsePosition(4);

    byte[] data = FieldParser.getBytes(testData, pos, 6);
    timeFormatter.parse(FieldType.TIME, Dimension.parse("FIXED(6)"), 6, data);
  }

  @Test
  public void testFormatDate4()
      throws ParseException {
    DateFormat df = new SimpleDateFormat("dd-MM-yy");
    byte[] data = dateFormatter.format(FieldType.DATE, df.parse("01-12-2001"), Dimension.parse("FIXED(4)"));

    assertThat(data.length, is(4));
    assertThat(new String(data), is("1201"));
  }

  @Test
  public void testFormatDate10()
      throws ParseException {
    DateFormat df = new SimpleDateFormat("dd-MM-yy/HH:mm:ss");
    byte[] data = dateFormatter.format(FieldType.DATE, df.parse("01-12-2001/09:45:33"), Dimension.parse("FIXED(10)"));
    assertThat(data.length, is(10));
    assertThat(new String(data), is("1201094533"));
  }

  @Test
  public void testFormatExpDate()
      throws ParseException {
    DateFormat df = new SimpleDateFormat("dd-MM-yy");
    byte[] data = dateFormatter.format(FieldType.EXDATE, df.parse("01-12-2010"), Dimension.parse("FIXED(4)"));

    assertThat(data.length, is(4));
    assertThat(new String(data), is("1012"));
  }

  @Test
  public void testFormatTime()
      throws ParseException {
    DateFormat df = new SimpleDateFormat("HH:mm:ss");
    byte[] data = timeFormatter.format(FieldType.TIME, df.parse("19:26:07"), Dimension.parse("FIXED(6)"));

    assertThat(data.length, is(6));
    assertThat(new String(data), is("192607"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatNullDate() {
    dateFormatter.format(FieldType.DATE, null, Dimension.parse("FIXED(4)"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatBadDate() {
    dateFormatter.format(FieldType.DATE, "12121212", Dimension.parse("FIXED(4)"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatNullTime() {
    timeFormatter.format(FieldType.TIME, null, Dimension.parse("FIXED(6)"));
  }

  @Test //(expected=IllegalArgumentException.class) - no longer fails as value constrained to dim specified
  public void testFormatBadTime() {
    timeFormatter.format(FieldType.TIME, "12121212", Dimension.parse("FIXED(6)"));
  }

}
