package xyz.tbvns.ao3m;

import lombok.SneakyThrows;

public class Utils {
    @SneakyThrows
    public static void sleep(int delay) {
        Thread.sleep(delay);
    }

    public static String simplifyNumber(long number) {
        if (number < 1000) {
            // If the number is less than 1000, return it as is
            return String.valueOf(number);
        } else if (number < 10_000) {
            // If the number is between 1000 and 9999, format it with one decimal place (e.g., 1.9k)
            double simplified = number / 1000.0; // Use 1000.0 to ensure floating-point division
            return String.format("%.1fk", simplified).replace(".0k", "k"); // Remove .0 for whole numbers
        } else if (number < 1_000_000) {
            // If the number is 10,000 or above, round to the nearest whole number (e.g., 12500 -> 13k)
            double simplified = number / 1000.0; // Use 1000.0 to ensure floating-point division
            return String.format("%.0fk", (double) Math.round(simplified)); // Round to the nearest whole number
        } else if (number < 1_000_000_000) {
            // If the number is between 1,000,000 and 999,999,999, format it in millions (M)
            double simplified = number / 1_000_000.0; // Use 1_000_000.0 to ensure floating-point division
            return String.format("%.1fM", simplified).replace(".0M", "M"); // Remove .0 for whole numbers
        } else {
            // If the number is 1 billion or more, format it in billions (B)
            double simplified = number / 1_000_000_000.0; // Use 1_000_000_000.0 to ensure floating-point division
            return String.format("%.1fB", simplified).replace(".0B", "B"); // Remove .0 for whole numbers
        }
    }
}
