package net.adoptopenjdk.icedteaweb.integration.testcase1.applications;

import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class SimpleJavaApplication {

    public static final String HELLO_FILE = "hello.txt";
    public static final String SYSTEM_PROPERTIES_FILE = "system.properties";
    public static final String ARGUMENTS_FILE = "arguments";
    public static final String SYSTEM_ENVIRONMENT_FILE = "system.environment";

    public static void main(String[] args) throws Exception {
        System.out.println("Simple Java application install and launched by Iced-Tea Web");
        System.out.println("Arguments: " +  Arrays.toString(args));

        writeFile(HELLO_FILE, "Hello from managed app\n");
        writeFile(SYSTEM_PROPERTIES_FILE, System.getProperties());
        writeFile(ARGUMENTS_FILE, Arrays.toString(args));
        writeFile(SYSTEM_ENVIRONMENT_FILE, asProperties(System.getenv()));
    }

    private static Properties asProperties(Map<String, String> getenv) {
        final Properties result = new Properties();
        for (Map.Entry<String, String> entry : getenv.entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void writeFile(final String fileName, final String content) throws Exception {
        writeFile(fileName, writer -> writer.write(content));
    }

    private static void writeFile(final String fileName, final Properties content) throws Exception {
        writeFile(fileName, writer -> content.store(writer, null));
    }

    private static void writeFile(final String fileName, final ThrowingConsumer<Writer> consumer) throws Exception {
        final PersistenceService persistenceService = (PersistenceService) ServiceManager.lookup("PersistenceService");
        final String fileUrl = "http://localhost/" + fileName;
        persistenceService.create(new URL(fileUrl), Long.MAX_VALUE);
        final FileContents fileContents = persistenceService.get(new URL(fileUrl));
        try (final OutputStream outputStream = fileContents.getOutputStream(true)) {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);

            consumer.accept(writer);
            writer.flush();
        }
    }

    private interface ThrowingConsumer<E> {
        void accept(E e) throws Exception;
    }
}
