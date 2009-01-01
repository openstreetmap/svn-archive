--------------------------------------------------------------------------
 Jcoord
 readme.txt

 (c) 2006 Jonathan Stott

 Created on 11-Feb-2006

 1.0 - 11 Feb 2006
  - Initial version created from PHPcoord v2.2
--------------------------------------------------------------------------

Jcoord is a collection of Java classes that provide functions for handling
various co-ordinate systems and converting between them. Currently, OSGB
(Ordnance Survey of Great Britain) grid references, UTM (Universal Transverse
Mercator) references and latitude/longitude are supported. A function is
also provided to find the surface distance between two points of latitude
and longitude.

When using the OSGB conversions, the majority of applications use the
WGS84 datum rather than the OSGB36 datum. Conversions between the two
data were added in v1.1 - the conversions should be accurate to within
5m or so. If accuracy is not important (i.e. to within 200m or so),
then it isn't necessary to perform the conversions.

Examples of how to use the classes in Jcoord can be found in the
uk.me.jstott.jcoord.Test class

See http://www.jstott.me.uk/jcoord/ for latest releases and information.


DISCLAIMER

Accuracy of the co-ordinate conversions contained within the Jcoord
package is not guaranteed. Use of the conversions is entirely at your
own risk and I cannot be held responsible for any consequences of
errors created by the conversions. I do not recommend using the package
for mission-critical applications.


LICENSING

This software product is available under the GNU General Public License
(GPL). Terms of the GPL can be read at http://www.jstott.me.uk/gpl/.
Any commercial use requires the purchase of a license - contact me at
jcoord@jstott.me.uk for details.