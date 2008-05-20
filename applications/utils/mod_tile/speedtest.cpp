#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/un.h>
#include <poll.h>
#include <errno.h>
#include <math.h>
#include <limits.h>

#include "gen_tile.h"
#include "protocol.h"
#include "render_config.h"
#include "dir_utils.h"

#define DEG_TO_RAD (M_PIl/180)
#define RAD_TO_DEG (180/M_PIl)

static const int minZoom = 0;
static const int maxZoom = 18;

#if 1
static double boundx0=-0.5;
static double boundy0=51.25;
static double boundx1=0.5;
static double boundy1=51.75;
#endif
#if 0
//    bbox = (-6.0, 50.0,3.0,58.0)
static double boundx0=-6.0;
static double boundy0=50.0;
static double boundx1=3.0;
static double boundy1=58.0;
#endif
#if 0
// UK: 49.7,-7.6, 58.8, 3.2
static double boundx0=-7.6;
static double boundy0=49.7;
static double boundx1=3.2;
static double boundy1=58.8;
#endif


static double minmax(double a, double b, double c)
{
#define MIN(x,y) ((x)<(y)?(x):(y))
#define MAX(x,y) ((x)>(y)?(x):(y))
    a = MAX(a,b);
    a = MIN(a,c);
    return a;
}

class GoogleProjection
{
    double *Ac, *Bc, *Cc, *zc;

    public:
        GoogleProjection(int levels=18) {
            Ac = new double[levels];
            Bc = new double[levels];
            Cc = new double[levels];
            zc = new double[levels];
            int d, c = 256;
            for (d=0; d<levels; d++) {
                int e = c/2;
                Bc[d] = c/360.0;
                Cc[d] = c/(2 * M_PIl);
                zc[d] = e;
                Ac[d] = c;
                c *=2;
            }
        }

        void fromLLtoPixel(double &x, double &y, int zoom) {
            double d = zc[zoom];
            double f = minmax(sin(DEG_TO_RAD * y),-0.9999,0.9999);
            x = round(d + x * Bc[zoom]);
            y = round(d + 0.5*log((1+f)/(1-f))*-Cc[zoom]);
        }
        void fromPixelToLL(double &x, double &y, int zoom) {
            double e = zc[zoom];
            double g = (y - e)/-Cc[zoom];
            x = (x - e)/Bc[zoom];
            y = RAD_TO_DEG * ( 2 * atan(exp(g)) - 0.5 * M_PIl);
        }
};

static GoogleProjection gprj(maxZoom+1);


void display_rate(struct timeval start, struct timeval end, int num) 
{
    int d_s, d_us;
    float sec;

    d_s  = end.tv_sec  - start.tv_sec;
    d_us = end.tv_usec - start.tv_usec;

    sec = d_s + d_us / 1000000.0;

    printf("Rendered %d tiles in %.2f seconds (%.2f tiles/s)\n", num, sec, num / sec);
    fflush(NULL);
}



int rx_process(const struct protocol *req)
{
    fprintf(stderr, "version(%d), cmd(%d), z(%d), x(%d), y(%d)\n",
            req->ver, req->cmd, req->z, req->x, req->y);
    return 0;
}

int process_loop(int fd, int x, int y, int z)
{
    struct protocol cmd, rsp;
    //struct pollfd fds[1];
    int ret = 0;

    bzero(&cmd, sizeof(cmd));

    cmd.ver = 1;
    cmd.cmd = cmdRender;
    cmd.z = z;
    cmd.x = x;
    cmd.y = y;
    //strcpy(cmd.path, "/tmp/foo.png");

        //printf("Sending request\n");
    ret = send(fd, &cmd, sizeof(cmd), 0);
    if (ret != sizeof(cmd)) {
        perror("send error");
    }
        //printf("Waiting for response\n");
    bzero(&rsp, sizeof(rsp));
    ret = recv(fd, &rsp, sizeof(rsp), 0);
    if (ret != sizeof(rsp)) {
        perror("recv error");
        return 0;
    }
        //printf("Got response\n");

    if (!ret)
        perror("Socket send error");
    return ret;
}


int main(int argc, char **argv)
{
    const char *spath = RENDER_SOCKET;
    int fd;
    struct sockaddr_un addr;
    int ret=0;
    int z;
    char name[PATH_MAX];
    struct timeval start, end;
    struct timeval start_all, end_all;
    int num, num_all = 0;
  
    
    fprintf(stderr, "Rendering client\n");

    fd = socket(PF_UNIX, SOCK_STREAM, 0);
    if (fd < 0) {
        fprintf(stderr, "failed to create unix socket\n");
        exit(2);
    }

    bzero(&addr, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, spath, sizeof(addr.sun_path));

    if (connect(fd, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
        fprintf(stderr, "socket connect failed for: %s\n", spath);
        close(fd);
        exit(3);
    }

    // Render something to counter act the startup costs
    // of obtaining the Postgis table extents

    printf("Initial startup costs\n");
    gettimeofday(&start, NULL);
    process_loop(fd, 0,0,0);
    gettimeofday(&end, NULL);
    display_rate(start, end, 1);

    gettimeofday(&start_all, NULL);

    for (z=minZoom; z<=maxZoom; z++) {
        double px0 = boundx0;
        double py0 = boundy1;
        double px1 = boundx1;
        double py1 = boundy0;
        gprj.fromLLtoPixel(px0, py0, z);
        gprj.fromLLtoPixel(px1, py1, z);

        int x, xmin, xmax;
        xmin = (int)(px0/256.0);
        xmax = (int)(px1/256.0);

        int y, ymin, ymax;
        ymin = (int)(py0/256.0);
        ymax = (int)(py1/256.0);

        num = (xmax - xmin + 1) * (ymax - ymin + 1);
//        if (!num) {
//            printf("No tiles at zoom(%d)\n", z);
//            continue;
//        }

        printf("\nZoom(%d) Now rendering %d tiles\n", z, num);
        num_all += num;
        gettimeofday(&start, NULL);

        for (x=xmin; x<=xmax; x++) {
            for (y=ymin; y<=ymax; y++) {
                struct stat s;
                xyz_to_meta(name, sizeof(name), x, y, z);
                if (stat(name, &s) < 0) {
                // File doesn't exist
                    ret = process_loop(fd, x, y, z);
                }
                //printf(".");
                fflush(NULL);
            }
        }
        //printf("\n");
        gettimeofday(&end, NULL);
        display_rate(start, end, num);
    }
    gettimeofday(&end_all, NULL);
    printf("\nTotal for all tiles rendered\n");
    display_rate(start_all, end_all, num_all);

    close(fd);
    return ret;
}
