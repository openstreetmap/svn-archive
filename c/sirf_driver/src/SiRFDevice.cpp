// include the header file
#include "SiRFDevice.hpp"

// need std::cerr
#include <iostream>

// include SiRF low-level commands and open, close, read & write syscalls
extern "C" {
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include "sirf_lowlevel.h"

  int errno;
}

namespace SiRF {

  // initialise
  SiRFDevice::SiRFDevice(const char *devicename, int baud) {
    // open the device
    open(devicename);
    // initialise
    lowlevel_init(fd, baud);
    // synchronise
    lowlevel_sync(fd, baud);
    // set up the buffer
    buf_size = 4096;
    buffer = new unsigned char[buf_size];
    buf_len = 0;
    buf_pos = 0;
  }
  
  // destroy
  SiRFDevice::~SiRFDevice() {
    // close the serial port
    close();
    // destroy the buffer
    delete [] buffer;
    buf_size = 0;
    buf_len = 0;
    buf_pos = 0;
  }

  // get a byte from the serial port
  SiRFDevice &SiRFDevice::operator>>(unsigned char &c) {
    // if we have enough data then use it
    if (buf_pos >= buf_len) {
      // but we don't, so get some more data
      refillBuffer();
    }
    c = buffer[buf_pos];
    buf_pos++;

    // send this device back for another go
    return (*this);
  }
  
  // open a serial port
  void SiRFDevice::open(const char *name) {
    // open the file descriptor
    fd = ::open(name, O_RDWR|O_NOCTTY|O_NONBLOCK, O_NDELAY);
    if (fd < 0) {
      std::cerr << "Error opening serial port: " 
		<< ::strerror(errno) << std::endl;
      exit(-1);
    }
  }

  // close a serial port
  void SiRFDevice::close() {
    if (::close(fd) != 0) {
      std::cerr <<"Error closing serial port: " 
		<< ::strerror(errno) << std::endl;
    }
  }

  // refills the buffer from the serial port
  void SiRFDevice::refillBuffer() {
    // we need some more data
    do {
      buf_len = ::read(fd, buffer, buf_size);
      if (buf_len < 0) {
	if (errno != EAGAIN) {
	  // some error other than time-out on data
	  std::cerr << "Error reading data from serial port!" << std::endl;
	}
	usleep(100000); // sleep - maybe it will all go away!
      }
    } while (buf_len < 0);
    // buf_len is now >= 0
    if (buf_len == 0) {
      std::cerr << "End of file!" << std::endl;
      exit(-1);
    }
    // buf_len > 0
    buf_pos = 0;
  }

}

