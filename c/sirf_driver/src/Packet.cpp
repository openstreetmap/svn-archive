#include "Packet.hpp"

namespace SiRF {

  /* output the packet to an ostream
   */
  std::ostream &operator<<(std::ostream &out, const Packet &p) {
    p.output(out);
    return out;
  }

}
