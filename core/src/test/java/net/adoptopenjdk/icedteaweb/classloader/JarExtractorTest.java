package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Test that the right parts, packages and jars are extracted from JNLP files.
 */
public class JarExtractorTest {

    private static final String DEFAULT_NAME = null;
    private static final boolean LAZY = true;
    private static final boolean EAGER = false;
    private static final List<Matcher<JARDesc>> NO_JARS = emptyList();
    private static final List<Matcher<PackageDesc>> NO_PACKAGES = emptyList();

    private JNLPFileFactory jnlpFileFactory;

    @Before
    public final void setUp() {
        jnlpFileFactory = new JNLPFileFactory();
    }

    @Test
    public void jnlpWithNoJars() throws Exception {
        // given
        final JNLPFile jnlpFile = new JNLPFileFactory().create(getUrl("empty.jnlp"));

        // when
        final List<Part> parts = new JarExtractor(jnlpFile, jnlpFileFactory).getParts();

        // then
        assertThat(parts, containsInAnyOrder(
                part(DEFAULT_NAME, LAZY, NO_JARS, NO_PACKAGES),
                part(DEFAULT_NAME, EAGER, NO_JARS, NO_PACKAGES)
        ));
    }

    @Test
    public void jnlpWithOneEagerAndOneUnnamedLazyJar() throws Exception {
        // given
        final JNLPFile jnlpFile = new JNLPFileFactory().create(getUrl("eager-and-unnamedLazy.jnlp"));

        // when
        final List<Part> parts = new JarExtractor(jnlpFile, jnlpFileFactory).getParts();

        // then
        assertThat(parts, containsInAnyOrder(
                part(DEFAULT_NAME, LAZY, jars("lazy.jar"), NO_PACKAGES),
                part(DEFAULT_NAME, EAGER, jars("eager.jar"), NO_PACKAGES)
        ));
    }

    @Test
    public void jnlpWithOneEagerAndOneLazyNamedJar() throws Exception {
        // given
        final JNLPFile jnlpFile = new JNLPFileFactory().create(getUrl("eager-and-lazy.jnlp"));

        // when
        final List<Part> parts = new JarExtractor(jnlpFile, jnlpFileFactory).getParts();

        // then
        assertThat(parts, containsInAnyOrder(
                part(DEFAULT_NAME, LAZY, NO_JARS, NO_PACKAGES),
                part(DEFAULT_NAME, EAGER, jars("eager.jar"), NO_PACKAGES),
                part("lazy-package", LAZY, jars("lazy.jar"), packages("class.in.lazy.Package"))
        ));
    }


    //TODO: add the following test cases
    // - extension with 'ext-part' and no 'part' and no 'download' => should make ext-part eager
    // - extension with 'ext-part' and no 'part' 'download="lazy"' and package in extension => should make ext-part lazy
    // - extension with 'ext-part' and no 'part' and 'download="lazy"' and no package in extension => should make ext-part eager
    // - extension with 'ext-part' and 'part' and no 'download' => should combine the two parts and make it eager
    // - extension with 'ext-part' and 'part' and no 'download = "lazy"' => should combine the two parts and make it lazy
    // - extension with 'ext-part' and 'part' and no 'download = "lazy"' and neither part has a package => should combine the two parts and make it eager

    //TODO: add the following test cases
    // - lazy and eager jar in same part => part should be eager
    // - resource filtered by locale => jars should not be in result
    // - resource filtered by os => jars should not be in result
    // - resource filtered by arch => jars should not be in result
    // - resource in <java> tag with wrong version => jars should not be in result
    // - extension without part mapping and different parts => should be 2 parts with different name
    // - extension without part mapping and parts with same name => should be 2 parts with same name
    // - a crazy nested example of all of the above:
    //      - resource with locale filter
    //      - in this a java element
    //      - in this a resource with arch filter
    //      - in this an extension
    //      - in this a resource with os filter
    //      - and finally the jar

    private Matcher<Part> part(String name, boolean lazy, List<Matcher<JARDesc>> jars, List<Matcher<PackageDesc>> packages) {
        return new BaseMatcher<Part>() {
            @Override
            public boolean matches(Object actual) {
                if (actual instanceof Part) {
                    final Part part = (Part) actual;
                    return Objects.equals(part.getName(), name) &&
                            part.isLazy() == lazy &&
                            matchesInAnyOrder(part.getJars(), jars) &&
                            matchesInAnyOrder(part.getPackages(), packages);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Part{name=" + name + " lazy=" + lazy + " jars=")
                        .appendList("[", ", ", "]", jars)
                        .appendText(" packages=")
                        .appendList("[", ", ", "]", packages)
                        .appendText("}");
            }
        };
    }

    private <T> boolean matchesInAnyOrder(List<T> actual, List<Matcher<T>> matchers) {
        return matchers.size() == actual.size() &&
                matchers.stream().allMatch(matcher -> actual.stream().anyMatch(matcher::matches));
    }

    private List<Matcher<JARDesc>> jars(String... jarNames) {
        return Arrays.stream(jarNames)
                .map(jarName -> {
                    final URL url = getUrl(jarName);
                    return new BaseMatcher<JARDesc>() {
                        @Override
                        public boolean matches(Object actual) {
                            if (actual instanceof JARDesc) {
                                return ((JARDesc) actual).getLocation().equals(url);
                            }
                            return false;
                        }

                        @Override
                        public void describeTo(Description description) {
                            description.appendText(url.toString());
                        }
                    };
                })
                .collect(Collectors.toList());
    }

    private List<Matcher<PackageDesc>> packages(String... packageNames) {
        return Arrays.stream(packageNames)
                .map(packageName -> new BaseMatcher<PackageDesc>() {
                    @Override
                    public boolean matches(Object actual) {
                        if (actual instanceof PackageDesc) {
                            return ((PackageDesc) actual).getName().equals(packageName);
                        }
                        return false;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText(packageName);
                    }
                })
                .collect(Collectors.toList());
    }

    private URL getUrl(String s) {
        try {
            final String selfClass = JarExtractorTest.class.getSimpleName() + ".class";
            final URL selfUrl = JarExtractorTest.class.getResource(selfClass);
            final String result = selfUrl.toString().replace(selfClass, s);
            return new URL(result);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
