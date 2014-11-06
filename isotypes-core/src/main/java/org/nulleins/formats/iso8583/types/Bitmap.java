package org.nulleins.formats.iso8583.types;

import java.util.Arrays;
import java.util.BitSet;
import java.util.regex.Pattern;


/**
 * Implementation of the ISO8583 bitmap type, with facilities to create, parse and format a
 * message's bitmap in a number of formats
 * @author phillipsr
 */
public class Bitmap {
  private static final Pattern HexMatcher = Pattern.compile("[0-9A-Fa-f]+");

  public enum Id {
    PRIMARY(0), SECONDARY(1), TERTIARY(2);
    private final int index;

    private Id(final int index) {
      this.index = index;
    }
  }

  /**
   * bitsets hold the three bitmaps available to a message, and
   * are indexed by the <code>Id.index</code> from the enum above
   */
  private final BitSet[] bitmaps = new BitSet[]{new BitSet(), new BitSet(), new BitSet()};

  public Bitmap() {
  }

  /**
   * Create a bitmap, instantiating it from the supplied Hex string, which
   * may represent a primary, primary + secondary or primary + secondary + tertiary
   * bitmap
   * @param value hexadecimal representation of a bitmap
   * @throws IllegalArgumentException if the bitmap is not a 16, 32 or 48 byte long
   *                                  hexadecimal string
   */
  public static Bitmap parse(final String value) {
    if (value == null) {
      throw new IllegalArgumentException("Hex bitmap must not be null");
    }
    final String hexBitmap = value.trim();
    final int hexlength = hexBitmap.length();
    if (hexlength != 16 && hexlength != 32 && hexlength != 48) {
      throw new IllegalArgumentException(
          "Hex bitmap must be 16, 32 or 48 characters in size (got: " + hexlength + " chars)");
    }
    if (!HexMatcher.matcher(hexBitmap).matches()) {
      throw new IllegalArgumentException("Hex bitmap must contain only hexadecimal digits (0-9A-F)");
    }
    final Bitmap result = new Bitmap();
    result.bitmaps[Id.PRIMARY.index] = BitsetUtil.hex2Bitset(hexBitmap.substring(0, 16));
    if (hexlength > 16) {
      result.bitmaps[Id.SECONDARY.index] = BitsetUtil.hex2Bitset(hexBitmap.substring(16, 32));
    }
    if (hexlength > 32) {
      result.bitmaps[Id.TERTIARY.index] = BitsetUtil.hex2Bitset(hexBitmap.substring(32));
    }
    return result;
  }

  /**
   * @param binBitmap
   */
  public Bitmap(final byte... binBitmap) {
    if (binBitmap == null || binBitmap.length < 8) {
      throw new IllegalArgumentException("Bin bitmap must be >= 8 bytes in size");
    }
    final int length = binBitmap.length;
    if (length > 16) {
      bitmaps[Id.TERTIARY.index] = BitsetUtil.bin2Bitset(Arrays.copyOfRange(binBitmap, 16, 24));
    }
    if (length > 8) {
      bitmaps[Id.SECONDARY.index] = BitsetUtil.bin2Bitset(Arrays.copyOfRange(binBitmap, 8, 16));
    }
    bitmaps[Id.PRIMARY.index] = BitsetUtil.bin2Bitset(Arrays.copyOfRange(binBitmap, 0, 9));
  }

  public String asHex(final Id map) {
    return BitsetUtil.bitset2Hex(bitmaps[map.index], 16);
  }

  public byte[] asBinary(final Id map) {
    return BitsetUtil.bitset2bin(bitmaps[map.index], 8);
  }

  public void clear() {
    bitmaps[Id.PRIMARY.index].clear();
    bitmaps[Id.SECONDARY.index].clear();
    bitmaps[Id.TERTIARY.index].clear();
  }

  /**
   * @param fieldNb
   * @throws IllegalArgumentException if the fieldNb is not {2..64} or {66..128} or {130..192}
   */
  public void setField(final int fieldNb) {
    if (fieldNb < 2 || fieldNb > 192 || fieldNb == 65 || fieldNb == 129) {
      throw new IllegalArgumentException(
          "fieldNb can only be: {2..64} or {66..128} or {130..192} (fieldNb=" + fieldNb + ")");
    }
    final Id bitmapId = getBitmapIdForField(fieldNb);
    final BitSet bitmap = bitmaps[bitmapId.index];
    bitmap.set(getFieldPosInBitmap(fieldNb, bitmapId));
    setBitmapPresent(bitmapId);
  }

  /**
   * @param fieldNb
   * @return
   */
  private Id getBitmapIdForField(final int fieldNb) {
    if (fieldNb <= 64) {
      return Id.PRIMARY;
    }
    if (fieldNb > 64 && fieldNb <= 128) {
      return Id.SECONDARY;
    }
    return Id.TERTIARY;
  }

  private int getFieldPosInBitmap(final int fieldNb, final Id bitmapId) {
    switch (bitmapId) {
      case PRIMARY:
        return fieldNb - 1;
      case SECONDARY:
        return fieldNb - 65;
      case TERTIARY:
        return fieldNb - 129;
      default:
        return 0; // unreachable as Id is enum
    }
  }

  /**
   * @param fieldNb
   * @return
   */
  public boolean isFieldPresent(final int fieldNb) {
    final Id bitmapId = getBitmapIdForField(fieldNb);
    final BitSet bitmap = bitmaps[bitmapId.index];
    final int pos = getFieldPosInBitmap(fieldNb, bitmapId);
    return bitmap.get(pos);
  }

  public boolean isBitmapPresent(final Id map) {
    if (map == Id.PRIMARY) {
      return true;
    }
    final BitSet bitmap = bitmaps[map.index - 1];
    return bitmap.get(0);
  }

  private void setBitmapPresent(final Id bitmapId) {
    if (bitmapId == Id.PRIMARY) {
      return; // always present
    }
    bitmaps[bitmapId.index - 1].set(0);

    // if tertiary bitmap is present, then secondary is implicitly present
    if (bitmapId == Id.TERTIARY) {
      setBitmapPresent(Id.SECONDARY);
    }
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    result.append(BitsetUtil.bitset2Hex(bitmaps[Id.PRIMARY.index], 16));
    if (isBitmapPresent(Id.SECONDARY)) {
      result.append(BitsetUtil.bitset2Hex(bitmaps[Id.SECONDARY.index], 16));
      if (isBitmapPresent(Id.TERTIARY)) {
        result.append(BitsetUtil.bitset2Hex(bitmaps[Id.TERTIARY.index], 16));
      }
    }
    return result.toString();
  }

}
