

package euphoria.common;

import java.util.List;

public class Collections {

    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }
}


