#ifndef OUTPUT_PACKET_H
#define OUTPUT_PACKET_H

/* this represents an output packet, that is: output FROM the SiRF
 * device
 */

#include "Packet.hpp"
#include "OutputStream.hpp"

namespace SiRF {

  class OutputPacket : public Packet {
  public:

    /* input a packet from a Stream
     */
    virtual void input(OutputStream &in) = 0;

  };

  /* allow operator use with virtual. this seems the cleanest way
   * to do it.
   */
  OutputStream &operator>>(OutputStream &in, OutputPacket &p);

}

#endif /* OUTPUT_PACKET_H */
