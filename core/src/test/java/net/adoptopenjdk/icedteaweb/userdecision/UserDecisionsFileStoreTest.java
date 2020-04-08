package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
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

    @Test
    public void shouldSaveDecisionInFileStore() throws Exception {
        // given
        final JNLPFile file = new JNLPFileFactory().create(getClass().getResource("/net/sourceforge/jnlp/basic.jnlp"));
        final File store = temporaryFolder.newFile("userDecisionStore.json");
        final UserDecisions userDecisions = new UserDecisionsFileStore(store);

        // when
        userDecisions.saveForApplication(file, UserDecision.of(RUN_UNSIGNED_APPLICATION, DENY));
        final Optional<AllowDeny> result = userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(DENY));
    }
}
