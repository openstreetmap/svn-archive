#include "VisibleList.hpp"

namespace SiRF {

  void VisibleList::input(OutputStream &in) {
    uint8 num_sats;
    VisibleSatInfo info;

    in >> num_sats;
    m_SatInfo.reserve(num_sats);

    for (int i = 0; i < num_sats; i++) {
      in >> info.SVID >> info.Azimuth >> info.Elevation;
      m_SatInfo.push_back(info);
    }
  }

  void VisibleList::output(std::ostream &out) const {
    out << "Visible Satellite List, " << m_SatInfo.size() << " entries" << std::endl;
    for (std::vector<VisibleSatInfo>::const_iterator itr = m_SatInfo.begin();
	 itr != m_SatInfo.end(); ++itr) {
      out << (int)itr->SVID << "\t" << (int)itr->Azimuth 
	  << "\t" << (int)itr->Elevation << std::endl;
    }
  }

  const uint8 VisibleList::type;

}
