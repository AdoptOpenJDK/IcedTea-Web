package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.DENY;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_UNSIGNED_APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link UserDecisionsFileStore}.
 */
public class UserDecisionsFileStoreTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private UserDecisions userDecisions;

    @Before
    public final void setUp() throws Exception {
        userDecisions = new UserDecisionsFileStore(temporaryFolder.newFile("userDecisionStore.json"));
    }

    @Test
    public void shouldSaveDecisionInFileStore() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");

        // when
        userDecisions.saveForApplication(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.of(DENY)));
    }

    private JNLPFile loadJnlpFile(String name) throws IOException, ParseException {
        return new JNLPFileFactory().create(getClass().getResource(name));
    }
}
