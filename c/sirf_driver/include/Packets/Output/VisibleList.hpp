#ifndef VISIBLE_LIST_H
#define VISIBLE_LIST_H

#include <Types.hpp>
#include <OutputPacket.hpp>
#include <OutputStream.hpp>
#include <vector>

namespace SiRF {
  
  struct VisibleSatInfo {
    uint8 SVID;
    uint16 Azimuth, Elevation;
  };

  class VisibleList : public OutputPacket {
    
  public:
    
    static const unsigned char type = 0x0d;
    
    unsigned char getType() { return 0x0d; }
    
    bool isInput() { return false; }
    
    void input(OutputStream &in);
    void output(std::ostream &out) const;
    
    inline uint8 getNumVisible() {
      return (uint8)m_SatInfo.size();
    }
    inline VisibleSatInfo getSatInfo(int i) {
      return m_SatInfo[i];
    }
    
  private:
    
    /* list of visible satellites */
    std::vector<VisibleSatInfo> m_SatInfo;
    
  };
  
}

#endif /* VISIBLE_LIST_H */

