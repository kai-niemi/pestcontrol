package io.cockroachdb.pc.util;

import java.time.Duration;
import java.util.Locale;

//@Deprecated(forRemoval = true) // Replaced by DurationFormatterUtils
public abstract class TimeUtils {
    private TimeUtils() {
    }

    public static String durationToDisplayString(Duration duration) {
        //        return DurationFormatterUtils.print(getRemainingDuration(), DurationFormat.Style.COMPOSITE);
        return millisecondsToDisplayString(duration.toMillis());
    }

    public static String millisecondsToDisplayString(long timeMillis) {
        double seconds = (timeMillis / 1000.0) % 60;
        int minutes = (int) ((timeMillis / 60000) % 60);
        int hours = (int) ((timeMillis / 3600000));

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format("%dh", hours));
        }
        if (hours > 0 || minutes > 0) {
            sb.append(String.format("%dm", minutes));
        }
        if (hours == 0 && seconds > 0) {
            sb.append(String.format(Locale.US, "%.1fs", seconds));
        }
        return sb.toString();
    }
}
