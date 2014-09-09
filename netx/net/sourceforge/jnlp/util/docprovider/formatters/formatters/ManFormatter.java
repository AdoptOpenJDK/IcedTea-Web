package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;

public class ManFormatter extends ReplacingTextFormatter {

    public static class KnownSections {

        public static final String NAME = "NAME";
        public static final String SYNOPSIS = "SYNOPSIS";
        public static final String DESCRIPTION = "DESCRIPTION";
        public static final String OPTIONS = "OPTIONS";
        public static final String COMMANDS = "COMMANDS";
        public static final String EXAMPLES = "EXAMPLES";
        public static final String FILES = "FILES";
        public static final String BUGS = "BUGS";
        public static final String AUTHOR = "AUTHOR";
        public static final String SEE_ALSO = "SEE_ALSO";

    }
    
    public  static final String SUFFIX = "1";

    @Override
    public String getNewLine() {
        return PlainTextFormatter.getLineSeparator() + ".br" + PlainTextFormatter.getLineSeparator();
    }

    @Override
    public String getBoldOpening() {
        return PlainTextFormatter.getLineSeparator() + ".B ";
    }

    @Override
    public String getBoldClosing() {
        return PlainTextFormatter.getLineSeparator();
    }

    /**
     * There is one line break less
     */
    @Override
    public String getBreakAndBold() {
        return PlainTextFormatter.getLineSeparator() + ".br" + PlainTextFormatter.getLineSeparator() + ".B ";
    }

    /**
     * There is one line break less
     */
    @Override
    public String getCloseBoldAndBreak() {
        return PlainTextFormatter.getLineSeparator() + ".B " + PlainTextFormatter.getLineSeparator() + ".br" + PlainTextFormatter.getLineSeparator();
    }

    /**
     * There are two lines breaks less
     */
    @Override
    public String getBoldCloseNwlineBoldOpen() {
        return PlainTextFormatter.getLineSeparator() + ".br" + PlainTextFormatter.getLineSeparator() + ".B ";
    }

    @Override
    public String getTitle(String name) {
        return ".SH " + localizeTitle(name) + PlainTextFormatter.getLineSeparator() + PlainTextFormatter.getLineSeparator();
    }

    @Override
    public String getHeaders(String id, String encoding) {
        return ".TH " + id + " 1 \"" + getManPageDate(true) + "\"" + " \"" + TextsProvider.ITW + " " + getVersion() + "\"" + PlainTextFormatter.getLineSeparator();
    }

    @Override
    public String getTail() {
        return "";
    }

    private String getManPageDate(boolean localize) {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int year = c.get(Calendar.YEAR);
        String month;
        if (localize) {
            Format formatter = new SimpleDateFormat("MMM");
            month = formatter.format(now);
        } else {
            month = getUnlocallizedMonth(c.get(Calendar.MONTH));;
        }
        return day + " " + month + " " + year;
    }

    private String getUnlocallizedMonth(int get) {
        switch (get) {
            case Calendar.JANUARY:
                return "Jan";
            case Calendar.FEBRUARY:
                return "Feb";
            case Calendar.MARCH:
                return "Mar";
            case Calendar.APRIL:
                return "Apr";
            case Calendar.MAY:
                return "May";
            case Calendar.JUNE:
                return "Jun";
            case Calendar.JULY:
                return "Jul";
            case Calendar.AUGUST:
                return "Aug";
            case Calendar.SEPTEMBER:
                return "Sep";
            case Calendar.OCTOBER:
                return "Oct";
            case Calendar.NOVEMBER:
                return "Nov";
            case Calendar.DECEMBER:
                return "Dec";
            default:
                return "Unkw";

        }
    }

    @Override
    public String getUrl(String url, String look) {
        return url;
    }

    @Override
    public String getSeeAlso(String s) {
        return ".BR " + s + " (1)" + PlainTextFormatter.getLineSeparator();
    }

    @Override
    public String getFileSuffix() {
        return "."+SUFFIX;
    }

    @Override
    public String wrapParagraph(String s) {
        return PlainTextFormatter.getLineSeparator() + ".PP" + PlainTextFormatter.getLineSeparator() + s;
    }

    @Override
    public String getOption(String key, String value) {
        if (key.startsWith("-")) {
            key = "\\" + key;
        }
        if (value.startsWith("-")) {
            value = "\\" + value;
        }
        return ".TP 12" + PlainTextFormatter.getLineSeparator()
                + key + PlainTextFormatter.getLineSeparator()
                + process(value) + PlainTextFormatter.getLineSeparator();
    }
}
