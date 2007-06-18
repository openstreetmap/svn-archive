package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.validator.*;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Checks for similar named ways, symptom of a possible typo. It uses the
 * Levenshtein distance to check for similarity
 * 
 * @author frsantos
 */
public class SimilarNamedWays extends Test 
{
    /** All ways, grouped by cells */
    Map<Point2D,List<Way>> cellWays;
    /** The already detected errors */
    Bag<Way, Way> errorWays;
    
    /**
	 * Constructor
	 */
	public SimilarNamedWays() 
	{
		super(tr("Similar named ways."),
			  tr("This test checks for ways with similar names that may have been misspelled."));
	}

    @Override
    public void startTest() 
    {
        cellWays = new HashMap<Point2D,List<Way>>(1000);
        errorWays = new Bag<Way, Way>();
    }

    @Override
    public void endTest() 
    {
        cellWays = null;
        errorWays = null;
    }
    
	@Override
	public void visit(Way w) 
	{
        if( w.deleted )
            return;
        
        String name = w.get("name");
        if( name == null || name.length() < 6 )
            return;
        
        List<List<Way>> cellWays = getWaysInCell(w);
        for( List<Way> ways : cellWays)
        {
            for( Way w2 : ways)
            {
                if( errorWays.contains(w, w2) || errorWays.contains(w2, w) )
                	continue;
                
                String name2 = w2.get("name");
                if( name2.length() < 6 )
                    continue;
                
                int levenshteinDistance = getLevenshteinDistance(name, name2);
                if( 0 < levenshteinDistance && levenshteinDistance <= 2 )
                {
                    List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
                    primitives.add(w);
                    primitives.add(w2);
                    errors.add( new TestError(this, Severity.WARNING, tr("Similar named ways"), primitives) );
                    errorWays.add(w, w2);
                }
            }
            ways.add(w);
        }
	}
    

    /**
     * Returns the start and end cells of a way.
     * @param w The way
     * @return A list with all the cells the way starts or ends
     */
    public List<List<Way>> getWaysInCell(Way w)
    {
        int numSegments = w.segments.size();
        if( numSegments == 0)
            return Collections.emptyList();

        Segment start = w.segments.get(0);
        Segment end = start;
        if( numSegments > 1 )
        {
            end = w.segments.get(numSegments - 1);
        }
        
        if( start.incomplete || end.incomplete )
            return Collections.emptyList();
        
        List<List<Way>> cells = new ArrayList<List<Way>>(2);
        Set<Point2D> cellNodes = new HashSet<Point2D>();
        Point2D cell;

        // First, round coordinates
        long x0 = Math.round(start.from.eastNorth.east()  * 100000);
        long y0 = Math.round(start.from.eastNorth.north() * 100000);
        long x1 = Math.round(end.to.eastNorth.east()      * 100000);
        long y1 = Math.round(end.to.eastNorth.north()     * 100000);

        // Start of the way
        cell = new Point2D.Double(x0, y0);
        cellNodes.add(cell);
        List<Way> ways = cellWays.get( cell );
        if( ways == null )
        {
            ways = new ArrayList<Way>();
            cellWays.put(cell, ways);
        }
        cells.add(ways);
        
        // End of the way
        cell = new Point2D.Double(x1, y1);
        if( !cellNodes.contains(cell) )
        {
            cellNodes.add(cell);
            ways = cellWays.get( cell );
            if( ways == null )
            {
                ways = new ArrayList<Way>();
                cellWays.put(cell, ways);
            }
            cells.add(ways);
        }

        // Then floor coordinates, in case the way is in the border of the cell.
        x0 = (long)Math.floor(start.from.eastNorth.east()  * 100000);
        y0 = (long)Math.floor(start.from.eastNorth.north() * 100000);
        x1 = (long)Math.floor(end.to.eastNorth.east()      * 100000);
        y1 = (long)Math.floor(end.to.eastNorth.north()     * 100000);

        // Start of the way
        cell = new Point2D.Double(x0, y0);
        if( !cellNodes.contains(cell) )
        {
            cellNodes.add(cell);
            ways = cellWays.get( cell );
            if( ways == null )
            {
                ways = new ArrayList<Way>();
                cellWays.put(cell, ways);
            }
            cells.add(ways);
        }
        
        // End of the way
        cell = new Point2D.Double(x1, y1);
        if( !cellNodes.contains(cell) )
        {
            cellNodes.add(cell);
            ways = cellWays.get( cell );
            if( ways == null )
            {
                ways = new ArrayList<Way>();
                cellWays.put(cell, ways);
            }
            cells.add(ways);
        }

        return cells;
    }
    

    /**
     * Compute Levenshtein distance
     * 
     * @param s First word
     * @param t Second word
     * @return The distance between words
     */
    public int getLevenshteinDistance(String s, String t)
    {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1

        n = s.length();
        m = t.length();
        if (n == 0) return m;
        if (m == 0) return n;
        d = new int[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) d[i][0] = i;
        for (j = 0; j <= m; j++) d[0][j] = j;

        // Step 3
        for (i = 1; i <= n; i++)
        {
            s_i = s.charAt(i - 1);

            // Step 4
            for (j = 1; j <= m; j++)
            {
                t_j = t.charAt(j - 1);

                // Step 5
                if (s_i == t_j)
                {
                    cost = 0;
                }
                else
                {
                    cost = 1;
                }

                // Step 6
                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
            }
        }

        // Step 7
        return d[n][m];

    }

    /**
     * Get minimum of three values
     */
    private static int Minimum(int a, int b, int c)
    {
        int mi = a;
        if (b < mi)
        {
            mi = b;
        }
        if (c < mi)
        {
            mi = c;
        }
        return mi;
    }
}
