#ifndef UNKNOWN_PACKET_H
#define UNKNOWN_PACKET_H

#include "OutputPacket.hpp"
#include <vector>

namespace SiRF {
  
  class UnknownPacket : public OutputPacket {
  public:

    // get our type, which may be variable!
    uint8 getType() { return type; }

    // but we are an output packet
    bool isInput() { return false; }

    // standard printing function
    void output(std::ostream &out) const;

    // binary printing function
    void outputBinary(std::ostream &out) const;

    // input from a stream
    void input(OutputStream &in);

    // set the given type
    void setType(uint8 t) { type = t; }

  private:

    /* our type, as read from the stream
     */
    uint8 type;

    /* hold the payload data
     */
    std::vector<uint8> payload;

  };

}

#endif /* UNKNOWN_PACKET_H */
