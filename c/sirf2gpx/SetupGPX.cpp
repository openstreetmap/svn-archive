#include "SetupGPX.hpp"
#include <iostream>

#include <MeasuredNavigationDataOut.hpp>
#include <MeasuredTrackerDataOut.hpp>

void SetupGPX::handle(OkToSend p) {
  if (isSetup == false) {
    //std::cout << "Setting up GPX handler." << std::endl;
    factory.registerHandler(&handler);
    factory.registerHandler(static_cast<PacketHandler<MeasuredNavigationDataOut>*>(&UI));
    factory.registerHandler(static_cast<PacketHandler<MeasuredTrackerDataOut>*>(&UI));
    UI.setStatus("GPS system set up");
    isSetup = true;
  }
}
