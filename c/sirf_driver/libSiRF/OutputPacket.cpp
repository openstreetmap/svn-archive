#include "OutputPacket.hpp"

namespace SiRF {

  OutputStream &operator>>(OutputStream &in, OutputPacket &p) {
    p.input(in);
    return in;
  }

}
