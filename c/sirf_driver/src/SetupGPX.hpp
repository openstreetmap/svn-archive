#ifndef SETUP_GPX_H
#define SETUP_GPX_H

#include <OkToSend.hpp>
#include <PacketHandler.hpp>
#include <PacketFactory.hpp>

using namespace SiRF;

#include "GPXHandler.hpp"
#include "GPXReporterUI.hpp"

class SetupGPX : public PacketHandler<OkToSend> {
public:
  SetupGPX(PacketFactory &f, GPXHandler &h, GPXReporterUI &u) 
    : factory(f), handler(h), UI(u) {
    isSetup = false;
  }
  void handle(OkToSend p);
private:
  bool isSetup;
  PacketFactory &factory;
  GPXHandler &handler;
  GPXReporterUI &UI;
};

#endif /* SETUP_GPX_H */
