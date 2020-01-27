package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JnlpApplicationClassLoaderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void findClass1() throws Exception {

        //given
        final PartsHandler partsHandler = createDummyPartsHandlerFor("empty.jnlp");

        //expect
        thrown.expect(ClassNotFoundException.class);
        thrown.expectMessage("not.in.Classpath");

        //when
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
        classLoader.findClass("not.in.Classpath");
    }

    @Test
    public void findClass3() throws Exception {

        //given
        final PartsHandler partsHandler = createErrorPartsHandler("unavailable-jar.jnlp");

        // expect
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Error while downloading jar!");

        //when
        new JnlpApplicationClassLoader(partsHandler);
    }

    @Test
    public void findClass4() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("eager-and-lazy.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assert.assertTrue(partsHandler.hasTriedToDownload("eager.jar"));
        Assert.assertFalse(partsHandler.hasTriedToDownload("lazy.jar"));
    }

    @Test
    public void findClass5() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("eager-and-lazy.jnlp");

        //when
        try {
            final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
            classLoader.findClass("class.in.lazy.Package");
        } catch (final Exception ignore) {}

        //than
        Assert.assertTrue(partsHandler.hasTriedToDownload("eager.jar"));
        Assert.assertTrue(partsHandler.hasTriedToDownload("lazy.jar"));
    }

    @Test
    public void findClass6() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("lazy-not-recursive.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assert.assertEquals(0, partsHandler.getDownloaded().size());
    }

    @Test
    public void findClass7() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("lazy-not-recursive.jnlp");

        //when
        try {
            final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
            classLoader.findClass("class.in.lazy.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, partsHandler.getDownloaded().size());
    }

    @Test
    public void findClass8() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("lazy-not-recursive.jnlp");

        //when
        try {
            final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
            classLoader.findClass("class.in.lazy.sub.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, partsHandler.getDownloaded().size());
    }

    @Test
    public void findClass9() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("lazy-recursive.jnlp");

        //when
        new JnlpApplicationClassLoader(partsHandler);

        //than
        Assert.assertEquals(0, partsHandler.getDownloaded().size());
    }

    @Test
    public void findClass10() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("lazy-recursive.jnlp");

        //when
        try {
            final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
            classLoader.findClass("class.in.lazy.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, partsHandler.getDownloaded().size());
    }

    @Test
    public void findClass11() throws Exception {

        //given
        final DummyPartsHandler partsHandler = createDummyPartsHandlerFor("lazy-recursive.jnlp");

        //when
        try {
            final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(partsHandler);
            classLoader.findClass("class.in.lazy.sub.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, partsHandler.getDownloaded().size());
    }

    private static class DummyPartsHandler extends PartsHandler {

        private final List<JARDesc> downloaded = new CopyOnWriteArrayList<>();

        public DummyPartsHandler(final List<Part> parts, final JNLPFile file) {
            super(parts, file);
        }

        @Override
        protected URL getLocalUrlForJar(final JARDesc jarDesc) {
            System.out.println("Should load " + jarDesc.getLocation());
            downloaded.add(jarDesc);
            return jarDesc.getLocation();
        }

        public boolean hasTriedToDownload(final String name) {
            return downloaded.stream()
                    .anyMatch(jar -> jar.getLocation().toString().endsWith(name));
        }

        public List<JARDesc> getDownloaded() {
            return Collections.unmodifiableList(downloaded);
        }
    }

    private static class ErrorPartsHandler extends PartsHandler {


        public ErrorPartsHandler(final List<Part> parts, final JNLPFile file) {
            super(parts, file);
        }

        @Override
        protected URL getLocalUrlForJar(final JARDesc jarDesc) {
            throw new RuntimeException("Can not download " + jarDesc.getLocation());
        }

    }

    public static ErrorPartsHandler createErrorPartsHandler(final String name) throws IOException, ParseException {
        final JNLPFile file = createFile(name);
        final List<Part> parts = createFor(file).getParts();
        return new ErrorPartsHandler(parts, file);
    }

    public static DummyPartsHandler createDummyPartsHandlerFor(final String name) throws IOException, ParseException {
        final JNLPFile file = createFile(name);
        final List<Part> parts = createFor(file).getParts();
        return new DummyPartsHandler(parts, file);
    }

    public static JNLPFile createFile(final String name) throws IOException, ParseException {
        final JNLPFileFactory jnlpFileFactory = new JNLPFileFactory();
        return jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource(name));
    }

    public static JarExtractor createFor(final JNLPFile file) {
        final JNLPFileFactory jnlpFileFactory = new JNLPFileFactory();
        return new JarExtractor(file, jnlpFileFactory);
    }

}
