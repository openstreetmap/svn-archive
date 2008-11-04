#include "llgr.h"
#include "ccoord/OSRef.h"
#include "ccoord/LatLng.h"

namespace OSM
{

EarthPoint wgs84_ll_to_gr(const EarthPoint& p)
{
	LatLng ll (p.y, p.x);
	ll.toOSGB36();
	OSRef gr = ll.toOSRef();
	return EarthPoint(gr.getEasting(), gr.getNorthing());
}

EarthPoint gr_to_wgs84_ll(const EarthPoint& p)
{
	OSRef gr (p.x, p.y);
	LatLng ll = gr.toLatLng();
	ll.toWGS84();
	return EarthPoint(ll.getLng(),ll.getLat());
}

}
