#ifndef DUMP_PACKET_HANDLER_H
#define DUMP_PACKET_HANDLER_H

#include "UnknownPacket.hpp"
#include "PacketHandler.hpp"
#include <fstream>

namespace SiRF {

  /* this class dumps packets to a file
   */
  class DumpPacketHandler : public PacketHandler<UnknownPacket> {
  public:
    
    /* open with a file
     */
    DumpPacketHandler(const char *filename);

    /* close the file
     */
    ~DumpPacketHandler();

    /* interface to handle a packet
     */
    void handle(UnknownPacket);

  private:
    
    /* backing file
     */
    std::ofstream file;
  };

}

#endif /* DUMP_PACKET_HANDLER_H */
