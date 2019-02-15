package euphoria.psycho.share.util;

import java.util.Formatter;

public class DateUtils {

    public static String getSimpleTimestampAsString(long time) {
        final long hours = time / 3600000;
        time %= 3600000;
        final long mins = time / 60000;
        time %= 60000;
        final long sec = time / 1000;
        return String.format("%02d:%02d:%02d", hours, mins, sec);
    }

    public static String getStringForTime(StringBuilder builder, Formatter formatter, long timeMs) {

        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        builder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

}
