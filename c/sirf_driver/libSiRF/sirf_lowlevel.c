#include <stdlib.h>
#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/poll.h>

#include "sirf_lowlevel.h"

// allocate a packet
packet_t *allocate_packet(unsigned int size) {
  packet_t *rv;
  
  if ((rv = (packet_t *)malloc(sizeof(packet_t))) == NULL) {
    fprintf(stderr, "Cannot allocate packet structure!\n");
    exit(-1);
  }
  
  if ((rv->buffer = (unsigned char *)malloc(sizeof(unsigned char) * size)) 
      == NULL) {
    fprintf(stderr, "Cannot allocate packet buffer!\n");
    exit(-1);
  }
  
  rv->size = size;
  
  return rv;
}

// free a packet
void free_packet(packet_t *p) {
  free(p->buffer);
  p->size = 0;
  free(p);
}

// find NMEA checksum
unsigned int nmea_checksum(const char *data) {
  unsigned int rv = 0;
  int i;
  
  for (i = 0; i < strlen(data); i++) {
    rv ^= data[i];
  }
  
  return rv;
}

// make an NMEA packet
packet_t *build_nmea_packet(int baud) {
  char payload[100], data[100];
  packet_t *rv;
  int i;
  
  snprintf(payload, 100, "PSRF100,0,%d,8,1,0", baud);
  snprintf(data, 100, "$%s*%02x\r\n", payload, nmea_checksum(payload));
  
  rv = allocate_packet(strlen(data));
  for (i = 0; i < strlen(data); i++) {
    rv->buffer[i] = (unsigned char)data[i];
  }
  
  return rv;
}

// find SiRF checksum
int sirf_write_checksum(unsigned char *buffer, int length) {
  unsigned int cksum = 0;
  int i;
  
  for (i = 0; i < length; i++) {
    cksum = (cksum + buffer[i]) & 0x7fff;
  }
  
  // SiRF byte ordering is MSB first?
  buffer[length] = ((cksum & 0xff00) >> 8);
  buffer[length+1] = (cksum & 0xff);
  
  return 2;
}

int sirf_write_line(unsigned char *buffer, 
			   unsigned char a,
			   unsigned char b,
			   unsigned char c,
			   unsigned int baud,
			   unsigned char d,
			   unsigned char e,
			   unsigned char f,
			   unsigned char g,
			   unsigned char h) {
  buffer[0] = a;
  buffer[1] = b;
  buffer[2] = c;
  
  // SiRF ordering is MSB first?!
  buffer[3] = ((baud & 0xff000000) >> 24);
  buffer[4] = ((baud & 0x00ff0000) >> 16);
  buffer[5] = ((baud & 0x0000ff00) >>  8);
  buffer[6] = ((baud & 0x000000ff)      );
  
  buffer[7] = d;
  buffer[8] = e;
  buffer[9] = f;
  buffer[10] = g;
  buffer[11] = h;
  
  return 12;
}

// make a SiRF packet
packet_t *build_sirf_packet(int baud) {
  packet_t *rv;
  int i = 0;
  
  rv = allocate_packet(57);
  
  rv->buffer[i++] = 0xa0;
  rv->buffer[i++] = 0xa2;
  rv->buffer[i++] = 0x00; // packet length
  rv->buffer[i++] = 49;   // .............
  
  rv->buffer[i++] = 0xa5; // packet type
  
  i += sirf_write_line(&rv->buffer[i], 0, 0, 0, baud, 8, 1, 0, 0, 0);
  i += sirf_write_line(&rv->buffer[i], 255, 5, 5,  0, 0, 0, 0, 0, 0);
  i += sirf_write_line(&rv->buffer[i], 255, 5, 5,  0, 0, 0, 0, 0, 0);
  i += sirf_write_line(&rv->buffer[i], 255, 5, 5,  0, 0, 0, 0, 0, 0);
  
  i += sirf_write_checksum(&rv->buffer[4], 49);
  
  rv->buffer[i++] = 0xb0;
  rv->buffer[i++] = 0xb3;
  
  return rv;
}

// very low-level write packet, just so this class is self-containing
void write_packet(int fd, packet_t *packet, const char *desc, 
		  int baudat, int baudto) {
  int iwrote;
  
  if ((iwrote = write(fd, packet->buffer, packet->size)) != packet->size) {
    fprintf(stderr, "Error writing %s packet at speed %i\n", desc, baudat);
    if (iwrote == -1) {
      fprintf(stderr, "ERROR: %s\n", strerror(errno));
    } else {
      fprintf(stderr, "Only wrote %d bytes!\n", iwrote);
    }
  } else {
    fprintf(stdout, "%s -> %i @ %i\n", desc, baudto, baudat);
  }
}

void lowlevel_sync(int fd, int baud) {
  packet_t *nmea_packet, *sirf_packet;
  int supported_rates[5] = {  4800,  9600,  19200,  38400,  57600 };
  int i;
  
  nmea_packet = build_nmea_packet(baud);
  sirf_packet = build_sirf_packet(baud);
  
  for (i = 0; i < 5; i++) {
    set_reset_speed(fd, get_unistd_speed(supported_rates[i]));
    
    write_packet(fd, nmea_packet, "NMEA", supported_rates[i], baud);
    usleep(500000);
    write_packet(fd, sirf_packet, "SiRF", supported_rates[i], baud);
    usleep(500000);
    
  }
  
  free_packet(nmea_packet);
  free_packet(sirf_packet);
  
  set_reset_speed(fd, get_unistd_speed(baud));
}

// set and reset the speed
void set_reset_speed(int fd, speed_t baud) {
  struct termios config;

  // flush any unwritten data
  tcdrain(fd);
  
  // set the speed the first time
  tcgetattr(fd, &config);
  cfsetispeed(&config, baud);
  cfsetospeed(&config, baud);
  tcsetattr(fd, TCSANOW, &config);
  
  // sleep to make sure the other side has time to catch up
  usleep(100000);
  
  // reset the speed - for some reason. to get rid of garbage?
  tcgetattr(fd, &config);
  cfsetispeed(&config, baud);
  cfsetospeed(&config, baud);
  tcsetattr(fd, TCSANOW, &config);

  usleep(100000);

  // kill all the old data
  tcflush(fd, TCIOFLUSH);
}

// initialise the serial port
void lowlevel_init(int fd, int baud) {
  struct termios config;
  speed_t requested_baud = get_unistd_speed(baud);
  
  tcgetattr(fd, &config);
  cfsetispeed(&config, requested_baud);
  cfsetospeed(&config, requested_baud);
  cfmakeraw(&config); // set the port for raw mode
  config.c_cflag &= ~CSTOPB; // set one stop bit
  config.c_iflag &= ~INPCK; // don't check parity
  config.c_oflag &= ~OFILL; // don't send fill characters
  tcsetattr(fd, TCSANOW, &config);
}

// get the unistd-compatible speed from an actual speed
speed_t get_unistd_speed(int baud) {
  // this doesn't need to have all the speeds in it, since the SiRF 
  // chipset only supports a limited number anyway.
  switch (baud) {
  case 0:
    return B0;
  case 4800:
    return B4800;
  case 9600:
    return B9600;
  case 19200:
    return B19200;
  case 38400:
    return B38400;
  case 57600:
    return B57600;
  default:
    // unrecognised speed
    fprintf(stderr, "Unknown speed \"%d\"\n", baud);
    exit(-1);
    break;
  }
  return B0;
}

