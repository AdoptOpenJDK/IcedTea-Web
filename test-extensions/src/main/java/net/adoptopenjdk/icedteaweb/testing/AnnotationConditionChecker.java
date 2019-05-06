package net.adoptopenjdk.icedteaweb.testing;

import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.adoptopenjdk.icedteaweb.testing.annotations.KnownToFail;
import net.adoptopenjdk.icedteaweb.testing.annotations.WindowsIssue;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static org.junit.Assume.assumeFalse;

/**
 * JUnit4 rule which evaluates the following annotations and only runs a test if appropriate
 * <ul>
 *   <li>{@link KnownToFail}</li>
 *   <li>{@link WindowsIssue}</li>
 * </ul>
 */
public class AnnotationConditionChecker implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(description);
                base.evaluate();
            }
        };
    }

    public void before(Description description) {
        final Class<?> testClass = description.getTestClass();
        final String methodName = description.getMethodName();

        if (testClass == null || isBlank(methodName)) {
            return;
        }

        try {
            Method test = testClass.getMethod(methodName);
            assumeFalse(hasAnnotation(test, KnownToFail.class));
            assumeFalse(hasAnnotation(test, WindowsIssue.class) && OsUtil.isWindows());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean hasAnnotation(Method method, Class<? extends Annotation> annotationType) {
        return method.getAnnotation(annotationType) != null || hasAnnotation(method.getDeclaringClass(), annotationType);
    }

    private boolean hasAnnotation(Class<?> declaringClass, Class<? extends Annotation> annotationType) {
        final Class<?> superclass = declaringClass.getSuperclass();
        if (superclass != null) {
            return hasAnnotation(superclass, annotationType);
        }
        return declaringClass.getAnnotation(annotationType) != null;
    }
}
