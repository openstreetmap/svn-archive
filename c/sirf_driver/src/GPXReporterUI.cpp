#include "GPXReporterUI.hpp"
#include "Coordinates.hpp"
#include "config.h"

#include <Message.hpp>

#ifdef HAVE_CURSES_H
extern "C" {
#include <curses.h>
#include <string.h>
#include <errno.h>
  extern int errno;
}
#else
#error You must have a Curses library installed to compile this file!
#endif /* HAVE_CURSES_H */

GPXReporterUI::GPXReporterUI() {
  // set the initial status
  status = "Initialising interface";

  // set up curses interface
  initscr(); cbreak(); noecho();
  // some more setup
  nonl(); intrflush(stdscr, FALSE); keypad(stdscr, TRUE);

  lat = 0.0; lon = 0.0; alt = 0.0;
  nSats = nTracks = 0;
  inTrack = false;
  memset(&lastTracker, 0, sizeof(MeasuredTrackerDataOut));

  // setup self as message handler
  Message::setupHandler(this);

  // add the status line
  updateUI();
}

GPXReporterUI::~GPXReporterUI() {
  // destroy curses and reset term
  endwin();
}

void GPXReporterUI::setStatus(const std::string &str) {
  status = str;
  
  updateUI();
}

void GPXReporterUI::handle(MeasuredNavigationDataOut p) {
  char tmp_status[100];

  Coordinates::convertToLLA(p.getX(), p.getY(), p.getZ(),
			    lat, lon, alt);
  datestring = 
    Coordinates::convertToTimeString(p.getWeek() + 1024, p.getTimeOfWeek());
  fixstring = Coordinates::convertToFix(p.getMode1() & 7);
  nSats = p.getSatellites();

  if ((p.getMode1() & 7) > 0) {
    if (!inTrack) {
      snprintf(tmp_status, 99, "Acquired track (%d)\n", nTracks++);
      status = tmp_status;
      inTrack = true;
    }
  } else {
    if (inTrack) {
      status = "Lost track";
      inTrack = false;
    }
  }

  updateUI();
}

void GPXReporterUI::handle(MeasuredTrackerDataOut p) {
  lastTracker = p;

  updateUI();
}

void GPXReporterUI::handleMessage(const char *msg) {
  messages.push_back(std::string(msg));
  if (messages.size() > 7) {
    messages.pop_front();
  }
  std::cout << "got message: " << msg << std::endl;

  updateUI();
}

static char *meanings[] = {
  "Not acquired",
  "Acquired",
  "Carrier phase valid",
  "Bit sync done",
  "Subframe sync done",
  "Carrier pull-in done",
  "Code locked",
  "Acquisition failed",
  "Got ephemeris"
};

void GPXReporterUI::updateUI() const {
  int line = 0;
  // clear the window
  clear();
  // add the status line
  mvprintw(line++, 0, "STATUS: %s", status.c_str());
  // add the other lines
  mvprintw(line++, 0, "Date:   %s", datestring.c_str());
  mvprintw(line++, 0, "Fix:    %s", fixstring.c_str());
  mvprintw(line++, 0, "nSats:  %d", nSats);
  mvprintw(line++, 0, "Pos:    % 10.7f, % 10.7f, % 10.5f", lon, lat, alt);
  // do the tracker
  for (int i = 0; i < lastTracker.getChannels(); i++) {
    TrackerSatelliteData data = lastTracker.getSatellites(i);
    unsigned int state = data.getState();
    unsigned int farth = 0;
    unsigned int str = 0;

    if (data.getID() > 0) {
      for (int j = 0; j < 10; j++) {
	if (data.getChannelNumber(j) > str) {
	  str = data.getChannelNumber(j);
	}
      }
      
      mvprintw(line++, 0, "%2d: %3d %03x ", data.getID(), str, state);

      for (int j = 0; j < 8; j++) {
	if ((state & (1 << j)) > 0) {
	  addch('*');
	  farth = j+1;
	} else {
	  addch('.');
	}
      }
      addstr(" - ");
      addstr(meanings[farth]);
    }
  }
  // do the other messages
  line = 16;
  for (std::list<std::string>::const_iterator itr = messages.begin();
       itr != messages.end(); ++itr) {
    mvprintw(line++, 0, "%s", itr->c_str());
  }
  // and put it to the screen
  refresh();
}

void *GPXReporterUI::pthreadFunction(void *ptr) {
  ((GPXReporterUI*)ptr)->waitToQuit();
  return NULL;
}

void GPXReporterUI::startThread() {
  pthread_create(&thread, NULL, &pthreadFunction, this);
}

void GPXReporterUI::joinThread() {
  pthread_join(thread, NULL);
}
 
void GPXReporterUI::waitToQuit() {
  int i;

  while (i != 'q') {
    i = getch();

    if (i == ERR) {
      // tell whats happening
      Message::info("getch() returned ERR, errno = %d\n", errno);
    }

    if (i == 'u') {
      // update the screen!
      updateUI();
      refresh();
    }
  }

  Signal::tellMainThread();
}

void GPXReporterUI::shutdownDisplay() {
  // destroy curses and reset term
  endwin();
}

