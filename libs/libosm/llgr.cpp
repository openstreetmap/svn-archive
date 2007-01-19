#include "llgr.h"
#include <cmath>

namespace OSM
{

/* @func GPS_Math_UKOSMap_To_WGS84_M ***********************************
**
** Transform UK Ordnance survey map position to WGS84 lat/lon
** Uses Molodensky transformation
**
** @param [r] map  [char *] map two letter code
** @param [r] mE   [double] map easting (metres)
** @param [r] mN   [double] map northing (metres)
** @param [w] lat  [double *] WGS84 latitude (deg)
** @param [w] lon  [double *] WGS84 longitude (deg)
**
** @return [int] success
************************************************************************/
EarthPoint gr_to_wgs84_ll(EarthPoint& gr)
{
	EarthPoint ll(0,0);

    double ht;
   
    EarthPoint ll2 = gr_to_ll(gr);
    GPS_Math_Known_Datum_To_WGS84_M(ll2.y,ll2.x,0,&(ll.y),&(ll.x),&ht);

    return ll;
}

/* @func GPS_Math_WGS84_To_UKOSMap_M ***********************************
**
** Convert WGS84 lat/lon to Ordnance survey map code and easting and
** northing. Uses Molodensky
**
** @param [r] lat  [double] WGS84 latitude (deg)
** @param [r] lon  [double] WGS84 longitude (deg)
** @param [w] mE   [double *] map easting (metres)
** @param [w] mN   [double *] map northing (metres)
** @param [w] map  [char *] map two letter code
**
** @return [int] success
************************************************************************/
EarthPoint wgs84_ll_to_gr(EarthPoint& ll)
{
	EarthPoint gr(0,0);

	EarthPoint ll2(0,0);
    double aht;


    GPS_Math_WGS84_To_Known_Datum_M(ll.y,ll.x,30,&(ll2.y),&(ll2.x),&aht);
	gr = ll_to_gr(ll2);
    return gr;
}

/* @func GPS_Math_Known_Datum_To_WGS84_M **********************************
**
** Transform datum to WGS84 using Molodensky
**
** @param [r] Sphi [double] source latitude (deg)
** @param [r] Slam [double] source longitude (deg)
** @param [r] SH   [double] source height  (metres)
** @param [w] Dphi [double *] dest latitude (deg)
** @param [w] Dlam [double *] dest longitude (deg)
** @param [w] DH   [double *] dest height  (metres)
** @param [r] n    [int] datum number from GPS_Datum structure
**
** @return [void]
************************************************************************/
void GPS_Math_Known_Datum_To_WGS84_M(double Sphi, double Slam, double SH,
				     double *Dphi, double *Dlam, double *DH)
{
    double Sa;
    double Sif;
    double Da;
    double Dif;
    double x;
    double y;
    double z;
    int    idx;
    
    Da  = (double) 6378137.0;
    Dif = (double) 298.257223563;
    
    Sa   = 6378206.400; 
    Sif  = 294.9786982; 
    x    = -8; 
    y    = 160; 
    z    = 176; 

    GPS_Math_Molodensky(Sphi,Slam,SH,Sa,Sif,Dphi,Dlam,DH,Da,Dif,x,y,z);

    return;
}



/* @func GPS_Math_WGS84_To_Known_Datum_M ********************************
**
** Transform WGS84 to other datum using Molodensky
**
** @param [r] Sphi [double] source latitude (deg)
** @param [r] Slam [double] source longitude (deg)
** @param [r] SH   [double] source height  (metres)
** @param [w] Dphi [double *] dest latitude (deg)
** @param [w] Dlam [double *] dest longitude (deg)
** @param [w] DH   [double *] dest height  (metres)
** @param [r] n    [int] datum number from GPS_Datum structure
**
** @return [void]
************************************************************************/
void GPS_Math_WGS84_To_Known_Datum_M(double Sphi, double Slam, double SH,
				     double *Dphi, double *Dlam, double *DH)
{
    double Sa;
    double Sif;
    double Da;
    double Dif;
    double x;
    double y;
    double z;
    int    idx;
    
    Sa  = (double) 6378137.0;
    Sif = (double) 298.257223563;
    
    Da   = 6377563.396; 
    Dif  = 299.3249646; 
    x    = -375;
    y    = 111;
    z    = -431;

    GPS_Math_Molodensky(Sphi,Slam,SH,Sa,Sif,Dphi,Dlam,DH,Da,Dif,x,y,z);

    return;
}

/* @func GPS_Math_Molodensky *******************************************
**
** Transform one datum to another
**
** @param [r] Sphi [double] source latitude (deg)
** @param [r] Slam [double] source longitude (deg)
** @param [r] SH   [double] source height  (metres)
** @param [r] Sa   [double] source semi-major axis (metres)
** @param [r] Sif  [double] source inverse flattening
** @param [w] Dphi [double *] dest latitude (deg)
** @param [w] Dlam [double *] dest longitude (deg)
** @param [w] DH   [double *] dest height  (metres)
** @param [r] Da   [double]   dest semi-major axis (metres)
** @param [r] Dif  [double]   dest inverse flattening
** @param [r] dx  [double]   dx
** @param [r] dy  [double]   dy
** @param [r] dz  [double]   dz
**
** @return [void]
************************************************************************/
void GPS_Math_Molodensky(double Sphi, double Slam, double SH, double Sa,
			 double Sif, double *Dphi, double *Dlam,
			 double *DH, double Da, double Dif, double dx,
			 double dy, double dz)
{
    double Sf;
    double Df;
    double esq;
    double bda;
    double da;
    double df;
    double N;
    double M;
    double tmp;
    double tmp2;
    double dphi;
    double dlambda;
    double dheight;
    double phis;
    double phic;
    double lams;
    double lamc;
    
    Sf = (double)1.0 / Sif;
    Df = (double)1.0 / Dif;
    
    esq = (double)2.0*Sf - pow(Sf,(double)2.0);
    bda = (double)1.0 - Sf;
    Sphi = (M_PI/180.0)*(Sphi);
    Slam = (M_PI/180.0)*(Slam); 
    
    da = Da - Sa;
    df = Df - Sf;

    phis = sin(Sphi);
    phic = cos(Sphi);
    lams = sin(Slam);
    lamc = cos(Slam);
    
    N = Sa /  sqrt((double)1.0 - esq*pow(phis,(double)2.0));
    
    tmp = ((double)1.0-esq) /pow(((double)1.0-esq*pow(phis,(double)2.0)),1.5);
    M   = Sa * tmp;

    tmp  = df * ((M/bda)+N*bda) * phis * phic;
    tmp2 = da * N * esq * phis * phic / Sa;
    tmp2 += ((-dx*phis*lamc-dy*phis*lams) + dz*phic);
    dphi = (tmp2 + tmp) / (M + SH);
    
    dlambda = (-dx*lams+dy*lamc) / ((N+SH)*phic);

    dheight = dx*phic*lamc + dy*phic*lams + dz*phis - da*(Sa/N) +
	df*bda*N*phis*phis;
    
    *Dphi = Sphi + dphi;
    *Dlam = Slam + dlambda;
    *DH   = SH   + dheight;
    
    *Dphi = (*Dphi) * (180.0/M_PI);
    *Dlam = (*Dlam) * (180.0/M_PI);

    return;
}

// ll_to_gr()
// Slightly modified version of:
/* @func modGPS_Math_Airy1830LatLonToNGEN **************************************
**
** Convert Airy 1830 datum latitude and longitude to UK Ordnance Survey
** National Grid Eastings and Northings
**
** @param [r] phi [double] WGS84 latitude     (deg)
** @param [r] lambda [double] WGS84 longitude (deg)
** @param [w] E [double *] NG easting (metres)
** @param [w] N [double *] NG northing (metres)
**
** @return [void]
*/

// Modifications:
// - Returns a Location structure.
// - Parameters renamed from "phi" and "lambda" to "lat" and "lng".

EarthPoint ll_to_gr ( const EarthPoint& ll )
{
	return ll_to_gr(ll.y,ll.x);
}

EarthPoint ll_to_gr ( double lat, double lon ) 
{

    double N0      = -100000;
    double E0      =  400000;
    double F0      = 0.9996012717;
    double phi0    = 49.;
    double lambda0 = -2.;
    double a       = 6377563.396;
    double b       = 6356256.910;
	double e, n;

    modGPS_Math_LatLon_To_EN(&e,&n,lat,lon,N0,E0,phi0,lambda0,F0,
							a,b);

    return EarthPoint(e,n);
}

/* @func  modGPS_Math_LatLon_To_EN **********************************
**
** Convert latitude and longitude to eastings and northings
** Standard Gauss-Kruger Transverse Mercator
**
** @param [w] E [double *] easting (metres)
** @param [w] N [double *] northing (metres)
** @param [r] phi [double] latitude (deg)
** @param [r] lambda [double] longitude (deg)
** @param [r] N0 [double] true northing origin (metres)
** @param [r] E0 [double] true easting  origin (metres)
** @param [r] phi0 [double] true latitude origin (deg)
** @param [r] lambda0 [double] true longitude origin (deg)
** @param [r] F0 [double] scale factor on central meridian
** @param [r] a [double] semi-major axis (metres)
** @param [r] b [double] semi-minor axis (metres)
**
** @return [void]
************************************************************************/
void modGPS_Math_LatLon_To_EN(double *E, double *N, double phi,
			   double lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b)
{
    double esq;
    double n;
    double etasq;
    double nu;
    double rho;
    double M;
    double I;
    double II;
    double III;
    double IIIA;
    double IV;
    double V;
    double VI;
    
    double tmp;
    double tmp2;
    double fdf;
    double fde;
   
	// Replaced these deg/rad conversions (originally done with a function)
	// with simple multiplication
    phi0    *= (M_PI/180.0); 
    lambda0 *= (M_PI/180.0); 
    phi     *= (M_PI/180.0); 
    lambda  *= (M_PI/180.0); 
    
    esq = ((a*a)-(b*b)) / (a*a);
    n   = (a-b) / (a+b);
    
    tmp  = (double)1.0 - (esq * sin(phi) * sin(phi));
    nu   = a * F0 * pow(tmp,(double)-0.5);
    rho  = a * F0 * ((double)1.0 - esq) * pow(tmp,(double)-1.5);
    etasq = (nu / rho) - (double)1.0;

    fdf   = (double)5.0 / (double)4.0;
    tmp   = (double)1.0 + n + (fdf * n * n) + (fdf * n * n * n);
    tmp  *= (phi - phi0);
    tmp2  = (double)3.0*n + (double)3.0*n*n + ((double)21./(double)8.)*n*n*n;
    tmp2 *= (sin(phi-phi0) * cos(phi+phi0));
    tmp  -= tmp2;

    fde   = ((double)15.0 / (double)8.0);
    tmp2  = ((fde*n*n) + (fde*n*n*n)) * sin((double)2.0 * (phi-phi0));
    tmp2 *= cos((double)2.0 * (phi+phi0));
    tmp  += tmp2;
    
    tmp2  = ((double)35.0/(double)24.0) * n * n * n;
    tmp2 *= sin((double)3.0 * (phi-phi0));
    tmp2 *= cos((double)3.0 * (phi+phi0));
    tmp  -= tmp2;

    M     = b * F0 * tmp;
    I     = M + N0;
    II    = (nu / (double)2.0) * sin(phi) * cos(phi);
    III   = (nu / (double)24.0) * sin(phi) * cos(phi) * cos(phi) * cos(phi);
    III  *= ((double)5.0 - (tan(phi) * tan(phi)) + ((double)9.0 * etasq));
    IIIA  = (nu / (double)720.0) * sin(phi) * pow(cos(phi),(double)5.0);
    IIIA *= ((double)61.0 - ((double)58.0*tan(phi)*tan(phi)) +
	     pow(tan(phi),(double)4.0));
    IV    = nu * cos(phi);

    tmp   = pow(cos(phi),(double)3.0);
    tmp  *= ((nu/rho) - tan(phi) * tan(phi));
    V     = (nu/(double)6.0) * tmp;

    tmp   = (double)5.0 - ((double)18.0 * tan(phi) * tan(phi));
    tmp  += tan(phi)*tan(phi)*tan(phi)*tan(phi) + ((double)14.0 * etasq);
    tmp  -= ((double)58.0 * tan(phi) * tan(phi) * etasq);
    tmp2  = cos(phi)*cos(phi)*cos(phi)*cos(phi)*cos(phi) * tmp;
    VI    = (nu / (double)120.0) * tmp2;
    
    *N = I + II*(lambda-lambda0)*(lambda-lambda0) +
	     III*pow((lambda-lambda0),(double)4.0) +
	     IIIA*pow((lambda-lambda0),(double)6.0);

    *E = E0 + IV*(lambda-lambda0) + V*pow((lambda-lambda0),(double)3.0) +
	 VI * pow((lambda-lambda0),(double)5.0);

    return;
}

// gr_to_ll()
// Slightly modified version of:
/* @func modGPS_Math_NGENToAiry1830LatLon **************************************
**
** Convert  to UK Ordnance Survey National Grid Eastings and Northings to
** Airy 1830 datum latitude and longitude
**
** @param [r] E [double] NG easting (metres)
** @param [r] N [double] NG northing (metres)
** @param [w] phi [double *] Airy latitude     (deg)
** @param [w] lambda [double *] Airy longitude (deg)
**
** @return [void]
************************************************************************/

// Modifications:
// - Grid reference passed as a struct of type Location.
// - Lat/long parameters renamed from "phi" and "lambda" to "lat" and "lng".

EarthPoint gr_to_ll(const EarthPoint& gridref)
{
    double N0      = -100000;
    double E0      =  400000;
    double F0      = 0.9996012717;
    double phi0    = 49.;
    double lambda0 = -2.;
    double a       = 6377563.396;
    double b       = 6356256.910;

	EarthPoint retval(0,0);

    modGPS_Math_EN_To_LatLon(gridref.x,gridref.y,&retval.y,&retval.x,N0,E0,
					phi0,lambda0,F0, a,b);
    
    return retval;
}

/* @func  modGPS_Math_EN_To_LatLon **************************************
**
** Convert Eastings and Northings to latitude and longitude
**
** @param [w] E [double] NG easting (metres)
** @param [w] N [double] NG northing (metres)
** @param [r] phi [double *] Airy latitude     (deg)
** @param [r] lambda [double *] Airy longitude (deg)
** @param [r] N0 [double] true northing origin (metres)
** @param [r] E0 [double] true easting  origin (metres)
** @param [r] phi0 [double] true latitude origin (deg)
** @param [r] lambda0 [double] true longitude origin (deg)
** @param [r] F0 [double] scale factor on central meridian
** @param [r] a [double] semi-major axis (metres)
** @param [r] b [double] semi-minor axis (metres)
**
** @return [void]
************************************************************************/
void modGPS_Math_EN_To_LatLon(double E, double N, double *phi,
			   double *lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b)
{
    double esq;
    double n;
    double etasq;
    double nu;
    double rho;
    double M;
    double VII;
    double VIII;
    double IX;
    double X;
    double XI;
    double XII;
    double XIIA;
    double phix;
    double nphi=0.0;
    
    double tmp;
    double tmp2;
    double fdf;
    double fde;

	// Replaced these deg/rad conversions (originally done with a function)
	// with simple multiplication
    phi0    *= (M_PI/180.0); 
    lambda0 *= (M_PI/180.0); 

    n     = (a-b) / (a+b);
    fdf   = (double)5.0 / (double)4.0;
    fde   = ((double)15.0 / (double)8.0);

    esq = ((a*a)-(b*b)) / (a*a);


    phix = ((N-N0)/(a*F0)) + phi0;
    
    tmp  = (double)1.0 - (esq * sin(phix) * sin(phix));
    nu   = a * F0 * pow(tmp,(double)-0.5);
    rho  = a * F0 * ((double)1.0 - esq) * pow(tmp,(double)-1.5);
    etasq = (nu / rho) - (double)1.0;

    M = (double)-1e20;

    while(N-N0-M > (double)0.000001)
    {
	nphi = phix;
	
	tmp   = (double)1.0 + n + (fdf * n * n) + (fdf * n * n * n);
	tmp  *= (nphi - phi0);
	tmp2  = (double)3.0*n + (double)3.0*n*n +
	        ((double)21./(double)8.)*n*n*n;
	tmp2 *= (sin(nphi-phi0) * cos(nphi+phi0));
	tmp  -= tmp2;


	tmp2  = ((fde*n*n) + (fde*n*n*n)) * sin((double)2.0 * (nphi-phi0));
	tmp2 *= cos((double)2.0 * (nphi+phi0));
	tmp  += tmp2;
    
	tmp2  = ((double)35.0/(double)24.0) * n * n * n;
	tmp2 *= sin((double)3.0 * (nphi-phi0));
	tmp2 *= cos((double)3.0 * (nphi+phi0));
	tmp  -= tmp2;

	M     = b * F0 * tmp;

	if(N-N0-M > (double)0.000001)
	    phix = ((N-N0-M)/(a*F0)) + nphi;
    }
    

    VII  = tan(nphi) / ((double)2.0 * rho * nu);

    tmp  = (double)5.0 + (double)3.0 * tan(nphi) * tan(nphi) + etasq;
    tmp -= (double)9.0 * tan(nphi) * tan(nphi) * etasq;
    VIII = (tan(nphi)*tmp) / ((double)24.0 * rho * nu*nu*nu);

    tmp  = (double)61.0 + (double)90.0 * tan(nphi) * tan(nphi);
    tmp += (double)45.0 * pow(tan(nphi),(double)4.0);
    IX   = tan(nphi) / ((double)720.0 * rho * pow(nu,(double)5.0)) * tmp;

    X    = (double)1.0 / (cos(nphi) * nu);

    tmp  = (nu / rho) + (double)2.0 * tan(nphi) * tan(nphi);
    XI   = ((double)1.0 / (cos(nphi) * (double)6.0 * nu*nu*nu)) * tmp;

    tmp  = (double)5.0 + (double)28.0 * tan(nphi)*tan(nphi);
    tmp += (double)24.0 * pow(tan(nphi),(double)4.0);
    XII  = ((double)1.0 / ((double)120.0 * pow(nu,(double)5.0) * cos(nphi)))
	   * tmp;

    tmp  = (double)61.0 + (double)662.0 * tan(nphi) * tan(nphi);
    tmp += (double)1320.0 * pow(tan(nphi),(double)4.0);
    tmp += (double)720.0  * pow(tan(nphi),(double)6.0);
    XIIA = ((double)1.0 / (cos(nphi) * (double)5040.0 * pow(nu,(double)7.0)))
	   * tmp;

    *phi = nphi - VII*pow((E-E0),(double)2.0) + VIII*pow((E-E0),(double)4.0) -
	   IX*pow((E-E0),(double)6.0);
    
    *lambda = lambda0 + X*(E-E0) - XI*pow((E-E0),(double)3.0) +
	      XII*pow((E-E0),(double)5.0) - XIIA*pow((E-E0),(double)7.0);

	// Replaced these rad/deg conversions (originally done with a function)
	// with simple multiplication
    *phi   *= (180.0/M_PI); 
    *lambda *= (180.0/M_PI); 

    return;
}

}
