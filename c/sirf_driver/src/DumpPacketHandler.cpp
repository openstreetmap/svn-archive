#include "DumpPacketHandler.hpp"

namespace SiRF {

  /* open with a file
   */
  DumpPacketHandler::DumpPacketHandler(const char *filename) : file(filename) {
  }
  
  /* close the file
   */
  DumpPacketHandler::~DumpPacketHandler() {
    file.close();
  }
  
  /* interface to handle a packet
   */
  void DumpPacketHandler::handle(UnknownPacket p) {
    p.outputBinary(file);
    file.flush();
  }

}
