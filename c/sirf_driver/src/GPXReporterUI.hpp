#ifndef GPX_REPORTER_UI_H
#define GPX_REPORTER_UI_H

#include <MeasuredNavigationDataOut.hpp>
#include <MeasuredTrackerDataOut.hpp>
#include <PacketHandler.hpp>
#include <Message.hpp>
#include <Signal.hpp>

using namespace SiRF;

#include <string>
#include <list>

class GPXReporterUI : virtual public PacketHandler<MeasuredNavigationDataOut>,
		      virtual public PacketHandler<MeasuredTrackerDataOut>,
		      virtual public MessageHandler {
public:
  GPXReporterUI();
  ~GPXReporterUI();
  void handle(MeasuredNavigationDataOut p);
  void handle(MeasuredTrackerDataOut p);
  void handleMessage(const char *);
  void shutdownDisplay();
  void setStatus(const std::string &str);
  static void *pthreadFunction(void *);
  void startThread();
  void joinThread();
  void waitToQuit();
  pthread_t threadID() {
    return thread;
  }
private:
  void updateUI() const;

  std::string status;
  double lat, lon, alt;
  std::string datestring, fixstring;
  int nSats, nTracks;
  std::list<std::string> messages;
  bool inTrack;
  MeasuredTrackerDataOut lastTracker;
  pthread_t thread;
};

#endif /* GPX_REPORTER_UI_H */
