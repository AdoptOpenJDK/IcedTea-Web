// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.xmlparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * This Reader will examine the wrapped input stream according to
 * appendix F of the XML specification in order to guess the encoding
 * of the XML content in the stream.
 * <p/>
 * If the encoding cannot be guessed the reader falls back to the {@code defaultEncoding}.
 * <p/>
 * The following aspects of the input stream are examined in the order below
 * <ol>
 * <li>Byte order mark (BOM)</li>
 * <li>First 20 byes to see if content starts with "&lt;?xml"</li>
 * <li>XML encoding declaration</li>
 * <li>Default encoding</li>
 * </ol>
 * <p/>
 * For details see:<br/>
 * <a href="https://www.w3.org/TR/xml/#sec-guessing">XML - Appendix F</a>
 */
public class XmlStreamReader extends Reader {

    // '\uFEFF' (byte order marker) byte arrays for detecting encodings

    static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    static final byte[] UTF16LE_BOM = {(byte) 0xFF, (byte) 0xFE};
    static final byte[] UTF16BE_BOM = {(byte) 0xFE, (byte) 0xFF};

    static final byte[] UTF32LE_BOM = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
    static final byte[] UTF32BE_BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};

    static final byte[] BROKEN_UTF32LE_BOM = {(byte) 0xFE, (byte) 0xFF, (byte) 0x00, (byte) 0x00};
    static final byte[] BROKEN_UTF32BE_BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFE};

    // "<?xml" byte arrays for detecting encodings
    //      < -- 0x3C
    //      ? -- 0x3F
    //      x -- 0x78
    //      m -- 0x6D
    //      l -- 0x6C

    private static final byte[] UTF8_FIRST_CHARS = {(byte) 0x3C, (byte) 0x3F, (byte) 0x78, (byte) 0x6D, (byte) 0x6C};

    private static final byte[] UTF16LE_FIRST_CHARS = {
            (byte) 0x3C, (byte) 0x00, (byte) 0x3F, (byte) 0x00, (byte) 0x78, (byte) 0x00,
            (byte) 0x6D, (byte) 0x00, (byte) 0x6C, (byte) 0x00};
    private static final byte[] UTF16BE_FIRST_CHARS = {
            (byte) 0x00, (byte) 0x3C, (byte) 0x00, (byte) 0x3F, (byte) 0x00, (byte) 0x78,
            (byte) 0x00, (byte) 0x6D, (byte) 0x00, (byte) 0x6C};

    private static final byte[] UTF32LE_FIRST_CHARS = {
            (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x3F, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x78, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x6D, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x6C, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static final byte[] UTF32BE_FIRST_CHARS = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3C,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3F,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x78,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6D,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6C};

    private static final byte[] BROKEN_UTF32LE_FIRST_CHARS = {
            (byte) 0x00, (byte) 0x3C, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x3F, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x78, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x6D, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x6C, (byte) 0x00, (byte) 0x00};
    private static final byte[] BROKEN_UTF32BE_FIRST_CHARS = {
            (byte) 0x00, (byte) 0x00, (byte) 0x3C, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x3F, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x78, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x6D, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x6C, (byte) 0x00};

    // names of encodings not found in java.nio.StandardCharsets

    static final String UTF32LE_NAME = "UTF-32LE";
    static final String UTF32BE_NAME = "UTF-32BE";

    // constants

    private static final int MAX_CHARS = 80;
    private static final int BUFFER_SIZE = MAX_CHARS * 4;

    // once the encoding has been detected all reading is delegated to this input stream reader.
    private final InputStreamReader delegate;

    /**
     * Constructor UTF-8 default encoding.
     * This constructor is equivalent to calling:<br/>
     * {@code new XmlStreamReader(in, StandardCharsets.UTF_8)}
     *
     * @param in an input stream with XML content.
     */
    public XmlStreamReader(final InputStream in) throws IOException {
        this(in, StandardCharsets.UTF_8);
    }

    /**
     * Constructor with passed in default encoding.
     *
     * @param in              an input stream with XML content.
     * @param defaultEncoding the encoding to use if no encoding
     *                        can be derived from the content of the stream
     */
    public XmlStreamReader(final InputStream in, final Charset defaultEncoding) throws IOException {
        requireNonNull(in);
        requireNonNull(defaultEncoding);

        final PushbackInputStream pin = new PushbackInputStream(in, BUFFER_SIZE);

        final Optional<Charset> fromBom = detectFromBom(pin);
        final Optional<Charset> fromXML = detectFromXml(pin);

        final Charset guessedEncoding = fromBom.orElseGet(() -> fromXML.orElse(defaultEncoding));

        final Optional<Charset> fromXmlTag = readOutOfXmlTag(pin, guessedEncoding);
        final Charset encoding = fromXmlTag.orElse(guessedEncoding);

        delegate = new InputStreamReader(pin, encoding);
    }

    /**
     * Reads the first 4 bytes of the input stream and detects any unicode byte order mark (BOM).
     * If a mark is detected the corresponding {@link Charset} is returned.
     * Any detected BOM is removed from the stream leaving only the content without the BOM for further processing.
     *
     * @param pin the input stream
     * @return the detected character set or empty if no BOM was found
     * @throws IOException if a unsupported BOM was detected.
     */
    private Optional<Charset> detectFromBom(final PushbackInputStream pin) throws IOException {

        final byte[] potentialBom = new byte[4];
        final int read = pin.read(potentialBom);

        if (read < 0) {
            // nothing read
            return empty();
        }

        if (startsWith(potentialBom, read, BROKEN_UTF32LE_BOM)) {
            throw new UnsupportedCharsetException("UTF-32LE - unusual ordered BOM");
        }
        if (startsWith(potentialBom, read, BROKEN_UTF32BE_BOM)) {
            throw new UnsupportedCharsetException("UTF-32BE - unusual ordered BOM");
        }
        if (startsWith(potentialBom, read, UTF32BE_BOM)) {
            return Optional.of(Charset.forName(UTF32BE_NAME));
        }
        if (startsWith(potentialBom, read, UTF32LE_BOM)) {
            return Optional.of(Charset.forName(UTF32LE_NAME));
        }
        if (startsWith(potentialBom, read, UTF8_BOM)) {
            pin.unread(potentialBom, 3, read - 3);
            return Optional.of(StandardCharsets.UTF_8);
        }
        if (startsWith(potentialBom, read, UTF16LE_BOM)) {
            pin.unread(potentialBom, 2, read - 2);
            return Optional.of(StandardCharsets.UTF_16LE);
        }
        if (startsWith(potentialBom, read, UTF16BE_BOM)) {
            pin.unread(potentialBom, 2, read - 2);
            return Optional.of(StandardCharsets.UTF_16BE);
        }

        pin.unread(potentialBom, 0, read);
        return empty();
    }


    /**
     * Reads the first 20 bytes of the input stream and detects if the bytes resemble the characters
     * {@code "<?xml"} in a unicode encoding.
     * If a match is found the corresponding {@link Charset} is returned.
     * The content in the stream is left unchanged for further processing.
     *
     * @param pin the input stream
     * @return the detected character set or empty if no {@code "<?xml"} was found
     * @throws IOException if a unsupported BOM was detected.
     */
    private Optional<Charset> detectFromXml(final PushbackInputStream pin) throws IOException {
        final byte[] firstChars = new byte[20];
        final int read = pin.read(firstChars);

        if (read < 0) {
            // nothing read
            return empty();
        }

        pin.unread(firstChars, 0, read);

        if (startsWith(firstChars, read, BROKEN_UTF32LE_FIRST_CHARS)) {
            throw new UnsupportedCharsetException("UTF-32LE - unusual ordered");
        }
        if (startsWith(firstChars, read, BROKEN_UTF32BE_FIRST_CHARS)) {
            throw new UnsupportedCharsetException("UTF-32BE - unusual ordered");
        }
        if (startsWith(firstChars, read, UTF32BE_FIRST_CHARS)) {
            return Optional.of(Charset.forName(UTF32BE_NAME));
        }
        if (startsWith(firstChars, read, UTF32LE_FIRST_CHARS)) {
            return Optional.of(Charset.forName(UTF32LE_NAME));
        }
        if (startsWith(firstChars, read, UTF8_FIRST_CHARS)) {
            return Optional.of(StandardCharsets.UTF_8);
        }
        if (startsWith(firstChars, read, UTF16LE_FIRST_CHARS)) {
            return Optional.of(StandardCharsets.UTF_16LE);
        }
        if (startsWith(firstChars, read, UTF16BE_FIRST_CHARS)) {
            return Optional.of(StandardCharsets.UTF_16BE);
        }

        return empty();
    }

    /**
     * Detects if a given byte array starts with a prefix of bytes.
     *
     * @param bytes  the bytes to search for the prefix
     * @param len    the number of bytes in {@code bytes} which are filled
     * @param prefix the prefix to search for
     * @return true iff {@code len} is greater or equal to {@code prefix.length}
     * and the {@code prefix} is a prefix of {@code bytes}
     */
    private boolean startsWith(final byte[] bytes, final int len, final byte[] prefix) {
        if (len < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to read the encoding from the encoding tag in an XML (<?xml version="1.0" encoding="UTF-8"?>).
     * <p/>
     * Reads the first {@code BUFFER_SIZE} bytes and converts it to a string using the {@code guessedEncoding}.
     * The resulting string is searched for an XML encoding tag. If found the corresponding character set is returned.
     * The content in the stream is left unchanged for further processing.
     *
     * @param pin             the input stream
     * @param guessedEncoding the encoding guessed by analyzing BOM and the first 20 bytes.
     * @return the encoding found in the XML tag or empty
     * @throws IOException if reading from the stream failed
     */
    private Optional<Charset> readOutOfXmlTag(final PushbackInputStream pin, final Charset guessedEncoding) throws IOException {
        final byte[] firstChars = new byte[BUFFER_SIZE];
        final int read = pin.read(firstChars);

        if (read < 0) {
            // nothing read
            return empty();
        }

        pin.unread(firstChars, 0, read);

        final String beginningOfXml = new String(firstChars, guessedEncoding);
        final int endStartTag = beginningOfXml.indexOf("?>");
        if (beginningOfXml.startsWith("<?xml") && endStartTag > 0) {
            final String startTag = beginningOfXml.substring(0, endStartTag + 2);
            final int beginOfEncoding = startTag.indexOf("encoding=\"");
            if (beginOfEncoding > 0) {
                final int endOfEncoding = startTag.indexOf('"', beginOfEncoding + 10);
                if (endOfEncoding > 0) {
                    final String encoding = startTag.substring(beginOfEncoding + 10, endOfEncoding);
                    return Optional.of(Charset.forName(encoding));
                }
            }
        }

        return empty();
    }

    /**
     * Returns the name of the character encoding being used by this stream.
     *
     * <p> If the encoding has an historical name then that name is returned;
     * otherwise the encoding's canonical name is returned.
     *
     * <p> If this instance was created with the {@link
     * #XmlStreamReader(InputStream, Charset)} constructor then the returned
     * name, being unique for the encoding, may differ from the name passed to
     * the constructor. This method will return <code>null</code> if the
     * stream has been closed.
     * </p>
     *
     * @return The historical name of this encoding, or
     * <code>null</code> if the stream has been closed
     * @see java.nio.charset.Charset
     */
    public String getEncoding() {
        return delegate.getEncoding();
    }

    @Override
    public boolean ready() throws IOException {
        return delegate.ready();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return delegate.read(cbuf, off, len);
    }


    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
