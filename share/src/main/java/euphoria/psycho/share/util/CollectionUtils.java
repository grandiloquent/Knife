package euphoria.psycho.share.util;

import java.util.HashSet;
import java.util.List;

public class CollectionUtils {


    public static <T> HashSet toHashSet(List<T> source) {
        return source == null ? new HashSet<T>() : new HashSet<T>(source);
    }

    public static <T> String toString(List<T> source) {
        StringBuilder sb = new StringBuilder();
        int length = source.size();
        for (int i = 0; i < length; i++) {
            sb.append(source.get(i));
            if (i + 1 < length) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
