#ifndef SIGNAL_H
#define SIGNAL_H

namespace SiRF {

  class Signal {
  public:
    /**
     * set up signal handling
     */
    static void setup();

    /**
     * set a watchdog timer
     */
    static void setWatchdog(unsigned int);

    /**
     * reset the watchdog
     */
    static void resetWatchdog();

    /**
     * whether or not we're shutting down now
     */
    static bool shuttingDown;

    /**
     * which thread called to exit
     */
    static pthread_t toldMainThread();

    /**
     * wait to be told
     */
    static void waitToBeTold();

    static void tellMainThread();
    
  private:
    /**
     * main thread for signalling
     */
    static pthread_t mainThread;
    static pthread_t waitFor;

    static void killme(int);
  };

}

#endif /* SIGNAL_H */
