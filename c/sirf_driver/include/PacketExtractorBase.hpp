#ifndef PACKET_EXTRACTOR_BASE_H
#define PACKET_EXTRACTOR_BASE_H

namespace SiRF {

  class PacketExtractorBase {
  public:

    virtual OutputPacket *getPacket() = 0;
    virtual void handle() = 0;

  };

}

#endif /* PACKET_EXTRACTOR_BASE_H */
