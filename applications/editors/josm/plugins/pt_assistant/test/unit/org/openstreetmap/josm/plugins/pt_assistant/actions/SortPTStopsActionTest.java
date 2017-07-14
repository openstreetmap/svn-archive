// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests for class {@link SortPTStopsAction}.
 *
 * @author giack
 *
 */
public class SortPTStopsActionTest extends AbstractTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().platform();

    private DataSet ds;

    @Before
    public void init() throws FileNotFoundException, IllegalDataException {
        ds = OsmReader.parseDataSet(new FileInputStream(AbstractTest.PATH_TO_SORT_PT_STOPS), null);
    }

    @Test
    public void test1() {
        Relation rel = (Relation) ds.getPrimitiveById(
                new SimplePrimitiveId(7367762L, OsmPrimitiveType.RELATION));
        new SortPTStopsAction().sortPTStops(rel);

        assertEquals("Acitillo - Istituto Fortunato", rel.getMember(0).getNode().getName());
        assertEquals("Ribera - Gemito", rel.getMember(1).getNode().getName());
        assertEquals("Gemito - Stadio Collana", rel.getMember(2).getNode().getName());
        assertEquals("Cilea 21", rel.getMember(3).getNode().getName());
        assertEquals("Cimarosa", rel.getMember(4).getNode().getName());
        assertEquals("Cimarosa Floridiana - Farmacia Orlando", rel.getMember(5).getNode().getName());
        assertEquals("Bernini - Fuga", rel.getMember(6).getNode().getName());
        assertEquals("Bernini - Solimene", rel.getMember(7).getNode().getName());
        assertEquals("Bernini-Fanzago - Centro Diagnostico Basile", rel.getMember(8).getNode().getName());
        assertEquals("Fiore-Santobono - Centro Diagnostico Basile", rel.getMember(9).getNode().getName());
        assertEquals("Niutta Medaglie d'Oro - Analisi Cliniche Pasteur", rel.getMember(10).getNode().getName());
        assertEquals("Niutta Muzii - Analisi Cliche Pasteur", rel.getMember(11).getNode().getName());
        assertEquals("Arenella Muzii - La Padella Rosticceria", rel.getMember(12).getNode().getName());
        assertEquals("Gigante - Orsi", rel.getMember(13).getNode().getName());
        assertEquals("Della Costituzione - Sottopasso", rel.getMember(14).getNode().getName());
        assertEquals("Della Costituzione - Isola B", rel.getMember(15).getNode().getName());
        assertEquals("Della Costituzione - Moro", rel.getMember(16).getNode().getName());
        assertEquals("Aulisio", rel.getMember(17).getNode().getName());
        assertEquals("Aulisio - Palazzo di Giustizia", rel.getMember(18).getNode().getName());
        assertEquals("Grimaldi - Procura", rel.getMember(19).getNode().getName());
        assertEquals("Biscradi", rel.getMember(20).getNode().getName());
        assertEquals("Nuova Poggioreale 160", rel.getMember(21).getNode().getName());
        assertEquals("Nuova Poggioreale Caramanico - Medicina Futura", rel.getMember(22).getNode().getName());
        assertEquals("Emiciclo Poggoreale", rel.getMember(23).getNode().getName());
    }

    @Test
    public void test2() {
        Relation rel = (Relation) ds.getPrimitives(p -> p.getType() == OsmPrimitiveType.RELATION &&
                "150 Piazza Garibaldi → Osp. Monaldi".equals(p.getName())).iterator().next();
        new SortPTStopsAction().sortPTStops(rel);

        assertNull(rel.getMember(0).getNode().getName());
        assertEquals("Alibus Airport Shuttle", rel.getMember(1).getNode().getName());
        assertEquals("Piazza Garibaldi - Poerio", rel.getMember(2).getNode().getName());
        assertEquals("Piazza Principe Umberto", rel.getMember(3).getNode().getName());
        assertEquals("Ponte Casanova - Ist. Sogliano", rel.getMember(4).getNode().getName());
        assertEquals("Ponte Casanova - Novara", rel.getMember(5).getNode().getName());
        assertEquals("Piazza Nazionale", rel.getMember(6).getNode().getName());
        assertEquals("Nuova Poggioreale - Corso Malta", rel.getMember(7).getNode().getName());
        assertEquals("Grimaldi - Procura", rel.getMember(8).getNode().getName());
        assertEquals("D'Aulisio 14 FR", rel.getMember(9).getNode().getName());
        assertEquals("Della Costituzione - Moro", rel.getMember(10).getNode().getName());
        assertEquals("Della Costituzione - Isola G", rel.getMember(11).getNode().getName());
        assertEquals("Malta - Zara", rel.getMember(12).getNode().getName());
        assertEquals("Arenella Muzzi - La Padella Rosticceria", rel.getMember(13).getNode().getName());
        assertEquals("Palermo - Arenella", rel.getMember(14).getNode().getName());
        assertEquals("Fontana 60", rel.getMember(15).getNode().getName());
        assertEquals("Fontana - Massari", rel.getMember(16).getNode().getName());
        assertEquals("Fontana - Presutti", rel.getMember(17).getNode().getName());
        assertEquals("Cavallino - Parco Ice Snei", rel.getMember(18).getNode().getName());
        assertEquals("Cavallino - Scuola Materna", rel.getMember(19).getNode().getName());
        assertEquals("Cavallino 77", rel.getMember(20).getNode().getName());
        assertEquals("Bernardo Cavallino - Pronto Soccorso Cardarelli", rel.getMember(21).getNode().getName());
        assertNull(rel.getMember(22).getNode().getName());
        assertEquals("Cardarelli - Ospedale", rel.getMember(23).getNode().getName());
        assertEquals("Pietravalle - Gatto", rel.getMember(24).getNode().getName());
        assertEquals("Pietravalle - De Amicis", rel.getMember(25).getNode().getName());
        assertEquals("Pansini - Policlinico", rel.getMember(26).getNode().getName());
        assertEquals("Montesano - Parcheggio M1", rel.getMember(27).getNode().getName());
        assertEquals("Montesano - Cangiani", rel.getMember(28).getNode().getName());
        assertEquals("L. Bianchi - Parco Angiola", rel.getMember(29).getNode().getName());
        assertEquals("Bianchi - Cangiani", rel.getMember(30).getNode().getName());
        assertEquals("Bianchi - Montelungo", rel.getMember(31).getNode().getName());
        assertNull(rel.getMember(32).getNode().getName());
    }
}
