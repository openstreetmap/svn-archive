package com.tilusnet.josm.plugins.alignways.geometry;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.tilusnet.josm.plugins.alignways.geometry.AlignWaysGeomLine.IntersectionStatus;

public class AlignWaysGeomLineTest {
    private AlignWaysGeomLine line_x1y1x2y2, line_par_x1y1x2y2;
    private AlignWaysGeomLine line_abc;
    private AlignWaysGeomLine line_mb;
    private AlignWaysGeomLine line_line;
    private AlignWaysGeomLine line_horiz, line_vert;

    @Before
    public void setUp() {
        line_x1y1x2y2 = new AlignWaysGeomLine(-2.0, -1.0, 4.0, 3.0);
        line_abc = new AlignWaysGeomLine(2.0/3, -1.0, 1.0/3);
        line_mb = new AlignWaysGeomLine(2.0/3, 1.0/3);
        line_line = new AlignWaysGeomLine(line_x1y1x2y2);
        line_par_x1y1x2y2 = new AlignWaysGeomLine(-2.0, -2.0, 4.0, 2.0);
        line_horiz = new AlignWaysGeomLine(-5.0, 3.0, 6.0, 3.0);
        line_vert = new AlignWaysGeomLine(2.0, -3.0, 2.0, -10.5);
    }

    @Test
    public void LineLineEquiv() {
        assertTrue(line_x1y1x2y2.equals(line_line));
    }

    @Test
    public void LineAbcLineEquiv() {
        assertTrue(line_x1y1x2y2.equals(line_abc));
    }

    @Test
    public void LineMbLineEquiv() {
        assertTrue(line_x1y1x2y2.equals(line_mb));
    }

    @Test
    public void LineAbcMbEquiv() {
        assertTrue(line_abc.equals(line_mb));
    }

    @Test
    public void LineLineParallel() {
        line_x1y1x2y2.getIntersection(line_par_x1y1x2y2);
        assertTrue(line_x1y1x2y2.getIntersectionStatus() == IntersectionStatus.LINES_PARALLEL);
    }

    @Test
    public void LineAbcOverlap() {
        line_x1y1x2y2.getIntersection(line_abc);
        assertTrue(line_x1y1x2y2.getIntersectionStatus() == IntersectionStatus.LINES_OVERLAP);
    }

    @Test
    public void LineMbOverlap() {
        line_x1y1x2y2.getIntersection(line_mb);
        assertTrue(line_x1y1x2y2.getIntersectionStatus() == IntersectionStatus.LINES_OVERLAP);
    }

    @Test
    public void AbcMbOverlap() {
        line_abc.getIntersection(line_mb);
        assertTrue(line_abc.getIntersectionStatus() == IntersectionStatus.LINES_OVERLAP);
    }

    @Test
    public void GetXOnHoriz() {
        assertTrue(line_horiz.getXonLine(2.0).isNaN());
    }

    @Test
    public void GetYOnVert() {
        assertTrue(line_vert.getYonLine(2.0).isNaN());
    }

    @Test
    public void StatusUndefAfterConstruct() throws Exception {
        AlignWaysGeomLine newLine = new AlignWaysGeomLine(1.0, 2.0);
        assertTrue(newLine.getIntersectionStatus() == IntersectionStatus.UNDEFINED);

    }

}
