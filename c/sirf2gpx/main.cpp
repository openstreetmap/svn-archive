#include <VisibleList.hpp>
#include <OkToSend.hpp>
#include <IOStream.hpp>
#include <PacketFactory.hpp>
#include <PrintPacketHandler.hpp>
#include <WarnPacketHandler.hpp>

#include "AuthorInfo.hpp"
#include "GPXHandler.hpp"
#include "SetupGPX.hpp"

#include <iostream>

int main(int argc, char *argv[]) {
  int baud = 57600;

  /* check for correct usage
   */
  if (argc < 3) {
    std::cerr << "Usage: sirf2gpx <input source> <output file> [baud]" 
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

  /* add the handlers
   */
  //factory.registerHandler(new PrintPacketHandler<VisibleList>);
  //factory.registerHandler(new PrintPacketHandler<OkToSend>);
  //factory.registerDefaultHandler(new WarnPacketHandler);

  /* add the GPX writer handler
   */
  AuthorInfo author_info("Matt Amos", "matt@matt-amos.uklinux.net");
  GPXHandler gpx(argv[2], author_info);
  GPXReporterUI ui;
  SetupGPX setup(factory, gpx, ui);
  factory.registerHandler(&setup);

  /* enter main loop
   */
  factory.eventLoop();

  // wait to quit
  ui.waitToQuit();

  // rejoin the other thread
  factory.exitLoop();

  /* return
   */
  return 0;
}
