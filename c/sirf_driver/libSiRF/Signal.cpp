#include <Signal.hpp>
#include <signal.h>
#include <unistd.h>

namespace SiRF {

  static void killme(int signo) {
    fprintf(stderr, "ERROR: timeout. possible device disconnect?\n");
    exit(-1);
  }

  void Signal::setup() {
    struct sigaction act, oact;

    act.sa_handler = killme;
    sigemptyset(&act.sa_mask);
    act.sa_flags = 0;
#ifdef SA_INTERRUPT
    act.sa_flags |= SA_INTERRUPT;
#endif
    if (sigaction(SIGALRM, &act, &oact) < 0) {
      fprintf(stderr, "ERROR: unable to install signal handler!\n");
      exit(-1);
    }
  }

  void Signal::setWatchdog(unsigned int timeout) {
    alarm(timeout);
  }

  void Signal::resetWatchdog() {
    alarm(0);
  }

}
