#include "PacketFactory.hpp"
#include "Packet.hpp"
#include "Exception.hpp"

#include <MeasuredNavigationDataOut.hpp>
#include <MeasuredTrackerDataOut.hpp>
#include <CPUThroughput.hpp>
#include <DifferentialData.hpp>
#include <Message.hpp>
#include <Debug.hpp>

#include <vector>
#include <iostream>

#include "config.h"

#ifndef HAVE_PTHREAD_H
#error I need pthreads
#endif

#ifndef HAVE_SEM_H
#error I need semaphores
#endif

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

  /* enter the threaded event loop
   */
  void PacketFactory::eventLoop() {
    if (Debug::isSingleThreaded()) {
      threadedEventLoop();
    } else {
      ::sem_init(&lock, 0, 0);
      ::pthread_create(&thread, NULL, &stupidHackedUpPthreadFunction, this);
    }
  }

  /* exit the threaded event loop
   */
  void PacketFactory::exitLoop() {
    if (!Debug::isSingleThreaded()) {
      ::sem_post(&lock);
      ::pthread_join(thread, NULL);
      ::sem_destroy(&lock);
    }
  }

  void *PacketFactory::stupidHackedUpPthreadFunction(void *ptr) {
    // stupid function!
    ((PacketFactory*)ptr)->threadedEventLoop();
    return NULL;
  }

  /* enters the main event loop, grabbing packets and 
   * firing them off to the PacketHandlers
   */
  void PacketFactory::threadedEventLoop() {
    uint8 type;
    OutputPacket *packet;

    // at some point when i thread this class this will be a lock,
    // so that another thread can make this one safely exit at a
    // packet boundary.
    while (goodToContinue()) {
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
	Message::warn("PacketFactory::eventLoop: caught exception!");
	Message::warn(e.what());
      }
    }

    // we never actually get here
    // but we might when the pthread locking is written...
  }

  /* is it good to continue?
   */
  bool PacketFactory::goodToContinue() {
    int i;

    if (Debug::isSingleThreaded()) {
      i = 0;
    } else {
      ::sem_getvalue(&lock, &i);
    }

    return (i == 0);
  }

  /* throw away packets until we have 5 nice ones in a row
   */
  void PacketFactory::throwAwayPacketsUntilNice() {
    int nice_count = 0;
    UnknownPacket p;
    uint8 type;

    do {
      try {
	in >> Stream::start >> type;
	p.setType(type);
	in >> p;
	in >> Stream::end;
	nice_count++;
	if (Debug::isDebug()) {
	  Message::info("Got a nice packet, packet count %i", nice_count);
	}
      } catch (std::exception &e) {
	Message::warn("PacketFactory::throwAwayPacketsUntilNice: caught exception.");
	Message::warn(e.what());
	nice_count = 0;
      }
    } while (nice_count < 5);

    Message::info("PacketFactory::throwAwayPacketsUntilNice: got 5 nice packets in a row.");
  }
  
}

