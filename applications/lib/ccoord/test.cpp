#include "OSRef.h"
#include "LatLng.h"
#include <iostream>
using namespace std;

int main()
{
	OSRef ref (489600, 128500);
	LatLng latLng = ref.toLatLng();
	cout << "Ref as six figure string: " << ref.toSixFigureString() << endl;
	cout << latLng.getLat() << " " << latLng.getLng() << endl;
	latLng.toWGS84();
	cout << latLng.getLat() << " " << latLng.getLng() << endl;
	latLng.toOSGB36();
	cout << latLng.getLat() << " " << latLng.getLng() << endl;
	OSRef ref2 = latLng.toOSRef();
	cout << ref2.getEasting() << " " << ref2.getNorthing() << endl;

	return 0;
}
