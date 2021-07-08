package net.adoptopenjdk.icedteaweb.jnlp.element.application;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;

public class ApplicationDescTest {

    @Test
    public void canAddArguments() {
        // arrange
        final ApplicationDesc applicationDesc = new ApplicationDesc("mainClass", new String[0]);

        // act
        applicationDesc.addArgument("someArg");

        // assert
        assertThat(applicationDesc.getArguments(), arrayContaining("someArg"));
    }

}
