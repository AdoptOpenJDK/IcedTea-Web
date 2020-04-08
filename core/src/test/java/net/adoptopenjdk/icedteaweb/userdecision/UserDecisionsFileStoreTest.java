package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.ALLOW;
import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.DENY;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_UNSIGNED_APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link UserDecisionsFileStore}.
 */
public class UserDecisionsFileStoreTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private UserDecisions userDecisions;
    private File store;

    @Before
    public final void setUp() throws Exception {
        store = temporaryFolder.newFile("userDecisionStore.json");
        userDecisions = new UserDecisionsFileStore(store);
    }

    @Test
    public void newStoreShouldReturnOptionalEmpty() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");

        // when
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void shouldSaveDecisionInForApplication() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");

        // when
        userDecisions.saveForApplication(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.of(DENY)));
    }

    @Test
    public void shouldSaveDecisionForDomain() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");

        // when
        userDecisions.saveForDomain(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.of(DENY)));
    }

    @Test
    public void shouldCreateFileIfMissing() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");
        assertTrue(store.delete());

        // when
        userDecisions.saveForApplication(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.of(DENY)));
    }

    @Test
    public void applicationShouldTakePrecedenceOverDomain() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");
        assertTrue(store.delete());

        // when
        // save domain twice to ensure order does not matter
        userDecisions.saveForDomain(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, ALLOW));
        userDecisions.saveForApplication(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        userDecisions.saveForDomain(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, ALLOW));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.of(DENY)));
    }

    @Test
    public void shouldNotReturnSavedValueOfOtherApplication() throws Exception {
        // given
        final JNLPFile file = loadJnlpFile("/net/sourceforge/jnlp/basic.jnlp");
        final JNLPFile otherFile = loadJnlpFile("/net/sourceforge/jnlp/launchApp.jnlp");

        // when
        userDecisions.saveForApplication(otherFile, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result, is(Optional.empty()));
    }

    private JNLPFile loadJnlpFile(String name) throws IOException, ParseException {
        return new JNLPFileFactory().create(getClass().getResource(name));
    }
}
