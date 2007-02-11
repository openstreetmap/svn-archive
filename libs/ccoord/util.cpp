#include <cmath>

// Based on Jonathan Stott's JCoord.
// Licenced under the GNU GPL.

double toDegrees(double rad)
{
	return rad*(180/M_PI);
}

double toRadians(double deg)
{
	return deg*(M_PI/180);
}

double sinSquared(double a)
{
	return sin(a)*sin(a);
}

double tanSquared(double a)
{
	return tan(a)*tan(a);
}

double sec(double a)
{
	return 1/cos(a);
}
