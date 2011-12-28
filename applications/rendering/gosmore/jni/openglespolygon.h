#if !defined(OPENGLESPOLYGON_H) && !defined(HEADLESS)
#define OPENGLESPOLYGON_H
#include <vector>
#if !defined(ANDROID_NDK) && !defined(NOGTK)
#include <gdk/gdk.h> 
#endif

struct FixedPoint {
  int x, y;
};

struct PolygonEdge {
  int isLeft : 1;
  int delta : 2; // Either -1 or 1
  int continues : 1; // It's a polygon stored in an array and this edge wraps
                     // over. We should continue at this+1 when cnt runs out.
  int cnt : 20;
  FixedPoint *pt, prev;
  PolygonEdge *opp;
/* I tried to make PolygonEdge a node in a tree by using a set<>. It failed
   and I'm not sure how to fix it (add an iterator here ?).
  set<PolygonEdge *, edgeCmp>::iterator itr;

   So instead I copied and pasted my own AA tree code here:
*/
  PolygonEdge *parent, *left, *right;
  int level;
};

#if defined(ANDROID_NDK) || defined(NOGTK)
struct GdkPoint {
  short x, y;
};

void Fill (std::vector<PolygonEdge> &d,int hasSea);
#else
void Fill (std::vector<PolygonEdge> &d,int hasSea, GdkWindow *w, GdkGC *gc);
#endif
void AddPolygon (std::vector<PolygonEdge> &d, FixedPoint *p, int cnt);
void AddClockwise (std::vector<PolygonEdge> &d, FixedPoint *p, int cnt);

#endif
