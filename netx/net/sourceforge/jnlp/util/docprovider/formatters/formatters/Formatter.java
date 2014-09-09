package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

public interface Formatter {

    public String process(String s);

    public String wrapParagraph(String s);

    public String getHeaders(String id, String encoding);

    public String getNewLine();
    
    public String getBold(String s);

    public String getBoldOpening();

    public String getBoldClosing();
    
    public String getBreakAndBold();
    
    public String getCloseBoldAndBreak();
    
    public String getBoldCloseNwlineBoldOpen();

    public String getTitle(String name);

    public String getUrl(String url);
    public String getUrl(String url, String  appearence);
    
    public String getOption(String key, String  value);

    public String getSeeAlso(String s);

    public String getTail();

    public String getFileSuffix();

}
