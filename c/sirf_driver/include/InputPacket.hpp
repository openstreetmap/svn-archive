#ifndef INPUT_PACKET_H
#define INPUT_PACKET_H

/* this packet represents an input packet - that is one which
 * is sent TO the SiRF device
 */

#include "Packet.hpp"
#include "InputStream.hpp"

namespace SiRF {

  class InputPacket : public Packet {
  public:

    virtual void output(InputStream &out) const = 0;

  };

  /* output a packet to the device.
   */
  InputStream &operator<<(InputStream &out, const InputPacket &p);

}

#endif /* INPUT_PACKET_H */

