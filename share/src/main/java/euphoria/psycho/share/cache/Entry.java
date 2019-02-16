package euphoria.psycho.share.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class Entry {
    public static final String[] ID_PROJECTION = { "_id" };

    public static interface Columns {
        public static final String ID = "_id";
    }

    // The primary key of the entry.
    @Column("_id")
    public long id = 0;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Table {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Column {
        String value();

        boolean indexed() default false;

        boolean fullText() default false;

        String defaultValue() default "";

        boolean unique() default false;
    }

    public void clear() {
        id = 0;
    }
}
