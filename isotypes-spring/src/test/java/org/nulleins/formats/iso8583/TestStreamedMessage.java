package org.nulleins.formats.iso8583;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.Message;
import org.nulleins.formats.iso8583.MessageException;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * @author phillipsr
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestStreamedMessage {
  private static final DateTimeFormatter DATE4Formatter = DateTimeFormat.forPattern("MMdd");
  private static final DateTimeFormatter TIME6Formatter = DateTimeFormat.forPattern("HHmmss");
  private static final DateTimeFormatter DATE10Formatter = DateTimeFormat.forPattern("MMddHHmmss");

  @Resource
  private MessageFactory factory;

  private static final String Payment_Request =
      "ISO01500007702007238000108A18000165264391220494002305700000000032000"
          + "121022021393716600021312111181800601368034522937166CIB08520263     CIB-57357"
          + "HOSPITAL     CAIRO          EG01120167124377818";

  // MTI (0x0990) is valid but not defined:
  private static final String Unknown_Request =
      "ISO01500007709907238000108A18000165264391220494002305700000000032000"
          + "121022021393716600021312111181800601368034522937166CIB08520263     CIB-57357"
          + "HOSPITAL     CAIRO          EG01120167124377818";

  @Test
  public void testParseMessage()
      throws ParseException, IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(Payment_Request.getBytes());

    Message response = factory.parse(input);

    assertThat((BigInteger)response.getFieldValue(2), is(BigInteger.valueOf(5264391220494002L)));
    assertThat((BigInteger)response.getFieldValue(3), is(BigInteger.valueOf(305700)));
    assertThat((BigInteger)response.getFieldValue(4), is(BigInteger.valueOf(32000)));
    assertThat((DateTime)response.getFieldValue(7), is(DATE10Formatter.parseDateTime("1210220213")));
    assertThat((BigInteger)response.getFieldValue(11), is(BigInteger.valueOf(937166)));
    assertThat((LocalTime)response.getFieldValue(12), is(TIME6Formatter.parseLocalTime("000213")));
    assertThat((DateTime)response.getFieldValue(13), is(DATE4Formatter.parseDateTime("1211")));
    assertThat((BigInteger)response.getFieldValue(32), is(BigInteger.valueOf(81800601368L)));
    assertThat((BigInteger)response.getFieldValue(37), is(BigInteger.valueOf(34522937166L)));
    assertThat((String)response.getFieldValue(41), is("CIB08520263"));
    assertThat((String)response.getFieldValue(43), is("CIB-57357HOSPITAL     CAIRO          EG0"));
    assertThat((BigInteger)response.getFieldValue(48), is(BigInteger.valueOf(20167124377L)));
    assertThat((BigInteger)response.getFieldValue(49), is(BigInteger.valueOf(818)));
  }

  @Test(expected = MessageException.class)
  public void testParseUnknownMessage()
      throws ParseException, IOException {
    factory.parse(
        new ByteArrayInputStream(Unknown_Request.getBytes()));
  }

  @Test(expected = MessageException.class)
  public void testParseMissingHeader()
      throws ParseException, IOException {
    factory.parse(
        new ByteArrayInputStream(Payment_Request.substring(12).getBytes()));
  }

  @Test(expected = NullPointerException.class)
  public void testParseNullStream()
      throws ParseException, IOException {
    InputStream input = null;
    factory.parse(input);
  }


  @Test
  public void testCreateMessage()
      throws IOException {
    Map<Integer, Object> params = new HashMap<Integer, Object>() {{
      put(2, 5264391220494002L);
      put(3, 305700);
      put(4, new BigInteger("32000"));
      put(7, DATE10Formatter.parseDateTime("1210220213"));
      put(11, 937166);
      put(12, TIME6Formatter.parseLocalTime("000213"));
      put(13, DATE4Formatter.parseDateTime("1211"));
      put(32, 81800601368L);
      put(37, 34522937166L);
      put(41, "CIB08520263");
      put(43, "CIB-57357HOSPITAL     CAIRO          EG0");
      put(48, 20167124377L);
      put(49, 818);
    }};

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    factory.writeFromNumberMap(MTI.create("0200"), params, baos);
    assertThat(baos.toString(), is(Payment_Request));
  }

}
