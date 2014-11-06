package org.nulleins.formats.iso8583.types;

import org.junit.Test;
import org.nulleins.formats.iso8583.types.Bitmap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
public class TestBitmap {
  @Test
  public void testBitmap() {
    Bitmap target = new Bitmap();

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
		 * 
		 * Expect:
		 * 01000010 00010000 00000000 00010001 00000010 11000000 01001000 00000100
		 * 42       10       00       11       02       C0       48       04
		 * 
		 * 00100000 00010010 00000011 01000000 10001000 00000000 00001000 01000010
		 */

    target.setField(2);
    target.setField(7);
    target.setField(12);
    target.setField(28);
    target.setField(32);
    target.setField(39);
    target.setField(41);
    target.setField(42);
    target.setField(50);
    target.setField(53);
    target.setField(62);

    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(7), is(true));
    assertThat(target.isFieldPresent(12), is(true));
    assertThat(target.isFieldPresent(28), is(true));
    assertThat(target.isFieldPresent(32), is(true));
    assertThat(target.isFieldPresent(39), is(true));
    assertThat(target.isFieldPresent(41), is(true));
    assertThat(target.isFieldPresent(42), is(true));
    assertThat(target.isFieldPresent(50), is(true));
    assertThat(target.isFieldPresent(53), is(true));
    assertThat(target.isFieldPresent(62), is(true));

