#ifndef GPX_HANDLER_H
#define GPX_HANDLER_H

#include <MeasuredNavigationDataOut.hpp>
#include <PacketHandler.hpp>

using namespace SiRF;

#include <cstdio>
#include "AuthorInfo.hpp"

class GPXHandler : virtual public PacketHandler<MeasuredNavigationDataOut> {
public:
  GPXHandler(const char *filename, const AuthorInfo &);
  ~GPXHandler();
  void handle(MeasuredNavigationDataOut p);
private:
  FILE *fh;
  const AuthorInfo &info;
  bool inTrack;
  unsigned int trackNumber;
  double minLat, minLon, maxLat, maxLon;

  void writeHeader();
  void writeFooter();
};

#endif /* GPX_HANDLER_H */
