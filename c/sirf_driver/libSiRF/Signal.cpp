#include <Message.hpp>
#include <Signal.hpp>
#include <signal.h>
#include <unistd.h>
#include <pthread.h>
#include <semaphore.h>

namespace SiRF {

  static sem_t lock;
  pthread_t Signal::mainThread;
  pthread_t Signal::waitFor;
  bool Signal::shuttingDown;

  void Signal::killme(int signo) {
    Message::critical("timeout. possible device disconnect?");
    tellMainThread();
  }

  void Signal::tellMainThread() {
    shuttingDown = true;
    waitFor = pthread_self();
    sem_post(&lock);
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
      Message::critical("unable to install a signal handler!\n");
      exit(-1);
    }

    sem_init(&lock, 0, 0);

    // the thread calling this has to be the main thread
    mainThread = pthread_self();

    // we're not shutting down yet
    shuttingDown = false;
  }

  void Signal::setWatchdog(unsigned int timeout) {
    alarm(timeout);
  }

  void Signal::resetWatchdog() {
    alarm(0);
  }

  pthread_t Signal::toldMainThread() {
    return waitFor;
  }

  void Signal::waitToBeTold() {
    sem_wait(&lock);
    sem_destroy(&lock);
  }
}
