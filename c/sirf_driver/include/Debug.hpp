#ifndef DEBUG_H
#define DEBUG_H

namespace SiRF {

  /**
   * class to handle debugging flags
   */
  class Debug {
  public:
    /**
     * find out
     * what the variable is
     */
    inline static bool isDebug() {
      return isSet;
    }

    /**
     * set the debug flag
     */
    inline void set(bool b) {
      isSet = b;
    }

  private:
    /**
     * variable declaration
     */
    static bool isSet;
  };

}

#endif /* DEBUG_H */
