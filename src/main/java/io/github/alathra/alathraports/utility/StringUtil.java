package io.github.alathra.alathraports.utility;

public class StringUtil {
    public static String doubleToPercent(double amount) {
        amount = Math.round(amount * 100);
        return (int) amount + "%";
    }
}
