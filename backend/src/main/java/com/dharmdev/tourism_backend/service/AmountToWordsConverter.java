package com.dharmdev.tourism_backend.service;

import java.math.BigDecimal;

/** Converts a rupee amount to Indian-style words, e.g. 2596.00 -> "Two Thousand Five Hundred and Ninety Six Rupees only". */
public class AmountToWordsConverter {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };
    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(BigDecimal amount) {
        long rupees = amount.longValue();
        if (rupees == 0) return "Zero Rupees only";
        return convertIndianStyle(rupees).trim() + " Rupees only";
    }

    private static String convertIndianStyle(long num) {
        if (num == 0) return "";
        StringBuilder sb = new StringBuilder();

        long crore = num / 10000000; num %= 10000000;
        long lakh = num / 100000; num %= 100000;
        long thousand = num / 1000; num %= 1000;
        long hundred = num / 100; num %= 100;

        if (crore > 0) sb.append(convertTwoDigit(crore)).append(" Crore ");
        if (lakh > 0) sb.append(convertTwoDigit(lakh)).append(" Lakh ");
        if (thousand > 0) sb.append(convertTwoDigit(thousand)).append(" Thousand ");
        if (hundred > 0) sb.append(ONES[(int) hundred]).append(" Hundred ");
        if (num > 0) {
            if (sb.length() > 0) sb.append("and ");
            sb.append(convertTwoDigit(num));
        }
        return sb.toString();
    }

    private static String convertTwoDigit(long num) {
        if (num < 20) return ONES[(int) num];
        return TENS[(int) (num / 10)] + (num % 10 != 0 ? " " + ONES[(int) (num % 10)] : "");
    }
}