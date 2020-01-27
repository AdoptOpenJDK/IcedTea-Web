package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesReader;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.JNLPFile;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.jar.Attributes.Name.MAIN_CLASS;

public class ClassLoaderUtils {

    private static final Executor BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();

    public static Executor getClassloaderBackgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static <V> V waitForCompletion(Future<V> f, String message) {
        try {
            return f.get();
        } catch (final Exception e) {
            throw new RuntimeException(message, e);
        }
    }

    public static String getMainClass(final JNLPFile file, final ResourceTracker tracker) {
        final String fromEntryPoint = getMainClassFromEntryPoint(file);
        if (fromEntryPoint != null) {
            return fromEntryPoint;
        }
        return getMainClassFromManifest(file, tracker);
    }

    private static String getMainClassFromManifest(final JNLPFile file, final ResourceTracker tracker) {
        final List<JARDesc> mainJars = file.getJnlpResources().getJARs().stream()
                .filter(JARDesc::isMain)
                .collect(Collectors.toList());
        if (mainJars.size() == 1) {
            final JARDesc jarDesc = mainJars.get(0);
            final String fromManifest = ManifestAttributesReader.getAttributeFromJar(MAIN_CLASS, jarDesc.getLocation(), tracker);
            return fromManifest;
        } else if (mainJars.size() == 0) {
            final JARDesc jarDesc = file.getJnlpResources().getJARs().get(0);
            final String fromManifest = ManifestAttributesReader.getAttributeFromJar(MAIN_CLASS, jarDesc.getLocation(), tracker);
            return fromManifest;
        }
        return null;
    }

    public static String getMainClassFromEntryPoint(final JNLPFile file) {
        final EntryPoint entryPoint = file.getEntryPointDesc();
        if (entryPoint instanceof ApplicationDesc || entryPoint instanceof AppletDesc) {
            return entryPoint.getMainClass();
        }
        return null;
    }

}