    String hexBitmap = target.asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap, is("4210001102C04804"));

    byte[] binaryBitmap = target.asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap[0], is((byte) 0x42));
    assertThat(binaryBitmap[1], is((byte) 0x10));
    assertThat(binaryBitmap[2], is((byte) 0x00));
    assertThat(binaryBitmap[3], is((byte) 0x11));
    assertThat(binaryBitmap[4], is((byte) 0x02));
    assertThat(binaryBitmap[5], is((byte) 0xc0));
    assertThat(binaryBitmap[6], is((byte) 0x48));
    assertThat(binaryBitmap[7], is((byte) 0x04));
  }

  @Test
  public void testSecondaryBitmap() {
    Bitmap target = new Bitmap();

    target.setField(2);
    target.setField(66);

    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(66), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.PRIMARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.TERTIARY), is(false));

    String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    // should be "1100000..." as first bit specifies secondary bitmap present
    assertThat(hexBitmap1, is("C000000000000000"));

    byte[] binaryBitmap1 = target.asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap1[0], is((byte) 0xC0));

    String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    assertThat(hexBitmap2, is("4000000000000000"));

    byte[] binaryBitmap2 = target.asBinary(Bitmap.Id.SECONDARY);
    assertThat(binaryBitmap2[0], is((byte) 0x40));
  }


  @Test
  public void testTertiaryBitmap() {
    Bitmap target = new Bitmap();

    target.setField(2);
    target.setField(140);

    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(140), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.PRIMARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.TERTIARY), is(true));

    String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    // should be "1100000..." as first bit specifies secondary bitmap present
    assertThat(hexBitmap1, is("C000000000000000"));

    byte[] binaryBitmap1 = target.asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap1[0], is((byte) 0xC0));

    String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0010000000000000"));

    byte[] binaryBitmap3 = target.asBinary(Bitmap.Id.TERTIARY);
    assertThat(binaryBitmap3[1], is((byte) 0x10));
  }

  @Test
  public void testTerciaryBitmap() {
    Bitmap target = new Bitmap();

    target.setField(190);
    assertThat(target.isBitmapPresent(Bitmap.Id.PRIMARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.TERTIARY), is(true));

    String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    // should be "1000000..." as first bit specifies secondary bitmap present
    assertThat(hexBitmap2, is("8000000000000000"));

    String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0000000000000004"));

    byte[] binaryBitmap3 = target.asBinary(Bitmap.Id.TERTIARY);
    assertThat(binaryBitmap3[7], is((byte) 0x04));
  }

  private static final String HEX1 = "E440000000000008";
  private static final String HEX2 = "0000000000000040";

  @Test
  public void testBitmapParseHex1() {
    // 01100100 01000000 00000000 00000000 00000000 00000000 00000000 000010000
    Bitmap target = Bitmap.parse("6440000000000008");

    assertThat(target.isFieldPresent(1), is(false));
    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(3), is(true));
    assertThat(target.isFieldPresent(4), is(false));
    assertThat(target.isFieldPresent(5), is(false));
    assertThat(target.isFieldPresent(6), is(true));
    assertThat(target.isFieldPresent(7), is(false));
    assertThat(target.isFieldPresent(8), is(false));
    assertThat(target.isFieldPresent(9), is(false));
    assertThat(target.isFieldPresent(10), is(true));

    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(false));

    String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap1, is("6440000000000008"));

    String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    assertThat(hexBitmap2, is("0000000000000000"));

    String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0000000000000000"));

  }

  @Test
  public void testBitmapParseHex2() {
    Bitmap target = Bitmap.parse(HEX1 + HEX2);
    assertThat(target.isFieldPresent(1), is(true));
    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(3), is(true));
    assertThat(target.isFieldPresent(6), is(true));

    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));

    String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap1, is("E440000000000008"));

    String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    assertThat(hexBitmap2, is("0000000000000040"));

    String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0000000000000000"));

    byte[] binaryBitmap3 = target.asBinary(Bitmap.Id.TERTIARY);
    assertThat(binaryBitmap3[7], is((byte) 0x00));
  }

  private static final byte[] BIN
      = new byte[]{0x01, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08};

  @Test
  public void testBitmapParseBinary1() {
    Bitmap target = new Bitmap(BIN);
    byte[] binaryBitmap = target.asBinary(Bitmap.Id.PRIMARY);

    assertThat(binaryBitmap[0], is((byte) 0x01));
    assertThat(binaryBitmap[1], is((byte) 0x40));
    assertThat(binaryBitmap[2], is((byte) 0x00));
    assertThat(binaryBitmap[3], is((byte) 0x00));
    assertThat(binaryBitmap[4], is((byte) 0x00));
    assertThat(binaryBitmap[5], is((byte) 0x00));
    assertThat(binaryBitmap[6], is((byte) 0x00));
    assertThat(binaryBitmap[7], is((byte) 0x08));
  }

  // 10000010 01000000 00000000 00000000 00000000 00000000 00000000 000010000
  private static final byte[] BIN1
      = new byte[]{(byte) 0x82, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08};
  // 00001000 00000000 00000000 00000000 00000000 00000000 01000000 000000000
  private static final byte[] BIN2
      = new byte[]{0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00};

  @Test
  public void testBitmapParseBinary2() {
    Bitmap target = new Bitmap(concatData(BIN1, BIN2));

    byte[] primaryBitmap = target.asBinary(Bitmap.Id.PRIMARY);
    byte[] secondaryBitmap = target.asBinary(Bitmap.Id.SECONDARY);

    assertThat(primaryBitmap[0], is((byte) 0x82));
    assertThat(primaryBitmap[1], is((byte) 0x40));
    assertThat(primaryBitmap[2], is((byte) 0x00));
    assertThat(primaryBitmap[3], is((byte) 0x00));
    assertThat(primaryBitmap[4], is((byte) 0x00));
    assertThat(primaryBitmap[5], is((byte) 0x00));
    assertThat(primaryBitmap[6], is((byte) 0x00));
    assertThat(primaryBitmap[7], is((byte) 0x08));

    assertThat(secondaryBitmap[0], is((byte) 0x08));
    assertThat(secondaryBitmap[1], is((byte) 0x00));
    assertThat(secondaryBitmap[2], is((byte) 0x00));
    assertThat(secondaryBitmap[3], is((byte) 0x00));
    assertThat(secondaryBitmap[4], is((byte) 0x00));
    assertThat(secondaryBitmap[5], is((byte) 0x00));
    assertThat(secondaryBitmap[6], is((byte) 0x40));
    assertThat(secondaryBitmap[7], is((byte) 0x00));

  }

  /**
   * @param data1
   * @param data2
   * @return
   */
  private byte[] concatData(byte[] data1, byte[] data2) {
    byte[] result = new byte[data1.length + data2.length];
    int index = 0;
    for (byte b : data1) {
      result[index++] = b;
    }
    for (byte b : data2) {
      result[index++] = b;
    }
    return result;
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testBitmapSetFieldMissing() {
    Bitmap target = Bitmap.parse("4210001102C04804");
    target.setField(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testBitmapHexNull() {
    Bitmap.parse(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testBitmapHexBadLength() {
    Bitmap.parse("4210001102C0484");
  }

  @Test(expected = IllegalArgumentException.class)
  public void
  testBitmapNonHex() {
    Bitmap.parse("4210001102G04804");
  }

  @Test//(expected=IllegalArgumentException.class)
  public void
  testBitmapTertiary() {
    Bitmap.parse("4210001102C048044210001102C048044210001102C04804");
  }

}
