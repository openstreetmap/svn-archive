// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadData;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileVEC.ObjectBlock;

/**
 * Post-processor mapping Edigeo objects to OSM tagged objects.
 */
public class EdigeoPostProcessor {

    final Predicate<ObjectBlock> predicate;
    final BiPredicate<CadastreDownloadData, OsmPrimitive> filter;
    final BiConsumer<ObjectBlock, OsmPrimitive> consumer;

    /**
     * Constructs a new {@code EdigeoPostProcessor}.
     */
    public EdigeoPostProcessor(
            Predicate<ObjectBlock> predicate,
            BiPredicate<CadastreDownloadData, OsmPrimitive> filter,
            BiConsumer<ObjectBlock, OsmPrimitive> consumer) {
        this.predicate = Objects.requireNonNull(predicate);
        this.filter = Objects.requireNonNull(filter);
        this.consumer = Objects.requireNonNull(consumer);
    }
}
