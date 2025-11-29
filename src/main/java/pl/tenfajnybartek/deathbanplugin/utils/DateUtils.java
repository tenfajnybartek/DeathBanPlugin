package pl.tenfajnybartek.deathbanplugin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.forLanguageTag("pl"));

    public static String formatDate(long millis) {
        return FORMAT.format(new Date(millis));
    }
}
