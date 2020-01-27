package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JarExtractor;
import net.adoptopenjdk.icedteaweb.classloader.Part;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;

import java.io.IOException;
import java.util.List;

public class ClassloaderTestUtils {

    public static final String CLASS_A = "net.adoptopenjdk.integration.ClassA";

    public static final String CLASS_B = "net.adoptopenjdk.integration.ClassB";

    public static final String JAR_1 = "classloader-integration-tests-module-1.jar";

    public static final String JAR_2 = "classloader-integration-tests-module-2.jar";

    public static final String JAR_3 = "classloader-integration-tests-module-3.jar";

    public static final String JAR_WITH_NATIVE = "classloader-integration-tests-module-native.jar";

    private static final JNLPFileFactory JNLP_FILE_FACTORY = new JNLPFileFactory();

    public static DummyPartsHandler createDummyPartsHandlerFor(final String name) throws IOException, ParseException {
        final JNLPFile jnlpFile = createFile(name);
        final List<Part> parts = createPartsFor(jnlpFile);
        return new DummyPartsHandler(parts, jnlpFile);
    }

    public static List<Part> createPartsFor(final JNLPFile file) {
        return createFor(file).getParts();
    }

    public static JNLPFile createFile(final String name) throws IOException, ParseException {
        return JNLP_FILE_FACTORY.create(IntegrationTestResources.class.getResource(name));
    }

    public static JarExtractor createFor(final JNLPFile file) {
        return new JarExtractor(file, JNLP_FILE_FACTORY);
    }
}
