package euphoria.psycho.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated method or class should never be inlined.
 *
 * The annotated method (or methods on the annotated class) are guaranteed not to be inlined by
 * Proguard. Other optimizations may still apply.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface DoNotInline {}
