#include <exception>
#include <string>

namespace SiRF {

  class FatalException : public std::exception {
  public:
    FatalException(const std::string &m) {
      message = m;
    }
    ~FatalException() throw() {
    }
    const char *what() const throw() {
      return message.c_str();
    }
  private:
    std::string message;
  };

  class MarkerNotFoundException : public std::exception {
  public:
    MarkerNotFoundException(uint32 a, uint32 b) {
      snprintf(message, 60, "Marker not found. %04x should be %04x.", a, b);
    }
    ~MarkerNotFoundException() throw() {}
    const char *what() const throw() {
      return message;
    }
  private:
    char message[60];
  };

  class InvalidParameterException : public std::exception {
  public:
    InvalidParameterException(uint32 a, const char *why) {
      snprintf(message, 60, "Invalid parameter. %x %s.", a, why);
    }
    ~InvalidParameterException() throw() {}
    const char *what() const throw() {
      return message;
    }
  private:
    char message[60];
  };

  class LengthException : public std::exception {
  public:
    LengthException(uint16 a, uint16 b) {
      snprintf(message, 60, "Bad length: %d, originally %d.", a, b);
    }
    ~LengthException() throw() {}
    const char *what() const throw() {
      return message;
    }
  private:
    char message[60];
  };

  class ChecksumException : public std::exception {
  public:
    ChecksumException() {}
    ~ChecksumException() throw() {}
    const char *what() const throw() {
      return "Bad packet checksum.";
    }
  };

  class UnknownPacketTypeException : public std::exception {
  public:
    UnknownPacketTypeException(uint8 a) {
      snprintf(message, 60, "Unknown packet type: %02x.", a);
    }
    ~UnknownPacketTypeException() throw() {}
    const char *what() const throw() {
      return message;
    }
  private:
    char message[60];
  };

}
