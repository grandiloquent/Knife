package euphoria.psycho.common;

import java.util.List;

public class CollectionUtils {


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
