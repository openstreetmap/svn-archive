#include <VisibleList.hpp>
#include <OkToSend.hpp>
#include <IOStream.hpp>
#include <PacketFactory.hpp>
#include <PrintPacketHandler.hpp>
#include <WarnPacketHandler.hpp>
#include <Signal.hpp>

#include "AuthorInfo.hpp"
#include "GPXHandler.hpp"

#include <iostream>
#include <pthread.h>
#include <signal.h>

// TODO: use getopt to set descriptions/names...

int main(int argc, char *argv[]) {
  int baud = 57600;

  /* check for correct usage
   */
  if (argc < 3) {
    std::cerr << "Usage: sirfheadless <input source> <output file> [baud]" 
	      << std::endl;
    return -1;
  }
  if (argc > 3) {
    baud = atoi(argv[3]);
  }

  /* set up signal handling
   */
  Signal::setup();

  /* get the file name from the first argument
   * initialise the SiRF stream
   */
  IOStream stream(argv[1], baud);

  /* make the packet factory
   */
  PacketFactory factory(stream);

  /* add the GPX writer handler
   */
  AuthorInfo author_info("Matt Amos", "matt@matt-amos.uklinux.net");
  GPXHandler gpx(argv[2], author_info);

  factory.registerHandler(&gpx);

  /* get good packets
   */
  factory.throwAwayPacketsUntilNice();

  /* enter main loop
   */
  factory.eventLoop();

  Signal::waitToBeTold();

  // rejoin the other thread
  factory.exitLoop();

  /* return
   */
  return 0;
}
