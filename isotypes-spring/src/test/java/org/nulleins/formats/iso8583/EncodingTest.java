package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.Message;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test message transformation using EBCDIC character set encoding for text fields
 * @author phillipsr
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EncodingTest {
  private static final String EBCDIC_CHARSET = "IBM1047";
  private static final String ExpectMessage =
      "ISO0150000770200F238000108A180000000004000000000135432818929192"
          + "00101000000000120012121212006666661212001212041029937278626262"
          + "ATM-10101       DUB87                                   12353863579271840003C10";

  @Resource(name = "ebcdicTest")
  private MessageFactory factory;

  @Test
  public void codecTest()
      throws IOException {
    assertThat(Charset.isSupported(EBCDIC_CHARSET), is(true));
    Charset ebcdicCodec = Charset.forName(EBCDIC_CHARSET);
    assertThat(ebcdicCodec, notNullValue());

    byte[] ebcdta = "Hello World".getBytes(EBCDIC_CHARSET);
    byte[] expect = new byte[]{(byte) 0xc8, (byte) 0x85, (byte) 0x93, (byte) 0x93, (byte) 0x96,
        0x40, (byte) 0xe6, (byte) 0x96, (byte) 0x99, (byte) 0x93, (byte) 0x84};

    assertThat(ebcdta, is(expect));
  }

  @Test
  public void testCreateMessageEbcdic() throws ParseException, IOException {
    assertThat(factory.getCharset().toString(), is("IBM1047"));
    Message message = getTestMessage();

    assertThat(message.validate(), empty());

    byte[] messageData = factory.getMessageData(message);
    assertThat(new String(messageData, EBCDIC_CHARSET), is(ExpectMessage));

    Message readback = factory.parse(messageData);
    assertThat(new String(factory.getMessageData(readback), EBCDIC_CHARSET), is(ExpectMessage));
  }

  private Message getTestMessage()
      throws ParseException {
    Date testDate = (new SimpleDateFormat("ddMMyyyy:HHmmss")).parse("12122012:121200");
    Message message = factory.create(MTI.create(0x0200));
    message.setFieldValue("cardNumber", 5432818929192L);
    message.setFieldValue("processingCode", 1010);
    message.setFieldValue("amount", new BigInteger("1200"));
    message.setFieldValue("transDateTime", testDate);
    message.setFieldValue("stan", 666666);
    message.setFieldValue("transTimeLocal", testDate);
    message.setFieldValue("transDateLocal", testDate);
    message.setFieldValue("acquierID", 1029);
    message.setFieldValue("extReference", 937278626262L);
    message.setFieldValue("cardTermId", "ATM-10101");
    message.setFieldValue("cardTermName", "DUB87");
    message.setFieldValue("msisdn", 353863579271L);
    message.setFieldValue("currencyCode", 840);
    message.setFieldValue("originalData", BigInteger.TEN);
    return message;
  }
}
