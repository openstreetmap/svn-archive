package public_transport;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public abstract class AStarAlgorithm
{
    // The following abstract functions and subclasses must be overridden by a class using
    // AStarAlgorithm.

    public static abstract class Vertex implements Comparable< Vertex >
    {
        public abstract int compareTo(Vertex v);
    };

    public static abstract class Edge
    {
        public abstract Vertex getBegin();
        public abstract Vertex getEnd();

        public abstract double getLength();
    };

    public abstract Vector< Edge > getNeighbors(Vertex vertex);

    public abstract double estimateDistance(Vertex vertex);

    // end of interface to override -------------------------------------------

    public AStarAlgorithm(Vertex begin, Vertex end)
    {
        this.begin = begin;
        this.end = end;
        openList = new TreeMap< Vertex, Double >();
        closedList = new TreeSet< Vertex >();
        pathTail = new TreeMap< Vertex, Edge >();
    }

    public Vertex determineCurrentStart()
    {
        Vertex minVertex = null;
        double minDist = 0;
        Iterator< Map.Entry< Vertex, Double > > iter = openList.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry< Vertex, Double > entry = iter.next();
            double distance = entry.getValue().doubleValue() + estimateDistance(entry.getKey());
            if (minVertex == null || distance < minDist)
            {
                minDist = distance;
                minVertex = entry.getKey();
            }
        }
        if (minVertex != null)
        {
            System.out.print(openList.get(minVertex).doubleValue());
            System.out.print("\t");
            System.out.println(minDist);
        }

        return minVertex;
    }

    Vector< Edge > shortestPath()
    {
        // Travel through the network
        Vertex currentStart = begin;
        openList.put(currentStart, 0.0);
        while (currentStart != null && !currentStart.equals(end))
        {
            double startDistance = openList.get(currentStart).doubleValue();

            // Mark currentStart as visited.
            openList.remove(currentStart);
            closedList.add(currentStart);

            Iterator< Edge > neighbors = getNeighbors(currentStart).iterator();
            while (neighbors.hasNext())
            {
                Edge edge = neighbors.next();

                // Don't walk back.
                if (closedList.contains(edge.getEnd()))
                    continue;

                // Update entry in openList
                Double knownDistance = openList.get(edge.getEnd());
                double distance = startDistance + edge.getLength();

                if (knownDistance == null || distance < knownDistance.doubleValue())
                {
		    openList.put(edge.getEnd(), distance);
                    pathTail.put(edge.getEnd(), edge);
                }
            }

            currentStart = determineCurrentStart();
        }

        if (currentStart == null)
            return null;

        // Reconstruct the found path
        Vector< Edge > backwards = new Vector< Edge >();
        Vertex currentEnd = end;
        while (!currentEnd.equals(begin))
        {
            backwards.add(pathTail.get(currentEnd));
            currentEnd = pathTail.get(currentEnd).getBegin();
        }

        Vector< Edge > result = new Vector< Edge >();
        for (int i = backwards.size()-1; i >= 0; --i)
            result.add(backwards.elementAt(i));
        return result;
    }

    protected Vertex begin;
    protected Vertex end;

    private TreeSet< Vertex > closedList;
    private TreeMap< Vertex, Double > openList;
    private TreeMap< Vertex, Edge > pathTail;
};
