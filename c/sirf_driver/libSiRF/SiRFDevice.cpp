// include the header file
#include <SiRFDevice.hpp>
#include <Message.hpp>
#include <Signal.hpp>
#include <Exception.hpp>

// need std::cerr
#include <iostream>

// include SiRF low-level commands and open, close, read & write syscalls
extern "C" {
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include "sirf_lowlevel.h"
}

//extern int errno;

namespace SiRF {

  // initialise
  SiRFDevice::SiRFDevice(const char *devicename, unsigned int baud) {
    // setup a time-out for 60 s
    Signal::setWatchdog(60);
    // open the device
    open(devicename);
    // initialise
    lowlevel_init(fd, baud);
    // synchronise
    lowlevel_sync(fd, baud);
    // all done, reset watchdog
    Signal::resetWatchdog();
    // set up the buffer
    read_buf_size = 4096;
    write_buf_size = 4096;
    read_buffer = new uint8[read_buf_size];
    write_buffer = new uint8[read_buf_size];
    read_buf_len = 0;
    read_buf_pos = 0;
    write_buf_pos = 0;
  }
  
  // destroy
  SiRFDevice::~SiRFDevice() {
    // close the serial port
    close();
    // destroy the buffer
    delete [] read_buffer;
    delete [] write_buffer;
    read_buf_size = 0;
    read_buf_len = 0;
    read_buf_pos = 0;
    write_buf_size = 0;
    write_buf_pos = 0;
  }

  // get a byte from the serial port
  SiRFDevice &SiRFDevice::operator>>(uint8 &c) {
    // if we have enough data then use it
    if (read_buf_pos >= read_buf_len) {
      // but we don't, so get some more data
      refillReadBuffer();
    }
    c = read_buffer[read_buf_pos];
    read_buf_pos++;

    // send this device back for another go
    return (*this);
  }
  
  // get a byte from the serial port
  SiRFDevice &SiRFDevice::operator<<(uint8 &c) {
    // if we have enough data then use it
    write_buffer[write_buf_pos] = c;
    write_buf_pos++;
    if (write_buf_pos >= write_buf_size) {
      flushWriteBuffer();
    }

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
  void SiRFDevice::refillReadBuffer() {
    // shouldn't take more than 30s to get more data, right?
    Signal::setWatchdog(10);
    // we need some more data
    do {
      read_buf_len = ::read(fd, read_buffer, read_buf_size);
      if (read_buf_len < 0) {
	if (errno != EAGAIN) {
	  // some error other than time-out on data
	  Message::critical("Error reading data from serial port!");
	}
	if (Signal::shuttingDown) {
	  // interrupted while reading, we should exit cleanly
	  read_buf_len = 0;
	  read_buf_pos = 0;
	  Message::info("about to throw interrupted exception");
	}
	usleep(100000); // sleep - maybe it will all go away!
      }
    } while (read_buf_len < 0);
    // buf_len is now >= 0
    if (read_buf_len == 0) {
      Message::critical("End of file!");
      exit(-1);
    }
    // buf_len > 0
    read_buf_pos = 0;
    // mission complete.
    Signal::resetWatchdog();
  }

  // flushes the write buffer to the device
  void SiRFDevice::flushWriteBuffer() {
    // flush write buffer to device
    int length = 0;
    do {
      int rv;
      rv = ::write(fd, &write_buffer[length], (write_buf_pos - length));
      if (rv == -1) {
	// error
      } else {
	// update the length
	length += rv;
      }
    } while (length < write_buf_pos);
    // reset the position
    write_buf_pos -= length;
  }

  // sync and flush the device
  void SiRFDevice::flush() {
    // first flush all data so far
    flushWriteBuffer();
    // now sync the device
    ::tcdrain(fd);
  }

}

