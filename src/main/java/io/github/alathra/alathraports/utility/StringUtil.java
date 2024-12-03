package io.github.alathra.alathraports.utility;

public class StringUtil {

    // Ex: "Hello, World!" with ',' becomes "Hello"
    public static String trimStringWithDelimiter(String str, char delimiter) {
        int index = str.indexOf(delimiter);
        if (index != -1) {
            return str.substring(0, index);
        } else {
            return str; // Return the original string if the delimiter is not found
        }
    }

    public static String convertStringToUnderscoreUpperCase(String inputString) {
        // Replace all spaces with underscores and convert to uppercase
        return inputString.replaceAll("\\s+", "_").toUpperCase();
    }
}
