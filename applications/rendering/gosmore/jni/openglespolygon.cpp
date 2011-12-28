/* Copyright (C) 2011 Nic Roets as detailed in the README file. */
#ifndef HEADLESS
#include <assert.h>
#include <string.h>
#include <malloc.h>
#ifdef ANDROID_NDK
#include <jni.h>
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <android/log.h>
#else
#include <gtk/gtk.h>
#include <gdk/gdk.h>
#include <gdk/gdkx.h>
#endif
#include "openglespolygon.h"
#define LG  //__android_log_print (ANDROID_LOG_WARN, "Gosmore", "%d", __LINE__);

using namespace std;

typedef long long calcType; // Used to prevent overflows when
                            // multiplying two GdkPoint fields

int edgeCmp /*::operator()*/(const PolygonEdge *a, const PolygonEdge *b)
{
  const FixedPoint *ap = a->pt, *bp = b->pt,
    *st = a->prev.y < b->prev.y ? &a->prev : &b->prev;
  calcType cross;
  if (a->pt == &a->prev) return a->prev.x < 0; // left and right borders
  if (b->pt == &b->prev) return b->prev.x > 0;
  while ((cross = (ap->x - st->x) * (calcType)(bp->y - st->y) -
                  (ap->y - st->y) * (calcType)(bp->x - st->x)) == 0) {
    if (ap->y < bp->y) {
      st = ap;
      ap += a->delta;
      if (ap - a->pt >= a->cnt || ap - a->pt + a->cnt <= 0) {
        if (!a->continues) return !b->isLeft; // Edges are the same
        a++;
        ap = a->pt;
      }
    }
    else {
      st = bp;
      bp += b->delta;
      if (bp - b->pt >= b->cnt || bp - b->pt + b->cnt <= 0) {
        if (!b->continues) return !b->isLeft; // Edges are the same
        b++;
        bp = b->pt;
      }
    }
  }
  return cross < 0;
}

//-------------------[ Start of AA tree code ]---------------------------
#define AATREE_NOREBALANCE 1

struct PolygonEdge *First (struct PolygonEdge *root)
{
  if (!root->left) return NULL;
  while (root->left) root = root->left;
  return root;
}

struct PolygonEdge *Next (struct PolygonEdge *n)
{
  if (n->right) {
    n = n->right;
    while (n->left) n = n->left;
  }
  else {
    while (n->parent && n->parent->right == n) n = n->parent;
    // Follow the parent link while it's pointing to the left.
    n = n->parent; // Follow one parent link pointing to the right

    if (!n->parent) return NULL;
    // The last PolygonEdge is the root, because it has nothing to it's right
  }
  return n;
}

struct PolygonEdge *Prev (struct PolygonEdge *n)
{
  if (n->left) {
    n = n->left;
    while (n->right) n = n->right;
  }
  else {
    while (n->parent && n->parent->left == n) n = n->parent;
    // Follow the parent link while it's pointing to the left.
    if (!n->parent) return NULL;
    // The last PolygonEdge is the root, because it has nothing to it's right
    n = n->parent; // Follow one parent link pointing to the right
  }
  return n;
}

void Skew (struct PolygonEdge *oldparent)
{
  struct PolygonEdge *newp = oldparent->left;
  
  if (oldparent->parent->left == oldparent) oldparent->parent->left = newp;
  else oldparent->parent->right = newp;
  newp->parent = oldparent->parent;
  oldparent->parent = newp;
  
  oldparent->left = newp->right;
  if (oldparent->left) oldparent->left->parent = oldparent;
  newp->right = oldparent;
  
  oldparent->level = oldparent->left ? oldparent->left->level + 1 : 1;
}

int Split (struct PolygonEdge *oldparent)
{
  struct PolygonEdge *newp = oldparent->right;
  
  if (newp && newp->right && newp->right->level == oldparent->level) { 
    if (oldparent->parent->left == oldparent) oldparent->parent->left = newp;
    else oldparent->parent->right = newp;
    newp->parent = oldparent->parent;
    oldparent->parent = newp;
    
    oldparent->right = newp->left;
    if (oldparent->right) oldparent->right->parent = oldparent;
    newp->left = oldparent;
    newp->level = oldparent->level + 1;
    return 1;
  }
  return 0;
}

#if 0
static struct PolygonEdge root;
// A static variable to make things easy.

