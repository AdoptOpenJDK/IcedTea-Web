package net.sourceforge.jnlp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation marks a test as a known failure (as opposed to a
 * regression). A test that is a known failure will not hold of a release,
 * nor should developers hold off a fix if they run the unit tests and a
 * test marked as a known failure fails.
 * </p><p>
 * This annotation is meant for adding tests for bugs before the fix is
 * implemented.
 * </p>
 */

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface KnownToFail {
    
}
