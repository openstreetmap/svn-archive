package org.wikipedia;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.josm.Main;

public class WikidataTagCellRendererTest {

    @Before
    public void setUp() throws Exception {
        Main.initApplicationPreferences();
    }

    @Test
    public void testRenderLabel() throws Exception {
        final List<String> ids = Arrays.asList("Q84", "Q1741", "Q278250");
        final WikidataTagCellRenderer renderer = new WikidataTagCellRenderer();
        renderer.renderValues(ids, new JTable(), new JLabel());
        for (String id : ids) {
            // wait for labels to be fetched
            renderer.labelCache.get(id).get();
        }
        final JLabel label = renderer.renderValues(ids, new JTable(), new JLabel());
        assertNotNull(label);
        assertThat(label.getText(), is("<html>" +
                "Q84 <span color='gray'>London</span>; " +
                "Q1741 <span color='gray'>Vienna</span>; " +
                "Q278250 <span color='gray'>Völs</span>"));
        assertThat(label.getToolTipText(), is("<html><ul>" +
                "<li>Q84 <span color='gray'>London</span></li>" +
                "<li>Q1741 <span color='gray'>Vienna</span></li>" +
                "<li>Q278250 <span color='gray'>Völs</span></li></ul>"));
    }

}