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
     *
     */
    inline static bool isSingleThreaded() {
      return isSingle;
    }

    /**
     * set the debug flag
     */
    inline static void setDebug(bool b) {
      isSet = b;
    }

    inline static void setSingleThreaded(bool b) {
      isSingle = b;
    }

  private:
    /**
     * variable declaration
     */
    static bool isSet;
    static bool isSingle;
  };

}

#endif /* DEBUG_H */
