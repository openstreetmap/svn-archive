#ifndef PACKET_H
#define PACKET_H

/*
 * class to encapsulate a SiRF packet and validate it.
 *
 */

#include <ostream>

namespace SiRF {

  /* This class looks after reading, writing, packing, unpacking and
   * validating packets as they come to and from the SiRF device.
   */
  class Packet {

  public:
    
    /* what type of packet is this?
     */
    virtual unsigned char getType() = 0;

    /* should we be sending this packet to the SiRF?
     */
    virtual bool isInput() = 0;

    /* output a packet
     */
    virtual void output(std::ostream &out) const = 0;

  private:

  };

  std::ostream &operator<<(std::ostream &out, const Packet &p);

}

#endif /* PACKET_H */
