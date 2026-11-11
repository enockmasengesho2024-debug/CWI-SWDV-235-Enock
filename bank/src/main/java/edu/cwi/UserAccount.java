package edu.cwi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

public record UserAccount(
        String name,
        Double balance,
        Map<String, String> transactions) {

    public String formatBalance() {
        DecimalFormat formatter = new DecimalFormat(
                "$#,###.00",
                DecimalFormatSymbols.getInstance(Locale.US)
        );

        formatter.setMinimumIntegerDigits(1);

        return formatter.format(balance);
    }
}