void RebalanceAfterLeafAdd (struct PolygonEdge *n)
{ // n is a PolygonEdge that has just been inserted and is now a leaf PolygonEdge.
  n->level = 1;
  n->left = NULL;
  n->right = NULL;
  for (n = n->parent; n != &root; n = n->parent) {
    // At this point n->parent->level == n->level
    if (n->level != (n->left ? n->left->level + 1 : 1)) {
      // At this point the tree is correct, except (AA2) for n->parent
      Skew (n);
      // We handle it (a left add) by changing it into a right add using Skew
      // If the original add was to the left side of a PolygonEdge that is on the
      // right side of a horisontal link, n now points to the rights side
      // of the second horisontal link, which is correct.
      
      // However if the original add was to the left of PolygonEdge with a horisontal
      // link, we must get to the right side of the second link.
      if (!n->right || n->level != n->right->level) n = n->parent;
    }
    if (!Split (n->parent)) break;
  }
}
#endif

void Delete (struct PolygonEdge *n)
{ // If n is not a leaf, we first swap it out with the leaf PolygonEdge that just
  // precedes it.
  struct PolygonEdge *leaf = n, *tmp;
  
  if (n->left) {
    for (leaf = n->left; leaf->right; leaf = leaf->right) {}
    // When we stop, left has no 'right' child so it cannot have a left one
    #ifdef AATREE_NOREBALANCE
    // Remove leaf:
    if (leaf->parent->left == leaf) leaf->parent->left = leaf->left;
    else leaf->parent->right = leaf->left;
    if (leaf->left) leaf->left->parent = leaf->parent;
    // Replace n with leaf:
    leaf->parent = n->parent;
    leaf->left = n->left;
    leaf->right = n->right;
    if (n->parent->left == n) n->parent->left = leaf;
    else n->parent->right = leaf;
    if (leaf->left) leaf->left->parent = leaf;
    if (leaf->right) leaf->right->parent = leaf;
    return;
    #endif
  }
  #ifdef AATREE_NOREBALANCE
  else {
    if (n->parent->left == n) n->parent->left = n->right;
    else n->parent->right = n->right;
    if (n->right) n->right->parent = n->parent;
    return;
  }
  #else
  else if (n->right) leaf = n->right;
  #endif

  tmp = leaf->parent == n ? leaf : leaf->parent;
  if (leaf->parent->left == leaf) leaf->parent->left = NULL;
  else leaf->parent->right = NULL;
  
  if (n != leaf) {
    if (n->parent->left == n) n->parent->left = leaf;
    else n->parent->right = leaf;
    leaf->parent = n->parent;
    if (n->left) n->left->parent = leaf;
    leaf->left = n->left;
    if (n->right) n->right->parent = leaf;
    leaf->right = n->right;
    leaf->level = n->level;
  }
  #ifndef AATREE_NOREBALANCE
  // free (n);
  while (tmp != &root) {
    // One of tmp's childern had it's level reduced
    if (tmp->level > (tmp->left ? tmp->left->level + 1 : 1)) { // AA2 failed
      tmp->level--;
      if (Split (tmp)) {
        if (Split (tmp)) Skew (tmp->parent->parent);
        break;
      }
      tmp = tmp->parent;
    }
    else if (tmp->level <= (tmp->right ? tmp->right->level + 1 : 1)) break;
    else { // AA3 failed
      Skew (tmp);
      //if (tmp->right) tmp->right->level = tmp->right->left ? tmp->right->left->level + 1 : 1;
      if (tmp->level > tmp->parent->level) {
        Skew (tmp);
        Split (tmp->parent->parent);
        break;
      }
      tmp = tmp->parent->parent;
    }
  }
  #endif
}

void Check (struct PolygonEdge *n)
{
  assert (!n->left || n->left->parent == n);
  assert (!n->right || n->right->parent == n);
//  assert (!Next (n) || n->data <= Next (n)->data);
  assert (!n->parent || n->parent->level >= n->level);
  assert (n->level == (n->left == NULL ? 1 : n->left->level + 1));
  assert (n->level <= 1 || n->right && n->level - n->right->level <= 1);
  assert (!n->parent || !n->parent->parent ||
          n->parent->parent->level > n->level);
}

void Add (struct PolygonEdge *n, struct PolygonEdge *root)
{
  struct PolygonEdge *s = root;
  int lessThan = 1;
  while (lessThan ? s->left : s->right) {
    s = lessThan ? s->left : s->right;
    lessThan = edgeCmp(n, s); //c.operator<(n, s);
  }
  if (lessThan) s->left = n;
  else s->right = n;
  n->parent = s;
  #ifdef AATREE_NOREBALANCE
  n->level = 1;
  n->left = NULL;
  n->right = NULL;
  #else
  RebalanceAfterLeafAdd (n);
  #endif
}
//----------------------------[ End of AA tree ]----------------------------

