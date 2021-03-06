package euphoria.psycho.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that signals that a class should be kept in the main dex file.
 *
 * This generally means it's used by child processes (renderer/utility), which can't load secondary
 * dexes on K and below.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface MainDex {}
