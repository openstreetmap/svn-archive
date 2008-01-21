using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.Geometry
{
    [Serializable]
    public class Polyline : ICloneable
    {
        public int VerticesCount { get { return vertices.Count; } }

        [SuppressMessage ("Microsoft.Design", "CA1006:DoNotNestGenericTypesInMemberSignatures")]
        public IList<Point3<double>> Vertices
        {
            get { return vertices; }
        }

        public double Orientation
        {
            get
            {
                // first find rightmost lowest vertex of the polygon
                int rmin = 0;
                double xmin = vertices[0].X;
                double ymin = vertices[0].Y;

                for (int i = 1; i < VerticesCount; i++)
                {
                    if (vertices[i].Y > ymin)
                        continue;
                    if (vertices[i].Y == ymin)
                    {    // just as low
                        if (vertices[i].X < xmin)   // and to left
                            continue;
                    }
                    rmin = i;          // a new rightmost lowest vertex
                    xmin = vertices[i].X;
                    ymin = vertices[i].Y;
                }

                // test orientation at this rmin vertex
                // ccw <=> the edge leaving is left of the entering edge
                return GeometryUtils.IsLeft (vertices[(rmin - 1 + VerticesCount) % VerticesCount], vertices[rmin], vertices[(rmin + 1) % VerticesCount]);
            }
        }

        /// <summary>
        /// Gets or sets a value indicating whether this polyline is closed, i.e. the last vertex is connected with the first one.
        /// </summary>
        /// <value><c>true</c> if this polyline is closed; otherwise, <c>false</c>.</value>
        public bool IsClosed
        {
            get { return isClosed; }
            set { isClosed = value; }
        }

        //http://softsurfer.com/Archive/algorithm_0108/algorithm_0108.htm#simple_Polygon()
//        // simple_Polygon(): test if a Polygon P is simple or not
//        //     Input:  Pn = a polygon with n vertices V[]
//        //     Return: FALSE(0) = is NOT simple
//        //             TRUE(1)  = IS simple
//        int
//        simple_Polygon (Polygon Pn)
//{
//    EventQueue  Eq(Pn);
//    SweepLine   SL(Pn);
//    Event*      e;                 // the current event
//    SLseg*      s;                 // the current SL segment

//    // This loop processes all events in the sorted queue
//    // Events are only left or right vertices since
//    // No new events will be added (an intersect => Done)
//    while (e = Eq.next()) {        // while there are events
//        if (e->type == LEFT) {     // process a left vertex
//            s = SL.add(e);         // add it to the sweep line
//            if (SL.intersect( s, s->above)) 
//                return FALSE;      // Pn is NOT simple
//            if (SL.intersect( s, s->below)) 
//                return FALSE;      // Pn is NOT simple
//        }
//        else {                     // processs a right vertex
//            s = SL.find(e);
//            if (SL.intersect( s->above, s->below)) 
//                return FALSE;      // Pn is NOT simple
//            SL.remove(s);          // remove it from the sweep line
//        }
//    }
//    return TRUE;      // Pn is simple
//}
//        //===================================================================


        public Polyline () 
        {
            vertices = new List<Point3<double>> ();
        }

        public Polyline (int verticesCount)
        {
            vertices = new List<Point3<double>> (verticesCount);
        }

        public void AddVertex (Point3<double> point)
        {
            vertices.Add (point);
        }

        public void InsertVertex (int index, Point3<double> vertex)
        {
            vertices.Insert (index, vertex);
        }

        public void RemoveVertex (int index)
        {
            vertices.RemoveAt (index);
        }

        /// <summary>
        /// Removes all vertices which have the same coordinates as their neighbouring vertex.
        /// </summary>
        public void RemoveDuplicateVertices ()
        {
            for (int i = 0; i < VerticesCount && VerticesCount > 1; )
            {
                Point3<double> point1 = vertices[i];

                int j = (i + 1) % VerticesCount;

                Point3<double> point2 = vertices[j];

                if (point2 == point1)
                    vertices.RemoveAt (j);
                else
                    i++;
            }
        }

        /// <summary>
        /// Reverses the order of vertices in the polyline.
        /// </summary>
        public void Reverse ()
        {
            vertices.Reverse ();
        }

        #region ICloneable Members

        public object Clone ()
        {
            Polyline clone = new Polyline ();

            clone.vertices = new List<Point3<double>> (vertices);

            return clone;
        }

        #endregion

        private List<Point3<double>> vertices;
        private bool isClosed;
    }
}
