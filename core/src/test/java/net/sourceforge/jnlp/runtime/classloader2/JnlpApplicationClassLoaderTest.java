package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class JnlpApplicationClassLoaderTest {

    private final JNLPFileFactory jnlpFileFactory = new JNLPFileFactory();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void findClass1() throws Exception {

        //given
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("empty.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, new DummyJarProvider());
        thrown.expect(ClassNotFoundException.class);
        thrown.expectMessage("not.in.Classpath");

        //when
        classLoader.findClass("not.in.Classpath");
    }

    @Test
    public void findClass2() throws Exception {

        //given
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("unavailable-jar.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, new DummyJarProvider());
        thrown.expect(ClassNotFoundException.class);
        thrown.expectMessage("not.in.Classpath");

        //when
        classLoader.findClass("not.in.Classpath");
    }

    @Test
    public void findClass3() throws Exception {

        //given
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("unavailable-jar.jnlp"));
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Error while creating classloader!");

        //when
        new JnlpApplicationClassLoader(file, new ErrorJarProvider());
    }

    @Test
    public void findClass4() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("eager-and-lazy.jnlp"));

        //when
        new JnlpApplicationClassLoader(file, jarProvider);

        //than
        Assert.assertTrue(jarProvider.hasTriedToDownload("eager.jar"));
        Assert.assertFalse(jarProvider.hasTriedToDownload("lazy.jar"));
    }

    @Test
    public void findClass5() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("eager-and-lazy.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        try {
            classLoader.findClass("class.in.lazy.Package");
        } catch (final Exception ignore) {}

        //than
        Assert.assertTrue(jarProvider.hasTriedToDownload("eager.jar"));
        Assert.assertTrue(jarProvider.hasTriedToDownload("lazy.jar"));
    }

    @Test
    public void findClass6() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("lazy-not-recursive.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //than
        Assert.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    public void findClass7() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("lazy-not-recursive.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        try {
            classLoader.findClass("class.in.lazy.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, jarProvider.getDownloaded().size());
    }

    @Test
    public void findClass8() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("lazy-not-recursive.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        try {
            classLoader.findClass("class.in.lazy.sub.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    public void findClass9() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("lazy-recursive.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //than
        Assert.assertEquals(0, jarProvider.getDownloaded().size());
    }

    @Test
    public void findClass10() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("lazy-recursive.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        try {
            classLoader.findClass("class.in.lazy.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, jarProvider.getDownloaded().size());
    }

    @Test
    public void findClass11() throws Exception {

        //given
        final DummyJarProvider jarProvider = new DummyJarProvider();
        final JNLPFile file = jnlpFileFactory.create(JnlpApplicationClassLoaderTest.class.getResource("lazy-recursive.jnlp"));
        final JnlpApplicationClassLoader classLoader = new JnlpApplicationClassLoader(file, jarProvider);

        //when
        try {
            classLoader.findClass("class.in.lazy.sub.A");
        } catch (final Exception ignore) {}

        //than
        Assert.assertEquals(1, jarProvider.getDownloaded().size());
    }



    private class DummyJarProvider implements Function<JARDesc, URL> {

        private final List<JARDesc> downloaded = new CopyOnWriteArrayList<>();

        @Override
        public URL apply(final JARDesc jarDesc) {
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

    private class ErrorJarProvider implements Function<JARDesc, URL> {

        @Override
        public URL apply(final JARDesc jarDesc) {
            throw new RuntimeException("Can not download " + jarDesc.getLocation());
        }

    }

}
