package net.adoptopenjdk.icedteaweb.integration.classloader;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.classloader2.JarExtractor;

import java.io.IOException;

public class ClassloaderTestUtils {

    public static final String CLASS_A = "net.adoptopenjdk.integration.ClassA";

    public static final String CLASS_B = "net.adoptopenjdk.integration.ClassB";

    public static final String JAR_1 = "classloader-integration-tests-module-1.jar";

    public static final String JAR_2 = "classloader-integration-tests-module-2.jar";

    public static final String JAR_WITH_NATIVE = "classloader-integration-tests-module-native.jar";

    private static final JNLPFileFactory JNLP_FILE_FACTORY = new JNLPFileFactory();

    public static JNLPFile createFile(final String name) throws IOException, ParseException {
        return JNLP_FILE_FACTORY.create(ClassloaderTestUtils.class.getResource(name));
    }

    public static JarExtractor createFor(final String name) throws IOException, ParseException {
        return new JarExtractor(JNLP_FILE_FACTORY.create(ClassloaderTestUtils.class.getResource(name)), JNLP_FILE_FACTORY);
    }
}
