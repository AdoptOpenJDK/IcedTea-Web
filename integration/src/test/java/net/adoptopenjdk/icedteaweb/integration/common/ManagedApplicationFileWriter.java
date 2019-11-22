package net.adoptopenjdk.icedteaweb.integration.common;

import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class ManagedApplicationFileWriter {
    public static void writeFile(final String fileName, final String content) throws Exception {
        writeFile(fileName, writer -> writer.write(content));
    }

    public static void writeFile(final String fileName, final Properties content) throws Exception {
        writeFile(fileName, writer -> content.store(writer, null));
    }

    public static void writeFile(final String fileName, final ThrowingConsumer<Writer> consumer) throws Exception {
        final PersistenceService persistenceService = (PersistenceService) ServiceManager.lookup("PersistenceService");
        final URL fileUrl = new URL("http://localhost/" + fileName);
        persistenceService.delete(fileUrl);
        persistenceService.create(fileUrl, Long.MAX_VALUE);
        final FileContents fileContents = persistenceService.get(fileUrl);
        if (Objects.nonNull(fileContents)) {
            try (final OutputStream outputStream = fileContents.getOutputStream(true)) {
                final OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                consumer.accept(writer);
                writer.flush();
            }
        } else {
            throw new IOException("Could not write to file: " + fileUrl);
        }
    }

    public interface ThrowingConsumer<E> {
        void accept(E e) throws Exception;
    }
}
