package io.github.alathra.alathraports.utility;

public class StringUtil {
    public static String doubleToPercent(double amount) {
        return String.format("%.1f%%", amount * 100);
    }
}
