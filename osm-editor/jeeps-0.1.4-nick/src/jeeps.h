#include <sys/param.h>

#define DEF_PORT 	"/dev/ttyS1"
#define DEF_ALM_FILE 	"almanac"
#define DEF_WAY_FILE 	"waypoints"
#define DEF_ROU_FILE 	"routes"
#define DEF_TRK_FILE 	"tracks"
#define DEF_PRX_FILE 	"proximity"

#define ALMREC	1
#define ALMTRA  2
#define WAYREC	3
#define WAYTRA  4
#define ROUREC  5
#define ROUTRA  6
#define TRKREC  7
#define TRKTRA  8
#define PRXREC  9
#define PRXTRA  10
#define TIMREC  11
#define TIMTRA  12
#define PSNREC  13
#define PSNTRA  14
#define PVTREC  15
#define INFO    16
#define SAVE    17
#define OFF     0


char port[MAXPATHLEN];

char stdalm[MAXPATHLEN];
char stdrou[MAXPATHLEN];
char stdway[MAXPATHLEN];
char stdprx[MAXPATHLEN];
char stdtrk[MAXPATHLEN];
