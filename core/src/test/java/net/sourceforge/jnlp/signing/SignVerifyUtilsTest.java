package net.sourceforge.jnlp.signing;

import net.sourceforge.jnlp.tools.CertInformation;
import org.junit.Test;

import java.io.File;

public class SignVerifyUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testFailOnNullResource() {
        SignVerifyUtils.getSignByMagic(null, p -> new CertInformation());
    }

    @Test(expected = NullPointerException.class)
    public void testFailOnNullCertInformationProvider() {
        SignVerifyUtils.getSignByMagic(new File(""), null);
    }

}
