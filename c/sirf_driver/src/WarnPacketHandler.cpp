#include "WarnPacketHandler.hpp"
#include <iostream>

namespace SiRF {
  
  /* handle a packet of unknown type - this will be passed to us 
   * from the PacketFactory and we should do with it whatever we 
   * want. usually we'll print a warning message.
   */
  void WarnPacketHandler::handle(UnknownPacket packet) {
    std::cerr << "WarnPacketHandler: unknown packet type " 
	      << (int)packet.getType() << " "
	      << std::hex << (int)packet.getType() << std::dec
	      << std::endl;
  }

}
