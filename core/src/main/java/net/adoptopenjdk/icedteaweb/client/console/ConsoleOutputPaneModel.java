package net.adoptopenjdk.icedteaweb.client.console;

import net.sourceforge.jnlp.util.logging.OutputControllerLevel;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import net.sourceforge.jnlp.util.logging.headers.ObservableMessagesProvider;
import net.sourceforge.jnlp.util.logging.headers.PluginHeader;
import net.sourceforge.jnlp.util.logging.headers.PluginMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.regex.Pattern;

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

    private abstract class CatchedMessageWithHeaderComparator implements Comparator<MessageWithHeader> {

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

        abstract int body(MessageWithHeader o1, MessageWithHeader o2);
    }

    //testing data provider
    static class TestMessagesProvider extends Observable implements ObservableMessagesProvider {

        List<MessageWithHeader> data = new ArrayList<MessageWithHeader>();
        List<MessageWithHeader> origData = new ArrayList<MessageWithHeader>();

        public List<MessageWithHeader> getData() {
            return data;
        }

        @Override
        public Observable getObservable() {
            return this;
        }

        public TestMessagesProvider() {
            createData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(new Random().nextInt(2000));
                            data.add(origData.get(new Random().nextInt(origData.size())));
                            TestMessagesProvider.this.setChanged();
                            TestMessagesProvider.this.notifyObservers();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        void createData() {
            String[] plugin = {
                "plugindebug 1384850630162925 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1204] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0:   PIPE: plugin read: plugin PluginProxyInfo reference 1 http://www.walter-fendt.de:80",
                "preinit_plugindebug 1384850630162920 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1204] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0:   PIPE: plugin read: plugin PluginProxyInfo reference 1 http://www.walter-fendt.de:80",
                "plugindebugX 1384850630162954 [jvanek][ITW-Cplugindebug 1384850630163008 [jvanek][ITW-C-PLUGIN][MESSAGE_DEBUG][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1124] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: parts[0]=plugin, parts[1]=PluginProxyInfo, reference, parts[3]=1, parts[4]=http://www.walter-fendt.de:80 -- decoded_url=http://www.walter-fendt.de:80",
                "preinit_pluginerror 1384850630163294 [jvanek][ITW-C-PLUGIN][MESSAGE_ERROR][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1134] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: Proxy info: plugin PluginProxyInfo reference 1 DIRECT",
                "pluginerror 1384850630163291 [jvanek][ITW-C-PLUGIN][MESSAGE_ERROR][Tue Nov 19 09:43:50 CET 2013][/home/jvanek/Desktop/icedtea-web/plugin/icedteanp/IcedTeaNPPlugin.cc:1134] ITNPP Thread# 140513434003264, gthread 0x7fcbd531f8c0: Proxy info: plugin PluginProxyInfo reference 1 DIRECT"
            };
            for (String string : plugin) {
                origData.add(new PluginMessage(string));
            }
            origData.add(new JavaMessage(new Header(OutputControllerLevel.ERROR_ALL), "message 1"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.ERROR_DEBUG), "message 3"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.WARNING_ALL), "message 2"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.WARNING_DEBUG), "message 4"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.MESSAGE_DEBUG), "message 9"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.MESSAGE_ALL, true), "app1"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.ERROR_ALL, true), "app2"));
            origData.add(new JavaMessage(new Header(OutputControllerLevel.MESSAGE_ALL), "message 0 - multilined \n"
                    + "since beginning\n"
                    + "         later\n"
                    + "again from beginning\n"
                    + "               even later"));
            data.addAll(origData);
        }
    }
    static final Pattern defaultPattern = Pattern.compile("(m?)(.*\n*)*");
    ObservableMessagesProvider dataProvider;
    Pattern lastValidPattern = defaultPattern;
    Pattern usedPattern = lastValidPattern;
    int lastUpdateIndex; //to add just what was added newly
    int statisticsShown;
    private static final String HTMLCOLOR_DIMRED = "FF6666";
    private static final String HTMLCOLOR_MIDGRAY = "666666";
    private static final String HTMLCOLOR_SPARKRED = "FF0000";
    private static final String HTMLCOLOR_LIGHTGRAY = "AAAAAA";
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
                sb.append("<div style='color:#");
                if (messageWithHeader.getHeader().isPlugin && messageWithHeader.getHeader() instanceof PluginHeader) {
                    if (!((PluginHeader) (messageWithHeader.getHeader())).preInit) {
                        if (messageWithHeader.getHeader().level.printToErrStream()) {
                            sb.append(HTMLCOLOR_DIMRED);
                        } else {
                            sb.append(HTMLCOLOR_MIDGRAY);
                        }
                    } else {
                        if (messageWithHeader.getHeader().level.printToErrStream()) {
                            sb.append(HTMLCOLOR_SPARKRED);
                        } else {
                            sb.append(HTMLCOLOR_LIGHTGRAY);
                        }
                    }
                } else {
                    if (messageWithHeader.getHeader().isClientApp) {
                        if (messageWithHeader.getHeader().level.printToErrStream()) {
                            sb.append(HTMLCOLOR_PURPLE);
                        } else {
                            sb.append(HTMLCOLOR_GREEN);
                        }
                    } else {
                        if (messageWithHeader.getHeader().level.isWarning()) {
                            sb.append(HTMLCOLOR_GREENYELLOW);
                        } else if (messageWithHeader.getHeader().level.printToErrStream()) {
                            sb.append(HTMLCOLOR_PINKYREAD);
                        } else {
                            sb.append(HTMLCOLOR_BLACK);
                        }
                    }
                }
                sb.append("'>");
                //sb.append("<![CDATA[");
            }
            if (messageWithHeader instanceof PluginMessage && ((PluginMessage) (messageWithHeader)).wasError) {
                sb.append("{corrupted}");
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
                    showLevel,
                    showDate,
                    showCode,
                    showThread1,
                    showThread2));
        }
        if (showMessage && showHeaders) {
            sb.append(": ");
        }
        if (showMessage) {
            sb.append(m.getMessage().toString());
        }
        return sb.toString();
    }

    List<MessageWithHeader> preSort(List<MessageWithHeader> data, int sortByLocal) {
        List<MessageWithHeader> sortedData;
        if (sortByLocal == 0) {
            if (revertSort) {
                sortedData = Collections.synchronizedList(new ArrayList<MessageWithHeader>(data));
                Collections.reverse(sortedData);
            } else {
                sortedData = data;
            }
        } else {
            sortedData = Collections.synchronizedList(new ArrayList<MessageWithHeader>(data));
            switch (sortByLocal) {
                case 1:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().user.compareTo(o2.getHeader().user);

                        }
                    });
                    break;
                case 2:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().getOrigin().compareTo(o2.getHeader().getOrigin());
                        }
                    });
                    break;
                case 3:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().level.toString().compareTo(o2.getHeader().level.toString());
                        }
                    });
                    break;
                case 4:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().timestamp.compareTo(o2.getHeader().timestamp);
                        }
                    });
                    break;
                case 5:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().caller.compareTo(o2.getHeader().caller);
                        }
                    });
                    break;
                case 6:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().thread1.compareTo(o2.getHeader().thread1);
                        }
                    });
                    break;
                case 7:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getMessage().compareTo(o2.getMessage());
                        }
                    });
                    break;
                case 8:
                    Collections.sort(sortedData, new CatchedMessageWithHeaderComparator() {
                        @Override
                        public int body(MessageWithHeader o1, MessageWithHeader o2) {
                            return o1.getHeader().thread2.compareTo(o2.getHeader().thread2);
                        }
                    });
                    break;
            }

        }
        return sortedData;
    }

    boolean filtered(MessageWithHeader m) {
        final Header header = m.getHeader();
        final OutputControllerLevel level = header.level;

        if (!showOut && level.printToOutStream() && !level.isWarning()) {
            return true;
        }
        if (!showErr && level.printToErrStream() && !level.isWarning()) {
            return true;
        }
        if (!showDebug && level.isDebug()) {
            return true;
        }
        if (!showInfo && level.isInfo()) {
            return true;
        }
        if (!showItw && !header.isClientApp) {
            return true;
        }
        if (!showApp && header.isClientApp) {
            return true;
        }
        if (!showJava && !header.isPlugin) {
            return true;
        }
        if (!showPlugin && header.isPlugin) {
            return true;
        }
        if (header instanceof PluginHeader) {
            PluginHeader mm = (PluginHeader) header;
            if (!showPreInit && mm.preInit) {
                return true;
            }
            if (!showPostInit && !mm.preInit) {
                return true;
            }
            if (!showIncomplete && m instanceof PluginMessage && ((PluginMessage) (m)).wasError) {
                return true;
            }
            if (!showComplete && m instanceof PluginMessage && !((PluginMessage) (m)).wasError) {
                return true;
            }
        }
        if (regExLabel) {
            String s = createLine(m);
            if (matchPattern && !usedPattern.matcher(s).matches()) {
                return true;
            }
            if (!matchPattern && usedPattern.matcher(s).matches()) {
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
    boolean showComplete;
    boolean showDate;
    boolean showDebug;
    boolean showErr;
    boolean showHeaders;
    boolean showIncomplete;
    boolean showInfo;
    boolean showItw;
    boolean showApp;
    boolean showJava;
    boolean showLevel;
    boolean showMessage;
    boolean showOrigin;
    boolean showOut;
    boolean showPlugin;
    boolean showPostInit;
    boolean showPreInit;
    boolean showThread1;
    boolean showThread2;
    boolean showUser;
    int sortBy;
    boolean wordWrap;
}
