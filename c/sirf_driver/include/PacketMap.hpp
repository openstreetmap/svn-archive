#ifndef PACKET_MAP_H
#define PACKET_MAP_H

#include "PacketFactory.hpp"
#include "PacketHandler.hpp"

namespace SiRF {
  
  template <unsigned char C, class T>
  class PacketMap {

  public:

    /* packet handler type
     */
    typedef std::vector<PacketHandler<T> *> PHList;
    
    /* statically map the correct Packet type to that packet's
     * initialisation function.
     */
    PacketMap<C, T>() {
      /* map it into PacketFactory
       */
      PacketFactory::mapTypeToInit(C, &readPacket);
    }

    /* allocate and return the correct packet type
     */
    static void readPacket(Stream &in) {
      T t;

      // read in the packet
      in >> t;

      // notify all the notifiers 
      // somehow - got to thread this?
      for (typename PHList::iterator itr = handlers.begin(); 
	   itr != handlers.end(); ++itr) {
	itr->handle(t);
      }
    }
    
    /* add a notifier... packet handler?
     */
    static void addNotifier(PacketHandler<T> *ph) {
      handlers.push_back(ph);
    }

  private:

    /* a packet handler vector
     */
    static PHList handlers;
   
  };

}

#endif /* PACKET_MAP_H */
