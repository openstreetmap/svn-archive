// Finds rectangles that cover the planet. Requires density.csv, which is created by
// make CFLAGS="-DMKDENSITY_CSV -O2" && bzcat planet.osm.bz2 | ./gosmore sortRelations
// g++ -O2 density6.cpp && time nice -n 19 ./a.out >density.txt
// Run time is approximately 5 minutes and will require 5 GB of RAM

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <vector>
#include <queue>
#include <assert.h>
using namespace::std;

int b[4000000][8], cnt = 0;
int dp[1025][1025], target, maks = 0, col;

int BCmp (int *a, int *b)
{
  return *a - *b;
}

struct state {
  long long nCovered;
  int **s, **best, c, mActive;
  state () {}
};

bool operator < (const state &a, const state &b)
{
//  return a.c == 0 ? false : b.c == 0 ? true : a.nCovered / a.c < b.nCovered / b.c;
  int cadj = col > 800 ? 112165 : col > 620 ? 3165 : col < 140 ? 15 :
    (col > 340 && col < 410) ? 1516 : 5565;
  return (a.nCovered + 60000000) / (a.c + cadj) <
         (b.nCovered + 60000000) / (b.c + cadj);
}

int IPtrCmp (int **a, int **b)
{
  return **b - **a;
}

vector<state> states;
priority_queue<state> que;
vector<int*> active;


#define I(a,b,e,d) (dp[a+1][b+1] - dp[e][b+1] - dp[a+1][d] + dp[e][d])
#define M 11000000

void Try (void)
{
  while (states.size () < 2000 && !que.empty ()) {
    //printf ("%lld %d\n", que.top ().nCovered, states.size ());
    int rh[1024], i, j, **s = que.top ().s, l, c = que.top ().c;
    int **best = que.top ().best, nCovered = que.top ().nCovered;
    memset (rh, 0, sizeof (rh));
    for (i = 0, l = 0; s[i]; i++) {
      for (j = s[i][1]; j < s[i][3]; j++) {
        if (rh[j] < s[i][2]) rh[j] = s[i][2];
      }
      if (s[i][2] > col) s[l++] = s[i]; // Remove rectangles we passed.
    }
    s[l] = NULL; // Remove rectangles we passed.
    for (i = 0; i < 1024 && rh[i] > col; i++) {}
    if (i == 1024) { // The state covers this column
      states.push_back (state ()); //que.top ());
      memcpy (&states.back (), &que.top (), sizeof (que.top ()));
    }
    //printf ("%d\n", i);
//          assert (states.empty () || states[0].best[0]);
    j = que.top ().mActive;
    que.pop ();
//          assert (states.empty () || states[0].best[0]);
    for (; i < 1024 && j < active.size (); j++) { // 'i' is the first row not covered.
      for (l = 0x3fffff; l >= active.size (); l/=2) {}
      int *a = active[j < l ? j ^ (/*nCovered &*/(col*1717) & l) : j];
      if (a[1] <= i && i < a[3]) {
        int newNcov = nCovered;
        for (l = a[1]; l < a[3]; l++) {
          if (rh[l] < a[2]) newNcov += I (l, a[2] - 1, l,
            (rh[l] < col ? col : rh[l]));
        }
       //assert (newNcov < nCovered + 14000000);
        //assert (newNcov >= I (1023, col - 1, 0, 0));
        //assert (a[2] > 800 ||newNcov <= I (1023, a[2] + 168, 0, 0));
        //if (newNcov > nCovered + (col < 200 ? 2000000: col < 600 ? 10000*0:
        //      col < 650 ? 200000:  col < 700 ? 1200000:col<780 ? 500000: col < 850 ? 100000: 10)) { // (c + 1) * 1000000) { //target) {
        if (newNcov > nCovered + que.size () / (col < 512 ? 20 :10)) {
          state nxt;
          for (l = 0; s[l]; l++) {}
          nxt.s = new int *[l+2];//(int**) malloc ((l + 2) * sizeof (*s));
          nxt.s[0] = a;
          memcpy (nxt.s + 1, s, (l + 1) * sizeof (*s));

          for (l = 0; best[l]; l++) {}
          assert (l ==c);
          nxt.best = new int *[l+2]; //(int**) malloc ((l + 2) * sizeof (*s));
          nxt.best[0] = a;
          memcpy (nxt.best + 1, best, (l + 1) * sizeof (*s));
          nxt.c = c + 1;
          nxt.nCovered = newNcov;
          nxt.mActive = j + 1;
          if (0&&newNcov < maks) {
            fprintf (stderr, "%d %d %d %d\n", newNcov, c, que.size (), i);
            maks = newNcov;
          }
          que.push (nxt);
//          assert (states.empty () || states[0].best[0]);
        }
      }
    } // Look for a bbox that can cover the gap.
    if (i < 1024) delete s; //free (s);
//          assert (states.empty () || (states[0].best[0] && best != states[0].best));
    if (i < 1024) delete best; //free (best);
  } // While creating new states
  for (; !que.empty (); que.pop ()) {
    delete que.top().s; //free (que.top ().s);
    delete que.top().best;//free (que.top ().best);
  }
}

