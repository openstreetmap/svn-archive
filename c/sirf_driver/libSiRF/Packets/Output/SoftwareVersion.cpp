#include <SoftwareVersion.hpp>

namespace SiRF {

 void SoftwareVersion::input(OutputStream &in) {
  for (int i = 0; i < 20; i++) {
   in >> m_Message[i];
  }
 }

 void SoftwareVersion::output(std::ostream &out) const {
  out << "Message:  software version message in ASCII " << std::endl;
  /*
  for (int i = 0; i < 20; i++) {
   out << i << ":\t" << m_Message[i] << std::endl;
  }
  */
  out << "Version \"" << m_Message << "\"" << std::endl;
 }

  const unsigned char SoftwareVersion::type;

}
