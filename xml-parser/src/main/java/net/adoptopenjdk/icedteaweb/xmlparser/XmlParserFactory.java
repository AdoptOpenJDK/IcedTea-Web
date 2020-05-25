package net.adoptopenjdk.icedteaweb.xmlparser;

import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;

public class XmlParserFactory {

    /**
     * @return a parser implementation for the given parser type.
     */
    public static XMLParser getParser(ParserType parserToUse) {
        ParseException.setUsed(parserToUse);
        return parserToUse == MALFORMED ? new MalformedXMLParser() : new XMLParser();
    }
}
