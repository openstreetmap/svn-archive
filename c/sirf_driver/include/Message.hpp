#ifndef MESSAGE_H
#define MESSAGE_H

#include <iostream>

namespace SiRF {

  /**
   * interface for classes interested in having output messages
   * routed to them
   */
  class MessageHandler {
  public:
    /**
     * handleMessage
     */
    virtual void handleMessage(const char *) = 0;
  };

  /**
   * singleton class to handle messages
   */
  class Message {
  public:
    /**
     * info type
     */
    static void info(const char *, ...);

    /**
     * warning
     */
    static void warn(const char *, ...);

    /**
     * critical
     */
    static void critical(const char *, ...);

    /**
     * setup a handler
     */
    static void setupHandler(MessageHandler *mh);

    /**
     * message handler behind it all
     * NOTE: this really should be private, but its nastily
     * here because to make it private would require all
     * files to include a set of even nastier C headers...
     */
    MessageHandler *handler;

  private:
    /**
     * construct myself
     */
    Message();

    /**
     * get self
     */
    static Message *getSelf();

    /**
     * ourself
     */
    static Message *self;

  };

}

#endif /* MESSAGE_H */
