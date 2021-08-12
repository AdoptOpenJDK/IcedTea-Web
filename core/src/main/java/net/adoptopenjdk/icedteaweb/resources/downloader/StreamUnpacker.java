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

package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CURRENT_VERSION_ID_QUERY_PARAM;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.JAR_DIFF_MIME_TYPE;

/**
 * Allows to unpack an input stream.
 */
interface StreamUnpacker {

    Logger LOG = LoggerFactory.getLogger(StreamUnpacker.class);

    String GZIP_ENCODING = "gzip";

    String PACK_200_GZIP_ENCODING = "pack200-gzip";

    String PACK_GZ_EXTENSION = ".pack.gz";

    static StreamUnpacker getCompressionUnpacker(final DownloadDetails downloadDetails) {
        final URL downloadFrom = downloadDetails.downloadFrom;
        final String contentEncoding = downloadDetails.contentEncoding;
        final boolean packgz = PACK_200_GZIP_ENCODING.equals(contentEncoding) || downloadFrom.getPath().endsWith(PACK_GZ_EXTENSION);
        final boolean gzip = GZIP_ENCODING.equals(contentEncoding);

        // It's important to check packgz first. If a stream is both
        // pack200 and gz encoded, then con.getContentEncoding() could
        // return ".gz", so if we check gzip first, we would end up
        // treating a pack200 file as a jar file.
        if (packgz) {
            LOG.debug("Will use Pack200 for '{}'", downloadDetails.downloadFrom);
            return new PackGzipUnpacker();
        } else if (gzip) {
            LOG.debug("Will use GZIP for '{}'", downloadDetails.downloadFrom);
            return new GzipUnpacker();
        }

        LOG.debug("Will use no compression-unpacker for '{}'", downloadDetails.downloadFrom);
        return new NotUnpacker();
    }

    static StreamUnpacker getContentUnpacker(final DownloadDetails downloadDetails, final URL resourceHref) {
        if (downloadDetails.contentType != null && downloadDetails.contentType.startsWith(JAR_DIFF_MIME_TYPE)) {
            final Map<String, String> querryParams = Optional.ofNullable(downloadDetails.downloadFrom.getQuery())
                    .map(query -> Stream.of(query.split(Pattern.quote("&"))))
                    .map(stream -> stream.collect(Collectors.toMap(e -> e.split("=")[0], e -> e.split("=")[1])))
                    .orElseGet(Collections::emptyMap);

            final VersionId currentVersionId = Optional.ofNullable(querryParams.get(CURRENT_VERSION_ID_QUERY_PARAM))
                    .map(VersionId::fromString)
                    .orElseThrow(() -> new IllegalArgumentException("Mime-Type " + JAR_DIFF_MIME_TYPE + " for non incremental request to " + downloadDetails.downloadFrom));

            final File cacheFile = Cache.getOrCreateCacheFile(resourceHref, currentVersionId);
            LOG.debug("Will use JarDiff for '{}'", resourceHref);
            return new JarDiffUnpacker(cacheFile);
        }

        LOG.debug("Will use no content-unpacker for '{}'", resourceHref);
        return new NotUnpacker();
    }

    /**
     * Unpacks the content of the input stream.
     * Provides a new input stream with the unpacked content.
     *
     * @param input a compressed input stream
     * @return an unpacked input stream
     * @throws IOException if anything goes wrong
     */
    InputStream unpack(InputStream input) throws IOException;
}
