/* standard little main program
 */

#include "IOStream.hpp"
#include "OutputPacket.hpp"
#include "InputPacket.hpp"

#include <MeasuredNavigationDataOut.hpp>
#include <MeasuredTrackerDataOut.hpp>
#include <CPUThroughput.hpp>
#include <SoftwareVersion.hpp>
#include <PollSoftwareVersion.hpp>
#include <CommandAcknowledgement.hpp>
#include <VisibleList.hpp>

#include "PacketFactory.hpp"
#include "PrintPacketHandler.hpp"
#include "WarnPacketHandler.hpp"
#include "NullPacketHandler.hpp"
#include "DumpPacketHandler.hpp"

#include <iostream>

using namespace SiRF;

int main(int argc, char *argv[]) {

  /* check for correct usage
   */
  if (argc < 2) {
    std::cerr << "Usage: sirfdemo <input source>" << std::endl;
    return -1;
  }

  /* get the file name from the first argument
   * initialise the SiRF stream
   */
  IOStream stream(argv[1]);

  /* make the packet factory
   */
  PacketFactory factory(stream);

  /* add the handlers
   */
  factory.registerHandler(new PrintPacketHandler<MeasuredNavigationDataOut>);
  factory.registerHandler(new NullPacketHandler<MeasuredTrackerDataOut>);
  factory.registerHandler(new NullPacketHandler<CPUThroughput>);
  factory.registerHandler(new PrintPacketHandler<CommandAcknowledgement>);
  factory.registerHandler(new PrintPacketHandler<VisibleList>);
  factory.registerDefaultHandler(new WarnPacketHandler);
  factory.registerDefaultHandler(new DumpPacketHandler("dump.out"));
  factory.registerHandler(new PrintPacketHandler<SoftwareVersion>);

  factory.throwAwayPacketsUntilNice();

  PollSoftwareVersion req;

  std::cout << "Sending version request" << std::endl;
  stream << Stream::start << req.getType();
  req.output(stream);
  stream << Stream::end;
  //stream.flush();
  std::cout << "Sent" << std::endl;

  /* make the printer
   */
  factory.eventLoop();
      
  /* return
   */
  return 0;

}
