#ifndef UNKNOWN_PACKET_H
#define UNKNOWN_PACKET_H

#include "OutputPacket.hpp"

namespace SiRF {
  
  class UnknownPacket : public OutputPacket {
  public:

    // get our type, which may be variable!
    uint8 getType() { return type; }

    // but we are an output packet
    bool isInput() { return false; }

    // standard printing function
    void output(std::ostream &out) const;

    // input from a stream
    void input(Stream &in);

    // set the given type
    void setType(uint8 t) { type = t; }

  private:

    /* our type, as read from the stream
     */
    uint8 type;

  };

}

#endif /* UNKNOWN_PACKET_H */
