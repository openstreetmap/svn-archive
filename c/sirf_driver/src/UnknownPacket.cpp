#include "UnknownPacket.hpp"

namespace SiRF {

  // standard printing function
  void UnknownPacket::output(std::ostream &out) const {
    out << "!!! Unknown packet type = " << (int)type << " !!!";
  }
  
  // input from a stream
  void UnknownPacket::input(Stream &in) {
    uint8 c;
    // gobble all the bytes from the stream
    while (in.remainingLength() > 0) {
      in >> c;
    }
  }

}
