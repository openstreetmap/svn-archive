#ifndef SOFTWARE_VERSION_H
#define SOFTWARE_VERSION_H

#include <Types.hpp>
#include <OutputPacket.hpp>
#include <OutputStream.hpp>

namespace SiRF {

 class SoftwareVersion : public OutputPacket {

 public:

  static const unsigned char type = 0x06;

  unsigned char getType() { return 0x06; }

  bool isInput() { return false; }

  void input(OutputStream &in);
  void output(std::ostream &out) const;

  inline int8 getMessage(int i) const { return m_Message[i]; }
  inline const int8 *getMessageArray() const {
   return m_Message;
  }

 private:

  /*  software version message in ASCII  */
  int8 m_Message[20];

 };

}
#endif /* SOFTWARE_VERSION_H */
