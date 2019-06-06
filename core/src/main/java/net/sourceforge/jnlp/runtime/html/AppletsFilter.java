/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.jnlp.runtime.html;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jvanek
 */
public class AppletsFilter {

    private final static Logger LOG = LoggerFactory.getLogger(AppletsFilter.class);

    private final List<Element> found;
    private final URL docBase;
    private final List<String> ids;

    public AppletsFilter(List<Element> found, URL docBase, List<String> ids) {
        this.found = found;
        this.docBase = docBase;
        this.ids = ids;
    }

    public List<AppletParser> getApplets() throws ParserConfigurationException, SAXException, IOException {
        List<Element> appletElement = getAppletElements();
        List<AppletParser> aps = new ArrayList<>(appletElement.size());
        for (int i = 0; i < appletElement.size(); i++) {
            Element element = appletElement.get(i);
            AppletParser ap = new AppletParser(element, docBase);
            aps.add(ap);
            LOG.debug("added: {}", aps.size() - 1);
            LOG.debug(ap.toString());
        }
        return aps;
    }

    private List<Element> getAppletElements() throws ParserConfigurationException, SAXException, IOException {
        if (found.isEmpty()) {
            throw new RuntimeException("No applet found on this html page (supported are object, embed and applet tags)");
        }
        List<Integer> id = sanitizeFilter();
        List<Element> r = new ArrayList<>(found.size());
        for (int i = 0; i < found.size(); i++) {
            Element element = found.get(i);
            if (id.contains(i)) {
                r.add(element);
                LOG.debug("adding applet id: {} as: {}", i, r.size() - 1);
            }
        }
        return r;
    }

    private List<Integer> sanitizeFilter() {
        List<Integer> r = new ArrayList<>(found.size());
        if (ids.isEmpty()) {
            if (found.size() > 1) {
                LOG.info("More then one ({}) applets found. Using  first. You can specify ''all'' or numbers to specify applets you want to run.", found.size());
            }
            r.add(0);
            return r;
        }
        if (ids.size() == 1 && ids.get(0).equalsIgnoreCase("all")) {
            for (int i = 0; i < found.size(); i++) {
                r.add(i);
            }
            return r;
        }
        for (int i = 0; i < ids.size(); i++) {
            Integer id  = null;
            try {
                id = Integer.parseInt(ids.get(i));
            } catch (NumberFormatException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                continue;
            }
            if (id < 0) {
                LOG.info("You have selected lesser then 0th applet. Using first");
                if (!r.contains(0)) {
                    r.add(0);
                }
                continue;
            }
            if (id >= found.size()) {
                LOG.info("You have selected higher then " + (found.size() - 1) + "th applet. Using last");
                if (!r.contains(found.size() - 1)) {
                    r.add(found.size() - 1);
                }
                continue;
            }
            LOG.info("Using " + id + "th applet from total of  count " + (found.size() - 1));
            r.add(id);
        }
        return r;
    }
}
