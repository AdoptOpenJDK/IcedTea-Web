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

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.BROKEN_UTF32BE_BOM;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.BROKEN_UTF32LE_BOM;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF16BE_BOM;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF16LE_BOM;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF32BE_BOM;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF32BE_NAME;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF32LE_BOM;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF32LE_NAME;
import static net.adoptopenjdk.icedteaweb.xmlparser.XmlStreamReader.UTF8_BOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Test for {@link XmlStreamReader}.
 */
public class XmlStreamReaderTest {

    private static final String BOM = Character.toString('\uFEFF');
    private static final InputStream EMPTY_STREAM = streamOf(new byte[0]);
    private static final String XML_ENCODING_TAG = "<?xml version=\"1.0\" encoding=\"{ENCODING}\" ?>";
    private static final Charset ISO_8859_15 = Charset.forName("ISO-8859-15");

    private static final List<CharsetAndBom> CHARSET_AND_BOMS = unmodifiableList(asList(
            new CharsetAndBom(StandardCharsets.UTF_8, UTF8_BOM),
            new CharsetAndBom(StandardCharsets.UTF_16LE, UTF16LE_BOM),
            new CharsetAndBom(StandardCharsets.UTF_16BE, UTF16BE_BOM),
            new CharsetAndBom(Charset.forName(UTF32LE_NAME), UTF32LE_BOM),
            new CharsetAndBom(Charset.forName(UTF32BE_NAME), UTF32BE_BOM)
    ));

    @Test
    public void constructorThrowsOnNullArgument() {
        assertThrows(NullPointerException.class, () ->
                new XmlStreamReader(null)
        );
        assertThrows(NullPointerException.class, () ->
                new XmlStreamReader(null, defaultCharset())
        );
        assertThrows(NullPointerException.class, () ->
                new XmlStreamReader(EMPTY_STREAM, null)
        );
    }

    @Test
    public void singleArgumentConstructorUsesUtf8() throws IOException {
        // when
        final XmlStreamReader reader = new XmlStreamReader(EMPTY_STREAM);

        // then
        assertReaderHasExpectedEncoding(reader, UTF_8);
    }

    @Test
    public void doubleArgumentConstructorUsesPassedCharset() throws IOException {
        // when
        final XmlStreamReader reader = new XmlStreamReader(EMPTY_STREAM, ISO_8859_1);

        // then
        assertReaderHasExpectedEncoding(reader, ISO_8859_1);
    }

    @Test
    public void detectEncodingFromBom() throws IOException {
        for (CharsetAndBom candidate : CHARSET_AND_BOMS) {
            // when
            final XmlStreamReader reader = new XmlStreamReader(streamOf(candidate.bom), ISO_8859_1);

            // then
            assertReaderHasExpectedEncoding(reader, candidate.charset);
        }
    }

    @Test
    public void throwsExceptionForUnsupportedEncodings() {
        assertThrows(UnsupportedCharsetException.class, () ->
                new XmlStreamReader(streamOf(BROKEN_UTF32LE_BOM))
        );
        assertThrows(UnsupportedCharsetException.class, () ->
                new XmlStreamReader(streamOf(BROKEN_UTF32BE_BOM))
        );
    }

    @Test
    public void ensureOnlyBomIsRemovedFromStream() throws IOException {
        for (CharsetAndBom candidate : CHARSET_AND_BOMS) {
            // given
            final String content = "abc";
            final XmlStreamReader reader = new XmlStreamReader(streamOf(candidate.bom, content, candidate.charset));

            // when
            final String line = new BufferedReader(reader).readLine();

            // then
            assertThat(line, is(content));
        }
    }

    @Test
    public void bomBytesCorrelatesToEncodings() {
        for (CharsetAndBom candidate : CHARSET_AND_BOMS) {
            assertThat(candidate.bom, is(BOM.getBytes(candidate.charset)));
        }
    }

    @Test
    public void detectEncodingFromXml() throws IOException {
        for (CharsetAndBom candidate : CHARSET_AND_BOMS) {
            // when
            final XmlStreamReader reader = new XmlStreamReader(streamOf(XML_ENCODING_TAG, candidate.charset), ISO_8859_1);

            // then
            assertReaderHasExpectedEncoding(reader, candidate.charset);
        }
    }

    @Test
    public void detectEncodingFromXmlTag() throws IOException {
        // when
        final XmlStreamReader reader = new XmlStreamReader(streamOf(XML_ENCODING_TAG, ISO_8859_15), ISO_8859_1);

        // then
        assertReaderHasExpectedEncoding(reader, ISO_8859_15);
    }


    private void assertReaderHasExpectedEncoding(final XmlStreamReader reader, final Charset charset) {
        final Set<String> aliases = new HashSet<>(charset.aliases());
        aliases.add(charset.name());
        assertThat(aliases, hasItem(reader.getEncoding()));
    }

    private static ByteArrayInputStream streamOf(final byte[] content) {
        return new ByteArrayInputStream(content);
    }

    private static ByteArrayInputStream streamOf(final String content, final Charset encoding) {
        return new ByteArrayInputStream(content.replace("{ENCODING}", encoding.name()).getBytes(encoding));
    }

    private static ByteArrayInputStream streamOf(final byte[] bom, final String content, final Charset encoding) {
        final byte[] contentBytesOnly = content.replace("{ENCODING}", encoding.name()).getBytes(encoding);
        final byte[] contentBytes = new byte[bom.length + contentBytesOnly.length];
        System.arraycopy(bom, 0, contentBytes, 0, bom.length);
        System.arraycopy(contentBytesOnly, 0, contentBytes, bom.length, contentBytesOnly.length);
        return new ByteArrayInputStream(contentBytes);
    }

    private void assertThrows(final Class<? extends Exception> expectedException, final ThrowingRunnable r) {
        try {
            r.execute();
            fail("expected a " + expectedException.getName() + " to be thrown");
        } catch (Exception e) {
            if (!expectedException.isInstance(e)) {
                fail("expected a " + expectedException.getName() + " to be thrown but " + e.getClass().getName() + " was thrown");
            }
        }
    }

    private interface ThrowingRunnable {
        void execute() throws Exception;
    }

    private static class CharsetAndBom {
        private final Charset charset;
        private final byte[] bom;

        private CharsetAndBom(final Charset charset, final byte[] bom) {
            this.charset = charset;
            this.bom = bom;
        }
    }
}