main ()
{
  FILE *in = fopen ("density.csv", "r");
  FILE *bf = fopen ("b.bin", "r+");
  if (!bf) bf = fopen ("b.bin", "w");
//  memset (c, 0, sizeof (c));
//  memset (dc, 0, sizeof (dc));
  int d, i, j, k, l;
  #if 1
  for (i = 0; i < 1025; i++) dp[i][0] = dp[0][i] = 0;
  for (i = 0; i < 1024; i++) {
    for (j = 0; j < 1024; j++) {
      fscanf (in, j == 1023 ? "%d\n" : "%d ", &d);
      dp[i+1][j+1] = d + dp[i][j+1] + dp[i+1][j] - dp[i][j];
    }
  }
  #endif
  //printf ("%d\n", dp[1024][1024]);
#if 1
//#define O 2
  for (i = 0; i < 1024; i++) {
    for (j = 0; j < 1024; j++) {
      int maxk = i < 415 && j > 412 ? 462 : i < 666 && j > 430 ? 712 : 1024, l2;
      // Reject rectangles that cross the South Atlantic or the Indian Ocean
      for (k = i, l2 = 1023; k < maxk; k++) {
        int mini = j > 430 && k > 712 ? 666 : j > 412 && k > 462 ? 415 : 0;
        maxk = min (maxk, i + 3 * (l2 - j));
        for (; l2 >= j && I (l2, k, j, i) > M; l2--) {}
        l = min (l2, j + 3 * (k - i));
        if (l >= j && // Area > 0
            (i == mini || I (l, k, j, i - 1) > M) && // Not expandable Westwards
            (j == 0 || I (l, k, j - 1, i) > M) && // Not expandable Northwards
            (k == maxk - 1 || I (l, k + 1, j, i) > M)) { // Not expandable Eastwards
          int O = 1; //i > 150 && k < 320 ? 0 : 2;
          // Some parts of US so crowded that 4x4 is too large
          if ((l >= 1023 || j == 0 || l >= j + O*1) && 
              (k >= 1023 || i == 0 || k >= i + O*1)) {
            b[cnt][0] = i == 0 ? i : i + (k - i + O*4)/9;
            b[cnt][1] = j == 0 ? j : j + (l - j + O*4)/9;
            b[cnt][2] = k == 1023 ? k + 1 : k + 1 - (k - i + O*4)/9;
            b[cnt][3] = l == 1023 ? l + 1 : l + 1 - (l - j + O*4)/9;
            b[cnt][4] = i;
            b[cnt][5] = j;
            b[cnt][6] = k + 1;
            b[cnt++][7] = l + 1;
          }
          //printf ("%4d %4d %4d %4d %d\n", i, j, k, l, I (l, k, j, i));
        }
      }
    }
  }
  qsort (b, cnt, sizeof (b[0]), (int (*)(const void *, const void *)) BCmp);
  fwrite (b, cnt, sizeof (b[0]), bf);
#else  
  cnt = fread (b, sizeof (b[0]), sizeof (b) / sizeof (b[0]), bf);
#endif
  fprintf (stderr, "%d bboxes\n", cnt);
  states.push_back (state ());
  states.back ().s = new int*[1];//(int**) calloc (sizeof (*states.back ().s), 1);
  states.back ().s[0] = NULL;
  states.back ().best = new int*[1]; //(int**) calloc (sizeof (*states.back ().best), 1);
  states.back ().best[0] = NULL;
  states.back ().nCovered = 0;
  states.back ().c = 0;
  for (col = 0, j = 0; col < 1024 && !states.empty (); col++) {
    for (;j < cnt && b[j][0] <= col; j++) active.push_back (&b[j][0]);
    target = 0; //10000000 / (state.size () + 3);
    for (k = 0; k < states.size (); k++) {
      target += (states[k].nCovered + 12000000 + states.size()*5000) / (states[k].c + 1) /
        states.size ();
      //printf ("%d %d\n", states[k].nCovered, 
    }
    fprintf (stderr, "%4d %d active, %d states %d %d\n", col, active.size (), states.size (),
      target, states[0].c);
    for (k = states.size () - 1; k >= 0; k--) {
      for (l = 0; states[k].s[l] && states[k].s[l][2] > col; l++) {}
      if (states[k].s[l] || l == 0 /* bootstrap */) {
        states[k].mActive = 0;
        que.push (states[k]);
        memcpy (&states[k], &states.back (), sizeof (states[k]));
        states.pop_back ();
      }
    }
    Try ();
/*    for (k = states.size () - 1; k >= 0; k--) {
          for (l = 0; states[k].best[l]; l++) {}
          assert (l ==states[k].c);
    }*/
    for (k = active.size () - 1; k >= 0; k--) {
      if (active[k][2] <= col + 1) {
        active[k] = active.back();
        active.pop_back ();
      }
    }
  }
  for (i = 1, j = 0; i < states.size (); i++) {
    if (states[i].c < states[j].c) j = i;
  }
  fprintf (stderr, "%d rectangles\n", states[j].c);
  for (i = 0; states[j].best[i]; i++) {
    printf ("%d %d %d %d\n", states[j].best[i][4], states[j].best[i][5],
      states[j].best[i][6], states[j].best[i][7]);
  }
  
}
