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

  };

}

#endif /* SIGNAL_H */
