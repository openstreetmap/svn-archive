#include "PacketFactory.hpp"
#include "Packet.hpp"
#include "Exception.hpp"

#include <MeasuredNavigationDataOut.hpp>
#include <MeasuredTrackerDataOut.hpp>
#include <CPUThroughput.hpp>
#include <DifferentialData.hpp>

#include <vector>
#include <iostream>

namespace SiRF {

  /* construct self using the given stream
   */
  PacketFactory::PacketFactory(OutputStream &_in) : in(_in) {
    /* code here to initialise the sirf device?
     * or should that go in Stream?
     */
  }

  /* returns a newly allocated packet of type "type"
   */
  OutputPacket *PacketFactory::getNewPacketForType(uint8 type) {
    switch (type) {
    case MeasuredNavigationDataOut::type:
      return new MeasuredNavigationDataOut;
    case MeasuredTrackerDataOut::type:
      return new MeasuredTrackerDataOut;
    case CPUThroughput::type:
      return new CPUThroughput;
    case DifferentialData::type:
      return new DifferentialData;
    }
    return NULL;
  }

  /* get a packet from the underlying stream
   */
  OutputPacket *PacketFactory::getPacket() {
    uint8 type;
    OutputPacket *p;

    in >> Stream::start;
    in >> type;
    p = getNewPacketForType(type);
    if (p == NULL) {
      throw UnknownPacketTypeException(type);
    }
    in >> *p;
    in >> Stream::end;

    return p;
  }

  /* enters the main event loop, grabbing packets and 
   * firing them off to the PacketHandlers
   */
  void PacketFactory::eventLoop() {
    uint8 type;
    OutputPacket *packet;

    // at some point when i thread this class this will be a lock,
    // so that another thread can make this one safely exit at a
    // packet boundary.
    while (true) {
      try {
	in >> Stream::start >> type;

	// if there are no registered packet handlers then 
	// use the default
	if (extractors.count(type) == 0) {
	  UnknownPacket *upacket = defaultHandlers.getPacket();
	  // set the type
	  upacket->setType(type);
	  // attach it to what we're expecting
	  packet = upacket;
	} else {
	  // we have handlers, perhaps more than one
	  packet = extractors[type]->getPacket();
	}

	// read in the packet
	in >> *packet;
	
	// end of the packet
	in >> Stream::end;

	// deal with the packet
	if (extractors.count(type) == 0) {
	  // use default handlers
	  defaultHandlers.handle();
	} else {
	  extractors[type]->handle();
	}

      } catch (std::exception &e) {
	std::cerr << "PacketFactory::eventLoop: caught exception!" << std::endl
		  << e.what() << std::endl;
      }
    }

    // we never actually get here
    // but we might when the pthread locking is written...
  }
  
}

