// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Partial list of Edigeo charsets according to NF Z 52000 Annex C, table C.1.
 * ISO 6937 (JEC) is not supported by Java.
 */
enum EdigeoCharset {
    ISO_646_IRV("IRV", StandardCharsets.US_ASCII),
    ISO_646_FRANCE("646-FRANCE", StandardCharsets.ISO_8859_1),
    ISO_8859_1("8859-1", StandardCharsets.ISO_8859_1),
    ISO_8859_2("8859-2", Charset.forName("ISO-8859-2")),
    ISO_8859_3("8859-3", Charset.forName("ISO-8859-3")),
    ISO_8859_4("8859-4", Charset.forName("ISO-8859-4")),
    ISO_8859_5("8859-5", Charset.forName("ISO-8859-5")),
    ISO_8859_6("8859-6", Charset.forName("ISO-8859-6")),
    ISO_8859_7("8859-7", Charset.forName("ISO-8859-7")),
    ISO_8859_8("8859-8", Charset.forName("ISO-8859-8")),
    ISO_8859_9("8859-9", Charset.forName("ISO-8859-9"));
    //ISO_6937_JEC("JEC");

    final String zv;
    final Charset cs;

    EdigeoCharset(String zv, Charset cs) {
        this.zv = Objects.requireNonNull(zv, "zv");
        this.cs = Objects.requireNonNull(cs, "cs");
    }

    static EdigeoCharset of(String zv) {
        for (EdigeoCharset e : values()) {
            if (e.zv.equals(zv)) {
                return e;
            }
        }
        throw new IllegalArgumentException(zv);
    }
}
