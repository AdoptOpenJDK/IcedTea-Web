package javax.jnlp;

public interface IntegrationService {

    public boolean hasAssociation(java.lang.String mimeType, java.lang.String[] extensions);

    public boolean hasDesktopShortcut();

    public boolean hasMenuShortcut();

    public boolean removeAssociation(java.lang.String mimeType, java.lang.String[] extensions);

    public boolean removeShortcuts();

    public boolean requestAssociation(java.lang.String mimeType, java.lang.String[] extensions);

    public boolean requestShortcut(boolean onDesktop, boolean inMenu, java.lang.String subMenu);

}
