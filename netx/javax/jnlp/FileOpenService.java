package javax.jnlp;

public interface FileOpenService {

    public FileContents openFileDialog(java.lang.String pathHint, java.lang.String[] extensions) throws java.io.IOException;

    public FileContents[] openMultiFileDialog(java.lang.String pathHint, java.lang.String[] extensions) throws java.io.IOException;

}
