#ifndef PRINT_ALL_HANDLER_H
#define PRINT_ALL_HANDLER_H

#include <PacketHandler.hpp>
#include <UnknownPacket.hpp>
#include <Message.hpp>
#include <sstream>

namespace SiRF {

  class PrintAllHandler : public PacketHandler<UnknownPacket> {
  public:
    /* set self up by adding to notifier ring
     */
    void handle(UnknownPacket packet) {
      std::ostringstream str;
      str << packet;
      // this next line is really ugly...
      Message::info(str.str().c_str());
    }
  };

}

#endif /* PRINT_ALL_HANDLER_H */
