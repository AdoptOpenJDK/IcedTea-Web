/* XMLElement.java
 *
 * $Revision: 1.2 $
 * $Date: 2002/08/03 04:36:34 $
 * $Name:  $
 *
 * This file is part of NanoXML 2 Lite.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 *****************************************************************************/

/* JAM: hacked the source to remove unneeded methods and comments. */

package net.adoptopenjdk.icedteaweb.xmlparser;

import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * XMLElement is a representation of an XML object. The object is able to parse
 * XML code.
 * <dl>
 * <dt><b>Parsing XML Data</b></dt>
 * <dd>
 * You can parse XML data using the following code:
 * <pre>{@code
 *XMLElement xml = new XMLElement();
 *FileReader reader = new FileReader("filename.xml");
 *xml.parseFromReader(reader);
 *}</pre></dd></dl>
 * <dl><dt><b>Retrieving Attributes</b></dt>
 * <dd>
 * You can enumerate the attributes of an element using the method
 * {@link #enumerateAttributeNames() enumerateAttributeNames}.
 * The attribute values can be retrieved using the method
 * {@link #getAttribute(java.lang.String) getAttribute}.
 * The following example shows how to list the attributes of an element:
 * <pre>{@code
 *XMLElement element = ...;
 *Enumeration enum = element.enumerateAttributeNames();
 *while (enum.hasMoreElements()) {
 *    String key = (String) enum.nextElement();
 *    String value = (String) element.getAttribute(key);
 *    System.out.println(key + " = " + value);
 *}}</pre></dd></dl>
 * <dl><dt><b>Retrieving Child Elements</b></dt>
 * <dd>
 * You can enumerate the children of an element using
 * {@link #enumerateChildren() enumerateChildren}.
 * The number of child elements can be retrieved using
 * {@code countChildren}.
 * </dd></dl>
 * <dl><dt><b>Elements Containing Character Data</b></dt>
 * <dd>
 * If an elements contains character data, like in the following example:
 * <pre>{@code <title>The Title</title>}</pre>
 * you can retrieve that data using the method
 * {@link #getContent() getContent}.
 * </dd></dl>
 * <dl><dt><b>Subclassing XMLElement</b></dt>
 * <dd>
 * When subclassing XMLElement, you need to override the method
 * {@link #createAnotherElement() createAnotherElement}
 * which has to return a new copy of the receiver.
 * </dd></dl>
 *
 * @see XMLParseException
 *
 * @author Marc De Scheemaecker
 *         &lt;<A href="mailto:cyberelf@mac.com">cyberelf@mac.com</A>&gt;
 * @version $Name:  $, $Revision: 1.2 $
 */
public class XMLElement {

    private static final String AMP_KEY = "amp";
    private static final String QUOT_KEY = "quot";
    private static final String APOS_KEY = "apos";
    private static final String LT_KEY = "lt";
    private static final String GT_KEY = "gt";
    private static final char AMP_CHAR = '&';
    private static final char QUOT_CHAR = '"';
    private static final char APOS_CHAR = '\'';
    private static final char LT_CHAR = '<';
    private static final char GT_CHAR = '>';
    /**
     * The attributes given to the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field can be empty.</li>
     *     <li>The field is never {@code null}.</li>
     *     <li>The keys and the values are strings.</li>
     * </ul></dd></dl>
     */
    private final Map<String, Object> attributes;

    /**
     * Child elements of the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field can be empty.</li>
     *     <li>The field is never {@code null}.</li>
     *     <li>The elements are instances of {@code XMLElement}
     *         or a subclass of {@code XMLElement}.</li>
     * </ul></dd></dl>
     */
    private final Vector<XMLElement> children;

    /**
     * The name of the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is {@code null} iff the element is not
     *         initialized by either parse or {@link #setName setName()}.</li>
     *     <li>If the field is not {@code null}, it's not empty.</li>
     *     <li>If the field is not {@code null}, it contains a valid
     *         XML identifier.</li>
     * </ul></dd></dl>
     */
    private String name;

    /**
     * The {@code #PCDATA} content of the object.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is {@code null} iff the element is not a
     *         {@code #PCDATA} element.</li>
     *     <li>The field can be any string, including the empty string.</li>
     * </ul></dd></dl>
     */
    private String contents;

    /**
     * Conversion table for &amp;...; entities. The keys are the entity names
     * without the &amp; and ; delimiters.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is never {@code null}.</li>
     *     <li>The field always contains the following associations:
     *         "lt"&nbsp;=&gt;&nbsp;"&lt;", "gt"&nbsp;=&gt;&nbsp;"&gt;",
     *         "quot"&nbsp;=&gt;&nbsp;"\"", "apos"&nbsp;=&gt;&nbsp;"'",
     *         "amp"&nbsp;=&gt;&nbsp;"&amp;"</li>
     *     <li>The keys are strings</li>
     *     <li>The values are char arrays</li>
     * </ul></dd></dl>
     */
    private final Map<String, char[]> entities;

    /**
     * {@code true} if the case of the element and attribute names are case
     * insensitive.
     */
    private final boolean ignoreCase;

    /**
     * {@code true} if the leading and trailing whitespace of {@code #PCDATA}
     * sections have to be ignored.
     */
    private final boolean ignoreWhitespace;

    /**
     * Character read too much.
     * <p>
     * This character provides push-back functionality to the input reader
     * without having to use a PushbackReader.
     * If there is no such character, this field is {@code '\0'}.
     */
    private char charReadTooMuch;

    /**
     * The reader provided by the caller of the parse method.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is not {@code null} while the parse method is
     *         running.</li>
     * </ul></dd></dl>
     */
    private Reader reader;

    /**
     * The current line number in the source content.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>parserLineNr &gt; 0 while the parse method is running.</li>
     * </ul></dd></dl>
     */
    private int parserLineNr;

    /**
     * Creates and initializes a new XML element.
     * <p>
     * Calling the construction is equivalent to:
     * <ul><li>{@code new XMLElement(new HashMap(), false, true)}</li></ul>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     *     <li>{@linkplain #enumerateChildren} =&gt; empty enumeration</li>
     *     <li>enumeratePropertyNames() =&gt; empty enumeration</li>
     *     <li>getChildren() =&gt; empty vector</li>
     *     <li>{@linkplain #getContent} =&gt; ""</li>
     *     <li>{@linkplain #getName} =&gt; null</li>
     * </ul></dd></dl>
     */
    public XMLElement() {
        this(new HashMap<>(), false, true, true);
    }

    /**
     * Creates and initializes a new XML element.
     * <p>
     * This constructor should <i>only</i> be called from
     * {@link #createAnotherElement} to create child elements.
     *
     * @param entities
     *     The entity conversion table.
     * @param skipLeadingWhitespace
     *     {@code true} if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     * @param fillBasicConversionTable
     *     {@code true} if the basic entities need to be added to
     *     the entity list (client code calling this constructor).
     * @param ignoreCase
     *     {@code true} if the case of element and attribute names have
     *     to be ignored.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code entities != null}</li>
     *     <li>if {@code fillBasicConversionTable == false}
     *         then {@code entities} contains at least the following
     *         entries: {@code amp}, {@code lt}, {@code gt}, {@code apos} and
     *         {@code quot}</li>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     *     <li>{@linkplain #enumerateChildren} =&gt; empty enumeration</li>
     *     <li>enumeratePropertyNames() =&gt; empty enumeration</li>
     *     <li>getChildren() =&gt; empty vector</li>
     *     <li>{@linkplain #getContent} =&gt; ""</li>
     *     <li>{@linkplain #getName} =&gt; null</li>
     * </ul></dd></dl>
     */
    private XMLElement(final Map<String, char[]> entities,
                       final boolean skipLeadingWhitespace,
                       final boolean fillBasicConversionTable,
                       final boolean ignoreCase) {
        this.ignoreWhitespace = skipLeadingWhitespace;
        this.ignoreCase = ignoreCase;
        this.name = null;
        this.contents = "";
        this.attributes = new HashMap<>();
        this.children = new Vector<>();
        this.entities = Objects.requireNonNull(entities);

        if (fillBasicConversionTable) {
            this.entities.put(AMP_KEY, new char[] {AMP_CHAR});
            this.entities.put(QUOT_KEY, new char[] {QUOT_CHAR});
            this.entities.put(APOS_KEY, new char[] {APOS_CHAR});
            this.entities.put(LT_KEY, new char[] {LT_CHAR});
            this.entities.put(GT_KEY, new char[] {GT_CHAR});
        }
    }

    /**
     * Adds a child element.
     *
     * @param child
     *     The child element to add.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code child != null}</li>
     *     <li>{@code child.getName() != null}</li>
     *     <li>{@code child} does not have a parent element</li>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     *     <li>{@linkplain #enumerateChildren} =&gt; old.enumerateChildren()
               + child</li>
     *     <li>getChildren() =&gt; old.enumerateChildren() + child</li>
     * </ul></dd></dl>
     *
     */
    private void addChild(final XMLElement child) {
        this.children.addElement(child);
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name
     *     The name of the attribute.
     * @param value
     *     The value of the attribute.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code name != null}</li>
     *     <li>{@code name} is a valid XML identifier</li>
     *     <li>{@code value != null}</li>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>{@linkplain #enumerateAttributeNames}
     *         =&gt; old.enumerateAttributeNames() + name</li>
     *     <li>{@linkplain #getAttribute(java.lang.String) getAttribute(name)}
     *         =&gt; value</li>
     * </ul></dd></dl>
     */
    private void setAttribute(final String name,
                              final Object value) {
        if (this.ignoreCase) {
            this.attributes.put(name.toUpperCase(), value.toString());
        } else {
            this.attributes.put(name, value.toString());
        }
    }

    /**
     * @return Enumeration of the attribute names.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>{@code result != null}</li>
     * </ul></dd></dl>
     */
    @SuppressWarnings("unchecked")
    public Enumeration enumerateAttributeNames() {
        return new Vector(this.attributes.keySet()).elements();
    }

    /**
     * @return Enumeration the child elements.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>{@code result != null}</li>
     * </ul></dd></dl>
     */
    public Enumeration<XMLElement> enumerateChildren() {
        return this.children.elements();
    }

    /**
     * @return the PCDATA content of the object. If there is no such content,
     * {@code null} is returned.
     */
    public String getContent() {
        return this.contents;
    }

    /**
     * @return an attribute of the element.
     * <p>
     * If the attribute doesn't exist, {@code null} is returned.
     *
     * @param name The name of the attribute.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code name != null}</li>
     *     <li>{@code name} is a valid XML identifier</li>
     * </ul></dd></dl>
     */
    public Object getAttribute(final String name) {
        if (this.ignoreCase) {
            return this.attributes.get(name.toUpperCase());
        } else {
            return this.attributes.get(name);
        }
    }

    /**
     * Returns the name of the element.
     * @return this {@code XMLElement} object's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Reads one XML element from a java.io.Reader and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code reader != null}</li>
     *     <li>{@code reader} is not closed</li>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader</li>
     *     <li>the reader points to the first character following the last
     *         {@code '&gt;'} character of the XML element</li>
     * </ul></dd></dl>
     *
     * @throws java.io.IOException
     *     If an error occurred while reading the input.
     * @throws XMLParseException
     *     If an error occurred while parsing the read data.
     */
    public void parseFromReader(final Reader reader)
            throws IOException, XMLParseException {
        this.charReadTooMuch = '\0';
        this.reader = reader;
        this.parserLineNr = 1;

        for (;;) {
            char ch = this.scanLeadingWhitespace();

            if (ch != '<') {
                throw this.expectedInput("<", ch);
            }

            ch = this.readChar();

            if ((ch == '!') || (ch == '?')) {
                this.skipSpecialTag(0);
            } else {
                this.unreadChar(ch);
                this.scanElement(this);
                return;
            }
        }
    }

    /**
     * Creates a new similar XML element.
     * <p>
     * You should override this method when subclassing XMLElement.
     * </p>
     * @return next element in tree based on global settings
     */
    private XMLElement createAnotherElement() {
        return new XMLElement(this.entities,
                              this.ignoreWhitespace,
                              false,
                              this.ignoreCase);
    }

    /**
     * Changes the content string.
     *
     * @param content
     *     The new content string.
     */
    private void setContent(final String content) {
        this.contents = content;
    }

    /**
     * Changes the name of the element.
     *
     * @param name
     *     The new name.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code name != null}</li>
     *     <li>{@code name} is a valid XML identifier</li>
     * </ul></dd></dl>
     */
    private void setName(final String name) {
        this.name = name;
    }

    /**
     * Scans an identifier from the current reader.
     * The scanned identifier is appended to <code>result</code>.
     *
     * @param result
     *     The buffer in which the scanned identifier will be put.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code result != null}</li>
     *     <li>The next character read from the reader is a valid first
     *         character of an XML identifier.</li>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>The next character read from the reader won't be an identifier
     *         character.</li>
     * </ul></dd></dl>
     * @throws java.io.IOException if something goes wrong
     */
    private void scanIdentifier(final StringBuffer result)
            throws IOException {
        Objects.requireNonNull(result);
        for (;;) {
            char ch = this.readChar();
            if (((ch < 'A') || (ch > 'Z')) && ((ch < 'a') || (ch > 'z'))
                    && ((ch < '0') || (ch > '9')) && (ch != '_') && (ch != '.')
                    && (ch != ':') && (ch != '-') && (ch <= '\u007E')) {
                this.unreadChar(ch);
                return;
            }
            result.append(ch);
        }
    }

    private boolean isRegularWhiteSpace(final char ch) {
        switch (ch) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                return true;
            default:
                return false;
        }
    }
    
    /**
     * This method scans an identifier from the current reader.
     *
     * @return the next character following the whitespace.
     * @throws java.io.IOException if something goes wrong
     */
    private char scanWhitespace()
            throws IOException {
        while(true) {
            final char ch = this.readChar();
            if (!isRegularWhiteSpace(ch)) {
                return ch;
            }
        }
    }
     /**
     * This method scans an leading identifier from the current reader.
     * 
     * Unlike scanWhitespace, it skips also BOM
     *
     * @return the next character following the whitespace.
     * @throws java.io.IOException if something goes wrong
     */
    private char scanLeadingWhitespace()
            throws IOException {
        while(true) {
            final char ch = this.readChar();
            //this is BOM , when used without \\u, appear like space, but causes issues on windows development
            if (ch == '\uFEFF') {
            } else if (!isRegularWhiteSpace(ch)) {
                return ch;
            }
        }
    }

    /**
     * This method scans an identifier from the current reader.
     * <p>
     * The scanned whitespace is appended to {@code result}.
     *
     * @param result where to append scanned text
     * @return the next character following the whitespace.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code result != null}</li>
     * </ul></dd></dl>
     * @throws java.io.IOException if something goes wrong
     */
    private char scanWhitespace(final StringBuffer result)
            throws IOException {
        Objects.requireNonNull(result);
        while (true) {
            final char ch = this.readChar();
            if (!isRegularWhiteSpace(ch)) {
                return ch;
            } else {
                switch (ch) {
                    case ' ':
                    case '\t':
                    case '\n':
                        result.append(ch);
                }
            }
        }
    }

    /**
     * This method scans a delimited string from the current reader.
     * <p>
     * The scanned string without delimiters is appended to {@code string}.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code string != null}</li>
     *     <li>the next char read is the string delimiter</li>
     * </ul></dd></dl>
     * @param string where to append the result
     * @throws java.io.IOException if something goes wrong
     */
    private void scanString(final StringBuffer string)
            throws IOException {
        final char delimiter = this.readChar();
        if ((delimiter != '\'') && (delimiter != '"')) {
            throw this.expectedInput("' or \"");
        }
        for (;;) {
            final char ch = this.readChar();
            if (ch == delimiter) {
                return;
            } else if (ch == '&') {
                this.resolveEntity(string);
            } else {
                string.append(ch);
            }
        }
    }

    /**
     * Scans a {@code #PCDATA} element. CDATA sections and entities are
     * resolved.
     * <p>
     * The next &lt; char is skipped.
     * <p>
     * The scanned data is appended to {@code data}.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code data != null}</li>
     * </ul></dd></dl>
     * @param data where to append data
     * @throws java.io.IOException if something goes wrong
     */
    private void scanPCData(final StringBuffer data)
            throws IOException {
        for (;;) {
            char ch = this.readChar();
            if (ch == '<') {
                ch = this.readChar();
                if (ch == '!') {
                    this.checkCDATA(data);
                } else {
                    this.unreadChar(ch);
                    return;
                }
            } else if (ch == '&') {
                this.resolveEntity(data);
            } else {
                data.append(ch);
            }
        }
    }

    /**
     * Scans a special tag and if the tag is a CDATA section, append its
     * content to {@code buf}.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code buf != null}</li>
     *     <li>The first &lt; has already been read.</li>
     * </ul></dd></dl>
     * @param buf buffer where to append data
     * @return whether the CDATA were ok
     * @throws java.io.IOException if something goes wrong
     */
    private boolean checkCDATA(final StringBuffer buf)
            throws IOException {
        char ch = this.readChar();
        if (ch != '[') {
            this.unreadChar(ch);
            this.skipSpecialTag(0);
            return false;
        } else if (!this.checkLiteral("CDATA[")) {
            this.skipSpecialTag(1); // one [ has already been read
            return false;
        } else {
            int delimiterCharsSkipped = 0;
            while (delimiterCharsSkipped < 3) {
                ch = this.readChar();
                switch (ch) {
                    case ']':
                        if (delimiterCharsSkipped < 2) {
                            delimiterCharsSkipped += 1;
                        } else {
                            buf.append(']');
                            buf.append(']');
                            delimiterCharsSkipped = 0;
                        }
                        break;
                    case '>':
                        if (delimiterCharsSkipped < 2) {
                            for (int i = 0; i < delimiterCharsSkipped; i++) {
                                buf.append(']');
                            }
                            delimiterCharsSkipped = 0;
                            buf.append('>');
                        } else {
                            delimiterCharsSkipped = 3;
                        }
                        break;
                    default:
                        for (int i = 0; i < delimiterCharsSkipped; i += 1) {
                            buf.append(']');
                        }
                        buf.append(ch);
                        delimiterCharsSkipped = 0;
                }
            }
            return true;
        }
    }

    /**
     * Skips a comment.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt;!-- has already been read.</li>
     * </ul></dd></dl>
     * @throws java.io.IOException if something goes wrong
     */
    private void skipComment()
            throws IOException {
        int dashesToRead = 2;
        while (dashesToRead > 0) {
            char ch = this.readChar();
            if (ch == '-') {
                dashesToRead -= 1;
            } else {
                dashesToRead = 2;
            }

            // Be more tolerant of extra -- (double dashes)
            // in comments.
            if (dashesToRead == 0) {
                ch = this.readChar();
                if (ch == '>') {
                    return;
                } else {
                    dashesToRead = 2;
                    this.unreadChar(ch);
                }
            }
        }
        /*
        if (this.readChar() != '>') {
            throw this.expectedInput(">");
        }
        */
    }

    /**
     * Skips a special tag or comment.
     *
     * @param bracketLevel The number of open square brackets ([) that have
     *                     already been read.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt;! has already been read.</li>
     *     <li>{@code bracketLevel &gt;= 0}</li>
     * </ul></dd></dl>
     * @throws java.io.IOException if something goes wrong
     */
    private void skipSpecialTag(final int bracketLevel)
            throws IOException {
        int internalBracketLevel = bracketLevel;
        int tagLevel = 1; // <
        char stringDelimiter = '\0';
        if (internalBracketLevel == 0) {
            char ch = this.readChar();
            if (ch == '[') {
                internalBracketLevel += 1;
            } else if (ch == '-') {
                ch = this.readChar();
                if (ch == '[') {
                    internalBracketLevel += 1;
                } else if (ch == ']') {
                    internalBracketLevel -= 1;
                } else if (ch == '-') {
                    this.skipComment();
                    return;
                }
            }
        }
        while (tagLevel > 0) {
            char ch = this.readChar();
            if (stringDelimiter == '\0') {
                if ((ch == '"') || (ch == '\'')) {
                    stringDelimiter = ch;
                } else if (internalBracketLevel <= 0) {
                    if (ch == '<') {
                        tagLevel += 1;
                    } else if (ch == '>') {
                        tagLevel -= 1;
                    }
                }
                if (ch == '[') {
                    internalBracketLevel += 1;
                } else if (ch == ']') {
                    internalBracketLevel -= 1;
                }
            } else {
                if (ch == stringDelimiter) {
                    stringDelimiter = '\0';
                }
            }
        }
    }

    /**
     * Scans the data for literal text.
     * <p>
     * Scanning stops when a character does not match or after the complete
     * text has been checked, whichever comes first.
     *
     * @param literal the literal to check.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code literal != null}</li>
     * </ul></dd></dl>
     * @return true if literal was ok
     * @throws java.io.IOException  if something goes wrong
     */
    private boolean checkLiteral(final String literal)
            throws IOException {
        final int length = literal.length();
        for (int i = 0; i < length; i += 1) {
            if (this.readChar() != literal.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads a character from a reader.
     * @return the read char
     * @throws java.io.IOException if something goes wrong
     */
    private char readChar()
            throws IOException {
        if (this.charReadTooMuch != '\0') {
            final char ch = this.charReadTooMuch;
            this.charReadTooMuch = '\0';
            return ch;
        } else {
            final int i = this.reader.read();
            if (i < 0) {
                throw this.unexpectedEndOfData();
            } else if (i == 10) {
                this.parserLineNr += 1;
                return '\n';
            } else {
                return (char) i;
            }
        }
    }

    /**
     * Scans an XML element.
     *
     * @param elt The element that will contain the result.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt; has already been read.</li>
     *     <li>{@code elt != null}</li>
     * </ul></dd></dl>
     * @throws java.io.IOException if something goes wrong
     */
    private void scanElement(final XMLElement elt)
            throws IOException {
        final StringBuffer buf = new StringBuffer();
        this.scanIdentifier(buf);
        final String lname = buf.toString();
        elt.setName(lname);
        char ch = this.scanWhitespace();
        while ((ch != '>') && (ch != '/')) {
            buf.setLength(0);
            this.unreadChar(ch);
            this.scanIdentifier(buf);
            final String key = buf.toString();
            ch = this.scanWhitespace();
            if (ch != '=') {
                throw this.expectedInput("=");
            }
            this.unreadChar(this.scanWhitespace());
            buf.setLength(0);
            this.scanString(buf);
            elt.setAttribute(key, buf);
            ch = this.scanWhitespace();
        }
        if (ch == '/') {
            ch = this.readChar();
            if (ch != '>') {
                throw this.expectedInput(">");
            }
            return;
        }
        buf.setLength(0);
        ch = this.scanWhitespace(buf);
        if (ch != '<') {
            this.unreadChar(ch);
            this.scanPCData(buf);
        } else {
            for (;;) {
                ch = this.readChar();
                if (ch == '!') {
                    if (this.checkCDATA(buf)) {
                        this.scanPCData(buf);
                        break;
                    } else {
                        ch = this.scanWhitespace(buf);
                        if (ch != '<') {
                            this.unreadChar(ch);
                            this.scanPCData(buf);
                            break;
                        }
                    }
                } else {
                    buf.setLength(0);
                    break;
                }
            }
        }
        if (buf.length() == 0) {
            while (ch != '/') {
                if (ch == '!') {
                    ch = this.readChar();
                    if (ch != '-') {
                        throw this.expectedInput("Comment or Element");
                    }
                    ch = this.readChar();
                    if (ch != '-') {
                        throw this.expectedInput("Comment or Element");
                    }
                    this.skipComment();
                } else {
                    this.unreadChar(ch);
                    final XMLElement child = this.createAnotherElement();
                    this.scanElement(child);
                    elt.addChild(child);
                }
                ch = this.scanWhitespace();
                if (ch != '<') {
                    throw this.expectedInput("<");
                }
                ch = this.readChar();
            }
            this.unreadChar(ch);
        } else {
            if (this.ignoreWhitespace) {
                elt.setContent(buf.toString().trim());
            } else {
                elt.setContent(buf.toString());
            }
        }
        ch = this.readChar();
        if (ch != '/') {
            throw this.expectedInput("/");
        }
        this.unreadChar(this.scanWhitespace());
        if (!this.checkLiteral(lname)) {
            throw this.expectedInput(lname);
        }
        if (this.scanWhitespace() != '>') {
            throw this.expectedInput(">");
        }
    }

    /**
     * Resolves an entity. The name of the entity is read from the reader.
     * <p>
     * The value of the entity is appended to {@code buf}.
     *
     * @param buf Where to put the entity value.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &amp; has already been read.</li>
     *     <li>{@code buf != null}</li>
     * </ul></dd></dl>
     * @throws java.io.IOException if something goes wrong
     */
    private void resolveEntity(final StringBuffer buf)
            throws IOException {
        char ch = '\0';
        final StringBuilder keyBuf = new StringBuilder();
        for (;;) {
            ch = this.readChar();
            if (ch == ';') {
                break;
            }
            keyBuf.append(ch);
        }
        final String key = keyBuf.toString();
        if (key.charAt(0) == '#') {
            try {
                if (key.charAt(1) == 'x') {
                    ch = (char) Integer.parseInt(key.substring(2), 16);
                } else {
                    ch = (char) Integer.parseInt(key.substring(1), 10);
                }
            } catch (NumberFormatException e) {
                throw this.unknownEntity(key);
            }
            buf.append(ch);
        } else {
            char[] value = entities.get(key);
            if (value == null) {
                throw this.unknownEntity(key);
            }
            buf.append(value);
        }
    }

    /**
     * Pushes a character back to the read-back buffer.
     *
     * @param ch The character to push back.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The read-back buffer is empty.</li>
     *     <li>{@code ch != '\0'}</li>
     * </ul></dd></dl>
     */
    private void unreadChar(final char ch) {
        this.charReadTooMuch = ch;
    }

    /**
     * Creates a parse exception for when the end of the data input has been
     * reached.
     * @return  exception to be used
     */
    private XMLParseException unexpectedEndOfData() {
        final String msg = "Unexpected end of data reached";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }


    /**
     * Creates a parse exception for when the next character read is not
     * the character that was expected.
     *
     * @param charSet The set of characters (in human readable form) that was
     *                expected.
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code charSet != null}</li>
     *     <li>{@code charSet.length() &gt; 0}</li>
     * </ul></dd></dl>
     * @return exception to be used
     */
    private XMLParseException expectedInput(final String charSet) {
        final String msg = "Expected: " + charSet;
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when the next character read is not
     * the character that was expected.
     *
     * @param charSet The set of characters (in human readable form) that was
     *                expected.
     * @param ch The character that was received instead.
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code charSet != null}</li>
     *     <li>{@code charSet.length() &gt; 0}</li>
     * </ul></dd></dl>
     * @return exception to be used
     */
    private XMLParseException expectedInput(final String charSet, final char ch) {
        final String msg = "Expected: '" + charSet + "'" + " but got: '" + ch + "'";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when an entity could not be resolved.
     *
     * @param name The name of the entity.
     * @return exception to be used
     *
     * <dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>{@code name != null}</li>
     *     <li>{@code name.length() &gt; 0}</li>
     * </ul></dd></dl>
     */
    private XMLParseException unknownEntity(final String name) {
        final String msg = "Unknown or invalid entity: &" + name + ";";
        return new XMLParseException(this.getName(), this.parserLineNr, msg);
    }
}
