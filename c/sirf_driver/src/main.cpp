/* standard little main program
 */

#include "IOStream.hpp"
#include "OutputPacket.hpp"

#include <MeasuredNavigationDataOut.hpp>
#include <MeasuredTrackerDataOut.hpp>
#include <CPUThroughput.hpp>

#include "PacketFactory.hpp"
#include "PrintPacketHandler.hpp"
#include "WarnPacketHandler.hpp"
#include "NullPacketHandler.hpp"

#include <iostream>

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
  SiRF::IOStream stream(argv[1]);

  /* make the packet factory
   */
  SiRF::PacketFactory factory(stream);

  /* add the handlers
   */
  factory.registerHandler(new SiRF::PrintPacketHandler<SiRF::MeasuredNavigationDataOut>);
  factory.registerHandler(new SiRF::NullPacketHandler<SiRF::MeasuredTrackerDataOut>);
  factory.registerHandler(new SiRF::PrintPacketHandler<SiRF::CPUThroughput>);
  factory.registerDefaultHandler(new SiRF::WarnPacketHandler);

  /* make the printer
   */
  factory.eventLoop();
      
  /* return
   */
  return 0;

}
