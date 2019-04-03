package javax.jnlp;

public interface PersistenceService {

    public static final int CACHED = 0;
    public static final int TEMPORARY = 1;
    public static final int DIRTY = 2;

    public long create(java.net.URL url, long maxsize) throws java.net.MalformedURLException, java.io.IOException;

    public FileContents get(java.net.URL url) throws java.net.MalformedURLException, java.io.IOException, java.io.FileNotFoundException;

    public void delete(java.net.URL url) throws java.net.MalformedURLException, java.io.IOException;

    public java.lang.String[] getNames(java.net.URL url) throws java.net.MalformedURLException, java.io.IOException;

    public int getTag(java.net.URL url) throws java.net.MalformedURLException, java.io.IOException;

    public void setTag(java.net.URL url, int tag) throws java.net.MalformedURLException, java.io.IOException;

}
