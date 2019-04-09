package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.Application;
import net.adoptopenjdk.icedteaweb.ApplicationManager;
import net.adoptopenjdk.icedteaweb.impl.ApplicationManagerImpl;
import net.sourceforge.jnlp.runtime.Boot;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.net.URL;
import java.util.concurrent.Executors;

public class ApplicationManagerTests {

    @Rule
    public GenericContainer container = ContainerUtils.create(JnlpAppConstants.JNLP_APP_1);

    @Test
    @Ignore
    public void testApplicationInstallation() throws Exception {
        //given:
        Boot.main(new String[]{});
        final URL url = ContainerUtils.getEndpointOnContainer(container, JnlpAppConstants.APP_JNLP_FILE);
        final ApplicationManager applicationManager = new ApplicationManagerImpl(Executors.newCachedThreadPool());

        //when:
        final Application application = applicationManager.install(url).get();

        //then:
        Assert.assertNotNull(application);
        Assert.assertTrue(applicationManager.contains(application));
    }

    @Test
    @Ignore
    public void testApplicationDeletion() throws Exception {
        //given:
        Boot.main(new String[]{});
        final URL url = ContainerUtils.getEndpointOnContainer(container, JnlpAppConstants.APP_JNLP_FILE);
        final ApplicationManager applicationManager = new ApplicationManagerImpl(Executors.newCachedThreadPool());

        //when:
        final Application application = applicationManager.install(url).get();
        applicationManager.remove(application).get();

        //then:
        Assert.assertFalse(applicationManager.contains(application));
    }
}