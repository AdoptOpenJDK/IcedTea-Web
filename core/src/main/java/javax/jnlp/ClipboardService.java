package javax.jnlp;

public interface ClipboardService {

    public java.awt.datatransfer.Transferable getContents();

    public void setContents(java.awt.datatransfer.Transferable contents);

}
