#include "OutputPacket.hpp"

namespace SiRF {

  Stream &operator>>(Stream &in, OutputPacket &p) {
    p.input(in);
    return in;
  }

}
