#include <IOStream.hpp>
#include <PacketFactory.hpp>
#include <PrintPacketHandler.hpp>
#include <PrintAllHandler.hpp>
#include <OkToSend.hpp>
#include <Debug.hpp>

#include <iostream>

using namespace SiRF;

int main(int argc, char *argv[]) {
  int baud = 57600;

  Debug::setDebug(true);
  Debug::setSingleThreaded(true);

  /* check for correct usage
   */
  if (argc < 3) {
    std::cerr << "Usage: sirfdebug <input source> <output file> [baud]" 
	      << std::endl;
    return -1;
  }
  if (argc > 3) {
    baud = atoi(argv[3]);
  }

  /* get the file name from the first argument
   * initialise the SiRF stream
   */
  IOStream stream(argv[1], baud);

  /* make the packet factory
   */
  PacketFactory factory(stream);

  // print all the packets
  factory.registerHandler(new PrintPacketHandler<OkToSend>);

  // print all the packets
  factory.registerDefaultHandler(new PrintAllHandler);

  /**
   *
   */
  factory.throwAwayPacketsUntilNice();

  /* enter main loop
   */
  factory.eventLoop();
  factory.exitLoop();

  return 0;
}
