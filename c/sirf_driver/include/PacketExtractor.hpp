#ifndef PACKET_EXTRACTOR_H
#define PACKET_EXTRACTOR_H

#include "PacketHandler.hpp"
#include "PacketExtractorBase.hpp"
#include <vector>

namespace SiRF {

  template <class T>
  class PacketExtractor : public PacketExtractorBase {

    // a vector of packet handlers of this type
    typedef typename std::vector<PacketHandler<T> *> PHVector;

  public:

    // get pointer to the packet we hold
    T *getPacket() {
      return &packet;
    }

    // add another handler
    void add(PacketHandler<T> *ph) {
      handlers.push_back(ph);
    }

    // handle a packet, after it has been read in
    void handle() {
      for (typename PHVector::iterator itr = handlers.begin(); 
	   itr != handlers.end(); ++itr) {
	(*itr)->handle(packet);
      }
    }

  private:

    // vector we hold of PacketHandlers for this type
    PHVector handlers;
    
    // packet of this type
    T packet;

  };

}

#endif /* PACKET_EXTRACTOR_H */
