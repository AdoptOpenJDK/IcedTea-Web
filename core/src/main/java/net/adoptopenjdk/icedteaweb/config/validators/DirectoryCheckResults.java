package net.adoptopenjdk.icedteaweb.config.validators;

import java.util.List;

/**
 * This class is holding results of directory validation.
 * Various errors like can not read, write create dir can appear
 * For summaries of results are here getPasses, getFailures methods
 * <p>
 * Individual results can be read from results field, or converted to string
 * </p>
 */
public class DirectoryCheckResults {

    public final List<DirectoryCheckResult> results;

    /**
     * Wraps results so we can make some statistics or convert to message
     *
     * @param results results to be checked
     */
    public DirectoryCheckResults(final List<DirectoryCheckResult> results) {
        this.results = results;
    }

    /**
     * @return sum of passed checks, 0-3 per result
     */
    public int getPasses() {
        return results.stream().mapToInt(r -> r.getPasses()).sum();
    }

    /**
     * @return sum of failed checks, 0-3 per results
     */
    public int getFailures() {
        return results.stream().mapToInt(r -> r.getFailures()).sum();
    }

    /**
     * The result have one result per line, separated by \n
     * as is inherited from result.getMessage() method.
     *
     * @return all results connected.
     */
    public String getMessage() {
        return resultsToString(results);
    }

    /**
     * using getMessage
     *
     * @return a text representation of a {@code DirectoryValidator} object
     */
    @Override
    public String toString() {
        return getMessage();
    }


    private static String resultsToString(final List<DirectoryCheckResult> results) {
        final StringBuilder sb = new StringBuilder();
        for (final DirectoryCheckResult r : results) {
            if (r.getFailures() > 0) {
                sb.append(r.getMessage());
            }
        }
        return sb.toString();
    }
}
