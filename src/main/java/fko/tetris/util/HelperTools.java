/**
 * The MIT License (MIT)
 *
 * "Tetris by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package fko.tetris.util;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * This class just provides some helper utilities and cannot be instanciated.
 */
public final class HelperTools {

    private final static Format digitFormat = new DecimalFormat("00");
    private final static Format milliFormat = new DecimalFormat("000");

    private HelperTools () {}

    /**
     * get a MByte String from a byte input
     * @param digit
     * @return String
     */
    public static String getDigit(long digit) {
        Locale.setDefault(new Locale("de", "DE"));
        NumberFormat f = NumberFormat.getInstance();
        if (f instanceof DecimalFormat) {
            f.setMaximumFractionDigits(1);
        }
        return f.format(digit);
    }

    /**
     * get a MByte String from a byte input
     * @param bytes
     * @return String
     */
    public static String getMBytes(long bytes) {
        double d = (Long.valueOf(bytes)).doubleValue() / (1024.0 * 1024.0);
        NumberFormat f = NumberFormat.getInstance();
        if (f instanceof DecimalFormat) {
            f.setMaximumFractionDigits(1);
        }
        return f.format(d);
    }

    /**
     * format a given time into 00:00:00
     * @param time
     * @param milliseconds
     * @return formatted string
     */
    public static String formatTime(long time, boolean milliseconds) {
        StringBuilder sb = new StringBuilder(digitFormat.format((time / 1000 / 60 / 60)));
        sb.append(':');
        sb.append(digitFormat.format((time / 1000 / 60) % 60));
        sb.append(':');
        sb.append(digitFormat.format((time / 1000) % 60));
        if (milliseconds) {
            sb.append('.');
            sb.append(milliFormat.format(time % 1000));
        }
        return sb.toString();
    }
}