int ptSize = 0;
void AddPolygon (vector<PolygonEdge> &d, FixedPoint *p, int cnt)
{
  int i, j, k, firstd = d.size();
  ptSize = cnt;
  for (j = cnt - 1; j > 0 && (p[j - 1].y == p[j].y || // p[j..cnt-1] becomes
    (p[j - 1].y < p[j].y) == (p[j].y < p[0].y)); j--) {} // monotone
  //if (j == 0) return; // Polygon has no height but it does not cause infinite loop
  for (i = 0; i < j && (p[i].y == p[i + 1].y ||
    (p[i].y < p[i + 1].y) == (p[j].y < p[0].y)); i++) {}
  // Now p[cn-1],p[0..i] is the longest monotone sequence
  d.resize (firstd + 2);
  memset (&d[firstd], 0, sizeof (d[0]) * 2); // TODO: remove
  d[firstd].delta = p[j].y < p[0].y ? 1 : -1;
  d[firstd].continues = 1;
  d[firstd + 1].delta = p[j].y < p[0].y ? 1 : -1;
  d[firstd + 1].continues = 0;
  d[firstd + !(p[j].y < p[0].y)].cnt = cnt - j;
  d[firstd + (p[j].y < p[0].y)].cnt = i + 1;
  d[firstd + !(p[j].y < p[0].y)].pt = p + (p[j].y < p[0].y ? j : cnt - 1);
  d[firstd + (p[j].y < p[0].y)].pt = p + (p[j].y < p[0].y ? 0 : i);
  //int lowest = j;
  for (; i < j ; i = k) {
    //if (p[lowest].y > p[i].y) lowest = i;
    for (k = i + 1; k < j && (p[k].y == p[k + 1].y || // p[i..k] becomes
        (p[k].y < p[k + 1].y) == (p[i].y < p[k].y)); k++) {} // monotone
    d.push_back(PolygonEdge());
    memset (&d.back(), 0, sizeof (d[0])); // TODO: remove
    d[d.size() - 1].pt = p + (p[i].y < p[k].y ? i : k);
    d[d.size() - 1].delta = p[i].y < p[k].y ? 1 : -1;
    d[d.size() - 1].continues = 0;
    d[d.size() - 1].cnt = k - i + 1;
  }
/*  GdkPoint *ll = &p[lowest], *lr = &p[lowest];
  do {
    if (--ll < p) ll = p + cnt - 1;
  } while (ll->y <= p[lowest].y);
  do {
    if (++lr >= p + cnt) lr = p;
  } while (lr->y <= p[lowest].y);*/
  calcType area = p[cnt-1].x * (calcType) p[0].y - p[0].x * (calcType) p[cnt-1].y;
  for (i = 0; i < cnt - 1; i++) area += p[i].x*(calcType)p[i+1].y-
    p[i+1].x * (calcType) p[i].y;
  for (i = firstd; i < d.size(); i++) {
    // This ll/lr inequality is a cross product that is true if the polygon
    // was clockwise. AddInnerPoly() will just negate isLeft.
    d[i].isLeft = (area < 0) == (d[i].delta == 1);
  }
}

void AddClockwise (vector<PolygonEdge> &d, FixedPoint *p, int cnt)
{
  int i, j;
  #if 0
  for (i = 0; i < cnt; i++) __android_log_print (ANDROID_LOG_WARN, "Gosmore",
    "pt[ptCnt].x = %d; pt[ptCnt++].y=%d;", p[i].x, p[i].y);
  __android_log_print (ANDROID_LOG_WARN, "Gosmore", "AddClockwise(d,pt,ptCnt); ptCnt = 0;");
  #endif
  for (i = 0; i < cnt - 1 && p[i].y == p[0].y; i++) {}
  int up = p[i].y > p[0].y;
  for (i = 0; i < cnt - 1; i = j) {
    d.push_back(PolygonEdge());
    memset (&d.back(), 0, sizeof (d[0])); //TODO: remove
    for (j = i; j + 1 < cnt &&
      (up ? p[j + 1].y >= p[j].y : p[j + 1].y <= p[j].y); j++) {}
    d[d.size() - 1].pt = up ? p + i : p + j;
    d[d.size() - 1].delta = up ? 1 : -1;
    d[d.size() - 1].isLeft = up;
    d[d.size() - 1].cnt = j - i + 1;
    d[d.size() - 1].continues = 0;
    up = !up;
  }
}

