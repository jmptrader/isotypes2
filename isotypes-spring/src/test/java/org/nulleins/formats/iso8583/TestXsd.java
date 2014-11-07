package org.nulleins.formats.iso8583;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.model.CardNumber;
import org.nulleins.formats.iso8583.model.PaymentRequestBean;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestXsd {
  @Resource
  private MessageFactory messages;

  @Test
  public void
  testListXsdSchema() {
    assertThat(messages, notNullValue());

    MessageTemplate msg0200 = messages.getTemplate(MTI.create(0x0200));
    assertThat(msg0200, notNullValue());
    assertThat(msg0200.getFields().size(), is(14));

    MessageTemplate msg0400 = messages.getTemplate(MTI.create(0x0400));
    assertThat(msg0400, notNullValue());
    assertThat(msg0400.getFields().size(), is(12));
  }

  @Test
  public void testCreateMessage()
      throws ParseException, IOException {
    // create the test message and set field values:
    Message message = messages.create(MTI.create(0x0200));
    message.setFieldValue("cardNumber", new CardNumber(5432818929192L));
    message.setFieldValue("processingCode", 1010);
    message.setFieldValue("amount", new BigInteger("1200"));
    message.setFieldValue("transDateTime",
        (new SimpleDateFormat("MMddHHmmss")).parse("1212121212"));
    message.setFieldValue("stan", 666666);
    message.setFieldValue("transTimeLocal",
        (new SimpleDateFormat("HHmmss")).parse("121212"));
    message.setFieldValue("transDateLocal",
        (new SimpleDateFormat("MMdd")).parse("1212"));
    message.setFieldValue("acquierID", 1029);
    message.setFieldValue("extReference", 937278626262L);
    message.setFieldValue("cardTermId", "ATM-10101");
    message.setFieldValue("cardTermName", "DUB87");
    message.setFieldValue("msisdn", 353863579271L);
    message.setFieldValue("currencyCode", 840);
    message.setFieldValue("originalData", BigInteger.TEN);

    // check the message has been correctly created:
    assertThat(message.validate(), empty());

    // convert the message into its wire format:
    byte[] messageData = messages.getMessageData(message);

    // parse the message back into a new message instance
    Message readback = messages.parse(messageData);
    // ensure the message context is the same as the original (allowing for type promotions):
    DateTime dateTime = DateTimeFormat.forPattern("MMddHHmmss").parseDateTime("1212121212");
    DateTime date = DateTimeFormat.forPattern("MMdd").parseDateTime("1212");
    LocalTime localTime = DateTimeFormat.forPattern("HHmmss").parseLocalTime("121212");
    assertThat((String)readback.getFieldValue("cardNumber"), is("5432*******92"));
    assertThat((BigInteger)readback.getFieldValue("processingCode"), is(BigInteger.valueOf(1010)));
    assertThat((BigInteger)readback.getFieldValue("amount"), is(BigInteger.valueOf(1200)));
    assertThat((DateTime)readback.getFieldValue("transDateTime"), is(dateTime));
    assertThat((BigInteger)readback.getFieldValue("stan"), is(BigInteger.valueOf(666666)));
    assertThat((LocalTime)readback.getFieldValue("transTimeLocal"), is(localTime));
    assertThat((DateTime)readback.getFieldValue("transDateLocal"), is(date));
    assertThat((BigInteger)readback.getFieldValue("acquierID"), is(BigInteger.valueOf(1029)));
    assertThat((BigInteger)readback.getFieldValue("extReference"), is(BigInteger.valueOf(937278626262L)));
    assertThat((String)readback.getFieldValue("cardTermId"), is("ATM-10101"));
    assertThat((String)readback.getFieldValue("cardTermName"), is("DUB87"));
    assertThat((BigInteger)readback.getFieldValue("msisdn"), is(BigInteger.valueOf(353863579271L)));
    assertThat((BigInteger)readback.getFieldValue("currencyCode"), is(BigInteger.valueOf(840)));
    assertThat((BigInteger)readback.getFieldValue("originalData"), is(BigInteger.TEN));

    // check the describer by comparing the desc of the original message
    // with that of the read-back message; as the data-types can change in translation,
    // only the first 21 chars are compared (the field definitions)
    //
    final Set<String> original = new HashSet<String>();
    for (final String line : message.describe()) {
      original.add(line.substring(0, 21));
    }
    final Set<String> derived = new HashSet<String>();
    for (final String line : readback.describe()) {
      derived.add(line.substring(0, 21));
    }
    assertThat(original, is(derived));
  }

  @Test(expected = MessageException.class)
  public void testCreateBadMessage() {
    // create the test message and set field values:
    Message message = messages.create(MTI.create(0x0200));
    List<String> errors = message.validate();
    if (!errors.isEmpty()) {
      throw new MessageException(errors);
    }
  }

  private static final String ExpectedBeanMessage =
      "ISO0150000770200F238000108A180000000004000000000" +
          "135432*******920010100000000000120923000000618172" +
          "1011121117041029937278626262ATM-10101       DUB87" +
          "                                   12353863579271840004C999";

  @Test
  public void testCreateMessageFromBean()
      throws ParseException {
    // this bean represent the business data in the transaction
    final PaymentRequestBean bean = new PaymentRequestBean();
    bean.setCardNumber(new CardNumber(5432818929192L));
    bean.setAmount(new BigInteger("12"));
    bean.setAcquierID(1029);
    bean.setExtReference(937278626262L);
    bean.setCardTermId("ATM-10101");
    bean.setCardTermName("DUB87");
    bean.setMsisdn(353863579271L);
    bean.setCurrencyCode(840);
    bean.setOriginalData(999);

    // this map contains the technical/protocol fields
    final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    final DateFormat tf = new SimpleDateFormat("HH:mm:ss");
    Map<Integer, Object> params = new HashMap<Integer, Object>() {{
      put(3, 1010);
      put(7, df.parse("23-09-2015"));
      put(11, 618172);
      put(12, tf.parse("10:11:12"));
      put(13, df.parse("17-11-2016"));
    }};

    Message message = messages.createFromBean(MTI.create("0200"), bean);
    message.addFields(params);

    assertThat(message.validate(), empty());

    String messageText = new String(messages.getMessageData(message));

    assertThat(messageText, is(ExpectedBeanMessage));

    Message response = messages.duplicate(MTI.create("0400"), message);

    response.setFieldValue("currencyCode2", 885);
    response.setFieldValue("currencyCode3", 350);

    assertThat(response.isValid(), is(true));
  }

  @Test
  public void testParseBeanMessageAsMap()
      throws ParseException, IOException {
    Message message = messages.parse(ExpectedBeanMessage.getBytes());

    assertThat(message.validate(), empty());
    Map<Integer, Object> result = message.getFields();

    assertThat(message.validate(), empty());
    assertThat(message.getMTI(), is(MTI.create("0200")));

    assertThat((String)result.get(2), is("5432*******92"));
    assertThat((BigInteger)result.get(3), is(new BigInteger("1010")));
    assertThat((BigInteger) result.get(4), is(new BigInteger("12")));
    assertThat(result.get(7).toString(), is("2000-09-23T00:00:00.000+01:00"));
    assertThat((BigInteger)result.get(11), is(new BigInteger("618172")));
    assertThat(result.get(12).toString(), is("10:11:12.000"));
    assertThat(result.get(13).toString(), is("2000-11-17T00:00:00.000Z"));
    assertThat((BigInteger)result.get(32), is(new BigInteger("1029")));
    assertThat((BigInteger)result.get(37), is(new BigInteger("937278626262")));
    assertThat((String)result.get(41), is("ATM-10101"));
    assertThat((String)result.get(43), is("DUB87"));
    assertThat((BigInteger)result.get(48), is(new BigInteger("353863579271")));
    assertThat((BigInteger)result.get(49), is(new BigInteger("840")));
    assertThat((BigInteger)result.get(90), is(new BigInteger("999")));
  }

  @Test
  public void testBitmap() {
    /*
		 * 4210001102C04804	Fields 2, 7, 12, 28, 32, 39, 41, 42, 50, 53, 62
		 * Explanation of Bitmap (8 BYTE Primary Bitmap = 64 Bit) field 4210001102C04804
		 * BYTE1 : 0100 0010 = 42x (fields 2 and 7 are present)
		 * BYTE2 : 0001 0000 = 10x (field 12 is present)
		 * BYTE3 : 0000 0000 = 00x (no fields present)
		 * BYTE4 : 0001 0001 = 11x (fields 28 and 32 are present)
		 * BYTE5 : 0000 0010 = 02x (field 39 is present)
		 * BYTE6 : 1100 0000 = C0x (fields 41 and 42 are present)
		 * BYTE7 : 0100 1000 = 48x (fields 50 and 53 are present)
		 * BYTE8 : 0000 0100 = 04x (field 62 is present)
		 */
    MessageTemplate template = messages.getTemplate(MTI.create("0400"));

    byte[] binaryBitmap = template.getBitmap().asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap[0], is((byte) 0x42));
    assertThat(binaryBitmap[1], is((byte) 0x38));
    assertThat(binaryBitmap[2], is((byte) 0x00));
    assertThat(binaryBitmap[3], is((byte) 0x01));
    assertThat(binaryBitmap[4], is((byte) 0x08));
    assertThat(binaryBitmap[5], is((byte) 0xa1));
    assertThat(binaryBitmap[6], is((byte) 0x08));
    assertThat(binaryBitmap[7], is((byte) 0x04));

    String hexBitmap = template.getBitmap().asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap, is("4238000108A10804"));
  }

  private static final String Payment_Request =
      "ISO01500007702007238000108A18000165264**********02305700000000032000"
          + "121022021393716600021312111181800601368034522937166CIB08520263     CIB-57357"
          + "HOSPITAL     CAIRO          EG01120167124377818";

  @Test
  public void testParseMessage()
      throws ParseException, IOException {
    Map<Integer, Object> params = messages.parse(Payment_Request.getBytes()).getFields();
    assertThat((String)params.get(2), is("5264**********02"));
    assertThat((BigInteger)params.get(3), is(BigInteger.valueOf(305700)));
    assertThat((BigInteger)params.get(4), is(BigInteger.valueOf(32000)));
    assertThat(params.get(7).toString(), is("2000-12-10T22:02:13.000Z"));
    assertThat((BigInteger)params.get(11), is(BigInteger.valueOf(937166)));
    assertThat(params.get(12).toString(), is("00:02:13.000"));
    assertThat(params.get(13).toString(), is("2000-12-11T00:00:00.000Z"));
    assertThat((BigInteger)params.get(32), is(BigInteger.valueOf(81800601368L)));
    assertThat((BigInteger)params.get(37), is(BigInteger.valueOf(34522937166L)));
    assertThat((String)params.get(41), is("CIB08520263"));
    assertThat((String)params.get(43), is("CIB-57357HOSPITAL     CAIRO          EG0"));
    assertThat((BigInteger)params.get(48), is(BigInteger.valueOf(20167124377L)));
    assertThat((BigInteger)params.get(49), is(BigInteger.valueOf(818)));
  }

  private static final String ExpectMessage =
      "ISO0150000770200F238000108A180000000004000000000135432818929192"
          + "00101000000000120012121212006666661212001212041029937278626262"
          + "ATM-10101       DUB87                                   12353863579271840003C10";

  @Test
  public void
  testCreateMessageAPI()
      throws IOException, ParseException {
    Message request = messages.create(MTI.create(0x0200));
    assertThat(request.isValid(), is(false));

    Date testDate = (new SimpleDateFormat("ddMMyyyy:HHmmss")).parse("12122012:121200");
    request.setFieldValue(2, 5432818929192L);
    request.setFieldValue(3, 1010);
    request.setFieldValue(4, new BigInteger("1200"));
    request.setFieldValue(7, testDate);
    request.setFieldValue(11, 666666);
    request.setFieldValue(12, testDate);
    request.setFieldValue(13, testDate);
    request.setFieldValue(32, 1029);
    request.setFieldValue(37, 937278626262L);
    request.setFieldValue(41, "ATM-10101");
    request.setFieldValue(43, "DUB87");
    request.setFieldValue(48, 353863579271L);
    request.setFieldValue(49, 840);
    request.setFieldValue(90, BigInteger.TEN);

    assertThat(request.isValid(), is(true));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    messages.writeToStream(request, baos);
    Message message = messages.parse(new ByteArrayInputStream(baos.toByteArray()));
    assertThat(new String(messages.getMessageData(message)), is(ExpectMessage));
  }

}
