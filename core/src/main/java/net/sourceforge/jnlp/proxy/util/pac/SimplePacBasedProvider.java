package net.sourceforge.jnlp.proxy.util.pac;

public class SimplePacBasedProvider extends AbstractPacBasedProvider {

    private final PacEvaluator pacEvaluator;

    public SimplePacBasedProvider(final PacEvaluator pacEvaluator) {
        this.pacEvaluator = pacEvaluator;
    }

    @Override
    protected PacEvaluator getPacEvaluator() {
        return pacEvaluator;
    }
}
