// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadData;
import org.openstreetmap.josm.tools.Logging;

/**
 * Superclass of all Edigeo files.
 */
abstract class EdigeoFile {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Block descriptor.
     */
    static class Block {
        /** RTY */ final String type;
        /** RID */ String identifier = "";

        // Remembers the last string read, to handle multiline text (more than 80 chars) with "NEXT" keyword
        private Consumer<String> lastReadString;

        Block(String type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        public final String getBlockType() {
            return type;
        }

        public final String getBlockIdentifier() {
            return identifier;
        }

        void processRecord(EdigeoRecord r) {
            if ("RID".equals(r.name)) {
                safeGet(r, s -> identifier += s);
            } else if ("NEX".equals(r.name) && lastReadString != null) {
                safeGet(r, lastReadString);
            } else {
                throw new IllegalArgumentException(r.toString());
            }
        }

        static boolean areNotNull(Object... objects) {
            return Arrays.stream(objects).noneMatch(o -> o == null);
        }

        static boolean areNotEmpty(String... strings) {
            return areNotNull((Object[]) strings) && Arrays.stream(strings).noneMatch(String::isEmpty);
        }

        static boolean areSameSize(int size, Collection<?>... collections) {
            return areNotNull((Object[]) collections) && Arrays.stream(collections).allMatch(c -> c.size() == size);
        }

        boolean isValid() {
            return type.length() == 3 && areNotEmpty(identifier);
        }

        void resolvePhase1() {
            // To be overriden if relevant (before relations are resolved)
        }

        void resolvePhase2() {
            // To be overriden if relevant (after relations are resolved)
        }

        protected final void safeGet(EdigeoRecord r, List<String> list) {
            list.add("");
            safeGet(r, s -> {
                int idx = list.size() - 1;
                list.set(idx, list.get(idx) + s);
            });
        }

        protected final void safeGet(EdigeoRecord r, Consumer<String> callback) {
            (lastReadString = callback).accept(r.length > 0 ? r.values.get(0) : null);
        }

        protected final char safeGetChar(EdigeoRecord r) {
            return r.length > 0 ? r.values.get(0).charAt(0) : 0;
        }

        protected final int safeGetInt(EdigeoRecord r) {
            return r.length > 0 ? Integer.parseInt(r.values.get(0)) : 0;
        }

        protected final LocalDate safeGetDate(EdigeoRecord r) {
            return r.length > 0 ? LocalDate.parse(r.values.get(0), dateFormatter) : null;
        }

        protected final double safeGetDouble(EdigeoRecord r) {
            return r.length > 0 ? Double.parseDouble(r.values.get(0)) : 0;
        }

        protected final EastNorth safeGetEastNorth(EdigeoRecord r) {
            return r.length > 0 ? new EastNorth(Double.parseDouble(r.values.get(0)),
                                                Double.parseDouble(r.values.get(1))) : null;
        }

        protected final void safeGetAndLog(EdigeoRecord r, Consumer<String> callback, String msg) {
            if (r.length > 0) {
                String v = r.values.get(0);
                Logging.info(msg + ": " + v);
                (lastReadString = callback).accept(v);
            }
        }

        protected final LocalDate safeGetDateAndLog(EdigeoRecord r, String msg) {
            if (r.length > 0) {
                LocalDate v = LocalDate.parse(r.values.get(0), dateFormatter);
                Logging.info(msg + ": " + v);
                return v;
            }
            return null;
        }
    }

    protected final Path path;
    private boolean bomFound;
    private boolean eomFound;
    EdigeoCharset charset;
    private Block currentBlock;

    EdigeoFile(Path path) {
        this.path = path;
    }

    public EdigeoFile read() throws IOException, ReflectiveOperationException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    // Read record
                    EdigeoRecord r = new EdigeoRecord(line);
                    // Process begin of file
                    if (!bomFound) {
                        bomFound = "BOM".equals(r.name);
                        if (!bomFound) {
                            throw new IOException("Unexpected first record: " + r);
                        } else {
                            assert r.length == 12 && r.values.size() == 1 : r;
                            continue;
                        }
                    }
                    // Process charset
                    if (charset == null) {
                        if (!"CSE".equals(r.name)) {
                            throw new IOException("Unexpected record instead of charset: " + r);
                        } else {
                            assert r.values.size() == 1 : r;
                            charset = EdigeoCharset.of(r.values.get(0));
                            continue;
                        }
                    }
                    // Process other records & end of file
                    if (eomFound) {
                        throw new IOException("Unexpected record after end of file: " + r);
                    }
                    eomFound = "EOM".equals(r.name);
                    if (!eomFound) {
                        processRecord(r);
                    } else {
                        assert r.length == 0 && r.values.isEmpty() : r;
                    }
                }
            }
        }
        return this;
    }

    protected abstract Block createBlock(String type) throws ReflectiveOperationException;

    protected static <T extends Block> T addBlock(List<T> blocks, T block) {
        blocks.add(block);
        return block;
    }

    private void processRecord(EdigeoRecord r) throws ReflectiveOperationException {
        if ("RTY".equals(r.name)) {
            currentBlock = Objects.requireNonNull(createBlock(r.values.get(0)), r.toString());
            return;
        }

        if (currentBlock == null) {
            throw new IllegalStateException(r.toString());
        }

        currentBlock.processRecord(r);
    }

    abstract boolean isValid();

    void resolve() {
        // To be overriden if relevant
    }

    EdigeoFile fill(DataSet ds, CadastreDownloadData data) {
        // To be overriden if relevant
        return this;
    }
}
