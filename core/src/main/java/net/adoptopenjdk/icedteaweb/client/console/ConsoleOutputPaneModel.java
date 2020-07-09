package net.adoptopenjdk.icedteaweb.client.console;

import net.sourceforge.jnlp.util.logging.OutputControllerLevel;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import net.sourceforge.jnlp.util.logging.headers.ObservableMessagesProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Comparator.comparing;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.DEBUG;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.ERROR;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.INFO;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.WARN;

public class ConsoleOutputPaneModel {

    ConsoleOutputPaneModel(ObservableMessagesProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    boolean shouldUpdate() {
        for (int i = lastUpdateIndex; i < dataProvider.getData().size(); i++) {
            if (!filtered(dataProvider.getData().get(i))) {
                return true;
            }
        }
        return false;
    }

    private class CatchedMessageWithHeaderComparator implements Comparator<MessageWithHeader> {

        private final Comparator<Header> delegate;

        protected CatchedMessageWithHeaderComparator(Comparator<Header> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(MessageWithHeader o1, MessageWithHeader o2) {
            try {
                final int order = revertSort ? 1 : -1;
                return order * body(o1, o2);
            } catch (NullPointerException npe) {
                //caused by corrupted c messages
                return 0;
            }
        }

        protected int body(MessageWithHeader o1, MessageWithHeader o2) {
            return delegate.compare(o1.getHeader(), o2.getHeader());
        }
    }

    static final Pattern defaultPattern = Pattern.compile("(m?)(.*\n*)*");
    ObservableMessagesProvider dataProvider;
    Pattern lastValidPattern = defaultPattern;
    Pattern usedPattern = lastValidPattern;
    int lastUpdateIndex; //to add just what was added newly
    int statisticsShown;
    private static final String HTMLCOLOR_GREENYELLOW = "AAAA00";
    private static final String HTMLCOLOR_PINKYREAD = "FF0055";
    private static final String HTMLCOLOR_BLACK = "000000";
    private static final String HTMLCOLOR_GREEN = "669966";
    private static final String HTMLCOLOR_PURPLE = "990066";
    String importList() {
        return importList(lastUpdateIndex);
    }

    String importList(int start) {
        return importList(highLight, start);
    }

    String importList(boolean mark, int start) {
        return  importList(mark, start, sortBy);
    }

    String importList(boolean mark, int start, int sortByLocal) {
        int added = start;
        StringBuilder sb = new StringBuilder();
        if (mark) {
            sb.append("<div style='");
            if (!wordWrap) {
                sb.append("white-space:nowrap;");
            }
            sb.append("font-family:\"Monospaced\"'>");
        }

        List<MessageWithHeader> sortedList;
        synchronized (dataProvider.getData()) {
            if (start == 0) {
                sortedList = preSort(dataProvider.getData(), sortByLocal);
            } else {
                sortedList = preSort(Collections.synchronizedList(dataProvider.getData().subList(start, dataProvider.getData().size())), sortByLocal);
            }
        }
        lastUpdateIndex = dataProvider.getData().size();

        for (MessageWithHeader messageWithHeader : sortedList) {
            if (filtered(messageWithHeader)) {
                continue;
            }


            if (mark) {
                final Header header = messageWithHeader.getHeader();
                final OutputControllerLevel level = header.level;
                sb.append("<div style='color:#");
                if (header.isClientApp) {
                    if (level == ERROR) {
                        sb.append(HTMLCOLOR_PURPLE);
                    } else {
                        sb.append(HTMLCOLOR_GREEN);
                    }
                } else {
                    if (level == WARN) {
                        sb.append(HTMLCOLOR_GREENYELLOW);
                    } else if (level == ERROR) {
                        sb.append(HTMLCOLOR_PINKYREAD);
                    } else {
                        sb.append(HTMLCOLOR_BLACK);
                    }
                }
                sb.append("'>");
                //sb.append("<![CDATA[");
            }
            String line = (createLine(messageWithHeader));
            if (mark) {
                line = escapeHtmlForJTextPane(line);
            }
            sb.append(line);
            if (mark) {
                //sb.append("]]>");
                sb.append("</div>");
            }
            //always wrap, looks better, works smoother
            sb.append("\n");
            added++;

        }
        if (mark) {
            sb.append("</div>");
        }
        statisticsShown = added;
        return sb.toString();

    }

    public static String escapeHtmlForJTextPane(String line) {
        line = line.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\n", "<br/>\n")
                .replaceAll("  ", "&nbsp; ")//small trick, html is reducing row of spaces to single space. This handles it and still allow line wrap
                .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        return line;
    }

    String createLine(MessageWithHeader m) {
        StringBuilder sb = new StringBuilder();
        if (showHeaders) {
            sb.append(m.getHeader().toString(showUser,
                    showOrigin,
                    showDate,
                    showLevel,
                    showCode,
                    showThread1,
                    showThread2));
        }
        if (showMessage && showHeaders) {
            sb.append(": ");
        }
        if (showMessage) {
            sb.append(m.getMessage());
        }
        return sb.toString();
    }

    List<MessageWithHeader> preSort(List<MessageWithHeader> data, int sortByLocal) {
        final List<MessageWithHeader> sortedData = new ArrayList<>(data);
        switch (sortByLocal) {
            case 0:
                if (revertSort) {
                    Collections.reverse(sortedData);
                }
                break;
            case 1:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.osUser)));
                break;
            case 2:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.isClientApp)));
                break;
            case 3:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.level)));
                break;
            case 4:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.timestampForSorting)));
                break;
            case 5:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.callerClass)));
                break;
            case 6:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.threadHash)));
                break;
            case 7:
                sortedData.sort(new CatchedMessageWithHeaderComparator(null) {
                    @Override
                    public int body(MessageWithHeader o1, MessageWithHeader o2) {
                        return o1.getMessage().compareTo(o2.getMessage());
                    }
                });
                break;
            case 8:
                sortedData.sort(new CatchedMessageWithHeaderComparator(comparing(h -> h.threadName)));
                break;
        }

        return Collections.synchronizedList(sortedData);
    }

    boolean filtered(MessageWithHeader m) {
        final Header header = m.getHeader();
        final OutputControllerLevel level = header.level;

        if (!showOut && !level.printToErrStream()) {
            return true;
        }
        if (!showErr && !level.printToOutStream()) {
            return true;
        }
        if (!showDebug && level == DEBUG) {
            return true;
        }
        if (!showInfo && level == INFO) {
            return true;
        }
        if (!showItw && !header.isClientApp) {
            return true;
        }
        if (!showApp && header.isClientApp) {
            return true;
        }
        if (regExLabel) {
            final String s = createLine(m);
            final boolean matches = usedPattern.matcher(s).matches();
            if (matchPattern != matches) {
                return true;
            }
        }
        return false;
    }

    String createStatisticHint() {
        return statisticsShown + "/" + dataProvider.getData().size();
    }
    boolean highLight;
    boolean matchPattern;
    boolean regExLabel;
    boolean revertSort;
    boolean showCode;
    boolean showDate;
    boolean showDebug;
    boolean showErr;
    boolean showHeaders;
    boolean showInfo;
    boolean showItw;
    boolean showApp;
    boolean showLevel;
    boolean showMessage;
    boolean showOrigin;
    boolean showOut;
    boolean showThread1;
    boolean showThread2;
    boolean showUser;
    int sortBy;
    boolean wordWrap;
}
