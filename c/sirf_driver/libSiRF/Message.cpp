#include "Message.hpp"

extern "C" {
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
}

#define BUF_LEN 255

namespace SiRF {

  /**
   * handle the message, sending either to the given
   * ostream or the designated message handler
   */
  static void internalHandleMessage(std::ostream &out, 
				    Message *m,
				    const char *prefix,
				    const char *msg,
				    va_list arg) {
    static char buffer[BUF_LEN+1];

    strncpy(buffer, prefix, BUF_LEN);
    vsnprintf(&buffer[strlen(prefix)], 
	      BUF_LEN - strlen(prefix), 
	      msg, arg);
    buffer[BUF_LEN] = '\0';

    if (m->handler == NULL) {
      out << buffer << std::endl;
    } else {
      m->handler->handleMessage(buffer);
    }
  }

  /**
   * info type
   */
  void Message::info(const char *msg, ...) {
    va_list arg;
    va_start(arg, msg);
    internalHandleMessage(std::cout, getSelf(), "", msg, arg);
    va_end(arg);
  }

  /**
   * warning
   */
  void Message::warn(const char *msg, ...) {
    va_list arg;
    va_start(arg, msg);
    internalHandleMessage(std::cout, getSelf(), "WARNING: ", msg, arg);
    va_end(arg);
  }

  /**
   * critical
   */
  void Message::critical(const char *msg, ...) {
    if (getSelf()->handler != NULL) {
      getSelf()->handler->shutdownDisplay();
      getSelf()->handler = NULL;
    }
    va_list arg;
    va_start(arg, msg);
    internalHandleMessage(std::cerr, getSelf(), "ERROR: ", msg, arg);
    va_end(arg);
  }

  /**
   * setup a handler
   */
  void Message::setupHandler(MessageHandler *mh) {
    Message *m = getSelf();

    m->handler = mh;
  }

  /**
   * construct myself
   */
  Message::Message() {
    handler = NULL;
  }

  /**
   * get self
   */
  Message *Message::getSelf() {
    if (self == NULL) {
      self = new Message();
    }
    return self;
  }

  /**
   * the self pointer
   */
  Message *Message::self = NULL;
      
}
 
