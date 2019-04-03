package javax.jnlp;

public interface PrintService {

    public java.awt.print.PageFormat getDefaultPage();

    public java.awt.print.PageFormat showPageFormatDialog(java.awt.print.PageFormat page);

    public boolean print(java.awt.print.Pageable document);

    public boolean print(java.awt.print.Printable painter);

}
