#ifndef PACKET_HANDLER_H
#define PACKET_HANDLER_H

#include "OutputPacket.hpp"

namespace SiRF {

  /* this class embodies packet handling. you may be wondering why
   * you have to get a packet and then handle it later, so here is 
   * the answer: because its good for PacketHandlers not to know
   * about the details of Stream and because it gives PacketFactory
   * a chance to do some error checking before allowing you to handle
   * the packet, so avoiding confusion with corrupt packets.
   */
  template <class T>
  class PacketHandler {
  public:
    
    /* interface to handle a packet
     */
    virtual void handle(T) = 0;

  };

}

#endif /* PACKET_HANDLER_H */