#ifdef ANDROID_NDK
void Fill (vector<PolygonEdge> &d,int isSea)
#else
void Fill (vector<PolygonEdge> &d,int isSea, GdkWindow *w, GdkGC *gc)
#endif
{
  //PolygonEdge **heap = (PolygonEdge **) malloc ((d.size() + 1) * sizeof (*heap));
  vector<PolygonEdge*> heap;
  heap.resize (d.size() + 1);
  memset (&heap[0], 0, sizeof (heap[0]) * (d.size()+1)); // TODO: remove
  // leave heap[0] open to make indexing easier
  int i, h = 1, j, sea = 0, start = 1;
  PolygonEdge dummy, left, right, root;
  
  root.left = NULL;
  root.right = NULL;
  root.level = 1000000;
  root.parent = NULL;
  
/*  for (i = 0; i < d.size(); i++) {
    for (j = 0; j + 1 < d[i].cnt; j++) assert (d[i].pt[j*d[i].delta].y <= d[i].pt[(j+1)*d[i].delta].y);
    if (d[i].continues)                assert (d[i].pt[j*d[i].delta].y <= d[i+1].pt->y);
  } // Make sure AddPolygon() and variants are correct */
  dummy.opp = &dummy;
  for (i = 0; i < d.size(); i++) {
    for (j = h++; j > 1 && heap[j / 2]->pt->y > d[i].pt->y; j /= 2) {
      heap[j] = heap[j/2];
    }
    heap[j] = &d[i];
    d[i].opp = NULL;
    memcpy (&d[i].prev, d[i].pt, sizeof (d[i].prev)); // This is only
    // to make the compare work.
    if (d[i].continues) ++i; // Don't add the second part to the heap
  }
  //for (i = 2; i < h; i++) assert (heap[i]->pt->y >= heap[i/2]->pt->y);
  left.prev.x = -6000*65536;
  left.prev.y = 0;
  left.opp = &dummy;
  //h < 3 || edgeCmp (heap[1],
  //                heap[h == 3 || heap[2]->pt->y < heap[3]->pt->y ? 2 : 3])
  //          != !heap[1]->isLeft ? &dummy : &right;
  left.pt = &left.prev;
  left.isLeft = 1;
  Add (&left, &root);
  right.prev.x = 6000*65536;
  right.prev.y = 0;
  right.opp = &dummy; //left.opp == &dummy ? &dummy : &left;
  right.pt = &right.prev;
  right.isLeft = 0;
  Add (&right, &root);
  while (h > 1) {
    PolygonEdge *head = heap[1];
    //printf ("%p %3d\n", head->opp, head->pt->y);
    if (!head->opp) { // Insert it
      Add (head, &root);
      head->opp = head->isLeft ? Next (head) : Prev (head);
      if (head->opp == NULL || head->opp->isLeft == head->isLeft) {
        head->opp = &dummy;
      }
    }
    PolygonEdge *o = head->opp;
    /* Now we render the trapezium between head->opp and head->opp->opp up
       to head->pt->y */
    if (o != &dummy && o->opp != &dummy) {
      GdkPoint q[6];
      #define CLAMPX(x) (short)((x) >= (1<<29) ? 8191 : (x) < -(1<<29) ? -8191 : (x) >> 16)
      q[2].x = CLAMPX (o->opp->prev.x);
      q[2].y = CLAMPX (o->opp->prev.y);
      q[3].x = CLAMPX (o->prev.x);
      q[3].y = CLAMPX (o->prev.y);
      o->prev.x = o->pt->y <= o->prev.y ? o->pt->x
        : o->prev.x + (o->pt->x - o->prev.x) *
            calcType (head->pt->y - o->prev.y) / (o->pt->y - o->prev.y);
      q[0].x = CLAMPX(o->prev.x);
      o->prev.y = head->pt->y;
      q[0].y = CLAMPX(o->prev.y);
      o->opp->prev.x = o->opp->pt->y <= o->opp->prev.y ? o->opp->pt->x
        : o->opp->prev.x + (o->opp->pt->x - o->opp->prev.x) *
      calcType (head->pt->y - o->opp->prev.y) / (o->opp->pt->y - o->opp->prev.y);
      q[1].x = CLAMPX (o->opp->prev.x);
      o->opp->prev.y = head->pt->y;
      q[1].y = q[0].y;
    //if (o->opp->prev.y != o->prev.y && o->opp->prev.x < 30000 &&
    //  o->opp->prev.x > -30000 && o->prev.x < 30000 && o->prev.x > -30000)
    //  __android_log_print (ANDROID_LOG_WARN, "Gosmore", "Prev dif");
      if ((isSea || (o->pt != &o->prev && o->opp->pt != &o->opp->prev)) &&
          q[o->pt == &o->prev ? 2 : 3].y < q[0].y) {
      // Frequently it happens that one of the triangles has 0 area because
      // two of the points are equal. TODO: Filter them out.
        #ifdef ANDROID_NDK
        memcpy (&q[4], &q[0], sizeof (q[4]));
        memcpy (&q[5], &q[2], sizeof (q[5]));
        glVertexPointer (2, GL_SHORT, 0, q);
        glDrawArrays (GL_TRIANGLES, 0, 6);
        #else
        //memcpy (&q[2], &o->prev, sizeof (q[2]));
        //memcpy (&q[3], &o->opp->prev, sizeof (q[3]));
        gdk_draw_polygon (w, gc, TRUE, q, 4);
        #endif
        if (q[0].x > 100 && q[1].x <= 100) sea = o->opp == &left;
        if (q[0].x <= 100 && q[1].x < 100) sea = o->opp == &right;
        if (q[1].x > 100 && q[0].x <= 100) sea = o == &left;
        if (q[1].x <= 100 && q[0].x < 100) sea = o == &right;
        
        if (start) {
          start = 0;
          if (sea) {
            q[2].x = -6000;
            q[3].x = 6000;
            q[4].x = 0;
            q[4].y = -6000;
            #ifdef ANDROID_NDK
            glVertexPointer (2, GL_SHORT, 0, q + 2);
            glDrawArrays (GL_TRIANGLES, 0, 3);
            #else
            //memcpy (&q[2], &o->prev, sizeof (q[2]));
            //memcpy (&q[3], &o->opp->prev, sizeof (q[3]));
            gdk_draw_polygon (w, gc, TRUE, q + 2, 3);
            #endif
          }
        }
      } // If the trapezium has a non-zero height
    }
    if (o != &dummy && o->opp != head) {
      o->opp->opp = &dummy; // Complete the 'Add'
      o->opp = head;
    }

    if (--head->cnt) head->pt += head->delta;
    else if (head->continues) {
      head->continues = head[1].continues;
      head->cnt = head[1].cnt;
      head->pt = head[1].pt;
    }
    else { // Remove it
      head->opp->opp = &dummy;
      PolygonEdge *n = head->isLeft ? Prev (head) : Next (head);
      if (n && n->opp == &dummy) {
        if (head->isLeft == n->isLeft && 
          (head->opp->pt != &head->opp->prev || n->pt != &n->prev || sea)) {
          n->opp = head->opp;
          head->opp->opp = n;
          n->prev.x += n->pt->y <= n->prev.y ? n->pt->x - n->prev.x :
            (n->pt->x - n->prev.x) *
            calcType(head->prev.y - n->prev.y) / (n->pt->y - n->prev.y);
          n->prev.y = head->prev.y;
        }
        else {
          // Either n is not a replacement for head because one of them is
          // a left side and the other right side 
          // OR
          // heap->opp is either left or right and n is either left or right.
          // and we recently drew all the way to the left or right ("sea").
          n->opp = &dummy;
          head->opp->opp = &dummy;
        }
      }
      Delete (head);
      head = heap[--h];
    }
    
    for (j = 2; j < h; j *= 2) {
      if (j + 1 < h && heap[j + 1]->pt->y < heap[j]->pt->y) j++;
      if (heap[j]->pt->y >= head->pt->y) break;
      heap[j / 2] = heap[j];
    }
    heap[j / 2] = head;
    for (i = 2; i < h; i++) assert (heap[i]->pt->y >= heap[i/2]->pt->y);
  } // While going through the edges
  if (sea) { //left.opp == &right) {
    GdkPoint end[3];
    end[0].x = -6000;
    end[0].y = max (left.prev.y, right.prev.y) >> 16;
    end[1].x = 6000;
    end[1].y = end[0].y;
    end[2].x = 0; 
    end[2].y = 6000;
    #ifdef ANDROID_NDK
    glVertexPointer (2, GL_SHORT, 0, end);
    glDrawArrays (GL_TRIANGLES, 0, 3);
    #else
    gdk_draw_polygon (w, gc, TRUE, end, 3);
    #endif
  }
  //free (heap);
}
#endif
