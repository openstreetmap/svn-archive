#ifndef SIRF_LOWLEVEL_H
#define SIRF_LOWLEVEL_H

#include <termios.h>
#include <unistd.h>

// "packet" type for ferrying information around
typedef struct packet {
  unsigned char *buffer;
  unsigned int size;
} packet_t;

// functions to allocate and free packet structures
packet_t *allocate_packet(unsigned int size);
void free_packet(packet_t *p);

// low-level NMEA functions (don't need them except to send NMEA 
// "switch to SiRF binary" instructions
unsigned int nmea_checksum(const char *data);
packet_t *build_nmea_packet(int baud);

// low-level SiRF functions to send the SiRF serial requests
int sirf_write_checksum(unsigned char *buffer, int length);
int sirf_write_line(unsigned char *buffer, unsigned char a,
		    unsigned char b, unsigned char c,
		    unsigned int baud, unsigned char d,
		    unsigned char e, unsigned char f,
		    unsigned char g, unsigned char h);
packet_t *build_sirf_packet(int baud);

// low level write to file descriptor
void write_packet(int fd, packet_t *packet, const char *desc, 
		  int baudat, int baudto);

// synchronise speed and protocol with the SiRF
void lowlevel_init(int fd, int baud);
void lowlevel_sync(int fd, int requested_baud);

// speed-related functions
void set_reset_speed(int fd, speed_t baud);
speed_t get_unistd_speed(int baud);


#endif /* SIRF_LOWLEVEL_H */
