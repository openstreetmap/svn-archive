/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.util;

import java.util.HashMap;
import java.util.Map;

import org.jopendocument.util.StringUtils.Escaper;

public final class FileUtils {

    private FileUtils() {
        // all static
    }

    // **io

    private static final Map<String, String> ext2mime;
    static {
        ext2mime = new HashMap<String, String>();
        ext2mime.put(".xml", "text/xml");
        ext2mime.put(".jpg", "image/jpeg");
        ext2mime.put(".png", "image/png");
        ext2mime.put(".tiff", "image/tiff");
    }

    /**
     * Try to guess the media type of the passed file name (see <a
     * href="http://www.iana.org/assignments/media-types">iana</a>).
     * 
     * @param fname a file name.
     * @return its mime type.
     */
    public static final String findMimeType(String fname) {
        for (final Map.Entry<String, String> e : ext2mime.entrySet()) {
            if (fname.toLowerCase().endsWith(e.getKey()))
                return e.getValue();
        }
        return null;
    }

    /**
     * An escaper suitable for producing valid filenames.
     */
    public static final Escaper FILENAME_ESCAPER = new StringUtils.Escaper('\'', 'Q');
    static {
        // from windows explorer
        FILENAME_ESCAPER.add('"', 'D').add(':', 'C').add('/', 'S').add('\\', 'A');
        FILENAME_ESCAPER.add('<', 'L').add('>', 'G').add('*', 'R').add('|', 'P').add('?', 'M');
    }
}
