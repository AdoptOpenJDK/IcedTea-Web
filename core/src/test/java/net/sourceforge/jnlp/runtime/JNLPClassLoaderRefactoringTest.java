package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * ...
 */
public class JNLPClassLoaderRefactoringTest {
    private static final JARDesc partOne_1 = jarDesc("file_1", "partOne");
    private static final JARDesc partOne_2 = jarDesc("file_2", "partOne");
    private static final JARDesc partOne_3 = jarDesc("file_3", "partOne");

    private static final JARDesc partTwo_1 = jarDesc("file_4", "partTwo");
    private static final JARDesc partTwo_2 = jarDesc("file_5", "partTwo");
    private static final JARDesc partTwo_3 = jarDesc("file_6", "partTwo");

    private static final JARDesc partThree_1 = jarDesc("file_7", "partThree");
    private static final JARDesc partThree_2 = jarDesc("file_8", "partThree");


    @Test
    public void shouldNotChangeJarsIfIsEmpty() {
        // given
        final List<JARDesc> jars = new ArrayList<>();

        // when
        final List<JARDesc> original = new ArrayList<>(jars);
        JNLPClassLoader.fillInPartJarsTestable(jars, null);

        // then
        assertThat(jars, is(original));
    }

    @Test
    public void shouldNotChangeJarsIfContainsOnlyJarsWithoutParts() {
        // given
        final List<JARDesc> jars = new ArrayList<>();
        jars.add(jarDesc(null, null));

        // when
        final List<JARDesc> original = new ArrayList<>(jars);
        JNLPClassLoader.fillInPartJarsTestable(jars, null);

        // then
        assertThat(jars, is(original));
    }

    @Test
    public void shouldNotChangeJarsIfNoJarWithSamePartIsAvailable() {
        // given
        final List<JARDesc> jars = asList(partOne_1);
        final List<JARDesc> available = asList(partTwo_1, partTwo_2);

        // when
        final List<JARDesc> original = new ArrayList<>(jars);
        JNLPClassLoader.fillInPartJarsTestable(jars, available);

        // then
        assertThat(jars, is(original));
    }

    @Test
    public void shouldNotChangeJarsIfNoOtherJarWithSamePartIsAvailable() {
        // given
        final List<JARDesc> jars = asList(partOne_1);
        final List<JARDesc> available = asList(partOne_1, partTwo_1, partTwo_2);

        // when
        final List<JARDesc> original = new ArrayList<>(jars);
        JNLPClassLoader.fillInPartJarsTestable(jars, available);

        // then
        assertThat(jars, is(original));
    }

    @Test
    public void shouldAddJarsWithSamePartToJars() {
        // given
        final List<JARDesc> jars = asList(partOne_1);
        final List<JARDesc> available = asList(partOne_2, partOne_1, partOne_3, partTwo_1, partTwo_2);

        // when
        JNLPClassLoader.fillInPartJarsTestable(jars, available);

        // then
        assertThat(jars, is(asList(partOne_1, partOne_2, partOne_3)));
    }

    @Test
    public void shouldAddJarsWithSamePartForAllJarsInInput() {
        // given
        final List<JARDesc> jars = asList(partOne_1, partTwo_1);
        final List<JARDesc> available = asList(partOne_2, partOne_1, partOne_3, partTwo_1, partTwo_2, partTwo_3, partThree_1, partThree_2);

        // when
        JNLPClassLoader.fillInPartJarsTestable(jars, available);

        // then
        assertThat(jars, is(asList(partOne_1, partTwo_1, partOne_2, partOne_3, partTwo_2, partTwo_3)));
    }

    private static JARDesc jarDesc(String fileName, String partName) {
        try {
            final URL url = new URL("http://localhost/" + fileName);
            return new JARDesc(url, null, partName, false, false, false, false) {
                @Override
                public String toString() {
                    return fileName;
                }
            };
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<JARDesc> asList(JARDesc... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }
}
