#include "UnknownPacket.hpp"

namespace SiRF {

  // standard printing function
  void UnknownPacket::output(std::ostream &out) const {
    out << "!!! Unknown packet type = " << (int)type << " !!!";
  }
  
  // binary printing function
  void UnknownPacket::outputBinary(std::ostream &out) const {
    // reconstruct the SiRF stream (kind of) we won't bother with
    // the checksum
    out << (uint8)0xa0 << (uint8)0xa2 << (uint8)((payload.size() + 1) >> 8)
	<< (uint8)((payload.size() + 1) & 0xff);
    out << type;
    for (std::vector<uint8>::const_iterator itr = payload.begin();
	 itr != payload.end(); ++itr) {
      out << *itr;
    }
    out << (uint8)0xff << (uint8)0xff // no need for a real checksum
	<< (uint8)0xb0 << (uint8)0xb3;
  }
  
  // input from a stream
  void UnknownPacket::input(OutputStream &in) {
    uint8 c;
    payload.clear();
    payload.reserve(in.remainingLength());
    // gobble all the bytes from the stream
    while (in.remainingLength() > 0) {
      in >> c;
      payload.push_back(c);
    }
  }

}
