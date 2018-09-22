// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Edigeo record.
 */
final class EdigeoRecord {

    enum Nature {
        RESERVED('T'),
        SIMPLE('S'),
        COMPOSED('C');

        final char code;
        Nature(char code) {
            this.code = code;
        }

        static Nature of(char c) {
            for (Nature n : values()) {
                if (c == n.code) {
                    return n;
                }
            }
            throw new IllegalArgumentException(Character.toString(c));
        }
    }

    enum Format {
        STRING('A'),
        BINARY('B'),
        COORDINATES('C'),
        DATE('D'),
        REAL_WITH_EXP('E'),
        SIGNED('I'),
        UNSIGNED('N'),
        DESCRIPTOR_REF('P'),
        REAL_WITHOUT_EXP('R'),
        TEXT('T'),
        RESERVED(' ');

        final char code;
        Format(char code) {
            this.code = code;
        }

        static Format of(char c) {
            for (Format f : values()) {
                if (c == f.code) {
                    return f;
                }
            }
            throw new IllegalArgumentException(Character.toString(c));
        }
    }

    final String name;
    final Nature nature;
    final Format format;
    final int length;
    final List<String> values;

    EdigeoRecord(String line) {
        name = line.substring(0, 3);
        assert 'A' <= name.charAt(0) && name.charAt(0) <= 'Z' : line;
        nature = Nature.of(line.charAt(3));
        format = Format.of(line.charAt(4));
        assert nature != Nature.RESERVED || format == Format.RESERVED : line;
        length = Integer.parseUnsignedInt(line.substring(5, 7));
        assert line.charAt(7) == ':' : line;
        if (line.length() > 8) {
            assert line.length() <= 80;
            values = Arrays.asList(line.substring(8).split(";"));
            assert nature == Nature.RESERVED
                    || (nature == Nature.SIMPLE && values.size() == 1)
                    || (nature == Nature.COMPOSED && values.size() > 1) : line;
        } else {
            values = Collections.emptyList();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, length, nature, name, values);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        EdigeoRecord other = (EdigeoRecord) obj;
        return format == other.format
            && length == other.length
            && nature == other.nature
            && Objects.equals(name, other.name)
            && Objects.equals(values, other.values);
    }

    @Override
    public String toString() {
        return "EdigeoRecord [name=" + name + ", nature=" + nature + ", format=" + format + ", length=" + length
                + ", values=" + values + ']';
    }
}
