#ifndef PRINT_ALL_HANDLER_H
#define PRINT_ALL_HANDLER_H

#include <iostream>
#include <vector>
#include "PacketMap.hpp"
#include "PacketHandler.hpp"

namespace SiRF {

  template <class T>
  class PrintAllHandler : public PacketHandler<T> {
  public:
    /* set self up by adding to notifier ring
     */
    PrintAllHandler<T>() {
      PacketMap<0x0, T>::addNotifier(this);
    }

    /* handle a packet by sending it to std::cout
     */
    void handle(const T &packet) {
      std::cout << packet << std::endl;
    }
  };

}

#endif /* PRINT_ALL_HANDLER_H */
