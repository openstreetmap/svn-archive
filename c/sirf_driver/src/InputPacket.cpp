#include "InputPacket.hpp"

namespace SiRF {

  InputStream &operator<<(InputStream &out, InputPacket &p) {
    p.output(out);
    return out;
  }

}
