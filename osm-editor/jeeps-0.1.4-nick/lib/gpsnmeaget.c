/********************************************************************
** @source JEEPS NMEA read functions
**
** @author Copyright (C) 2000 Alan Bleasby
** @version 1.0 
** @modified June 29th 2000 Alan Bleasby. First version
** @@
** 
** This library is free software; you can redistribute it and/or
** modify it under the terms of the GNU Library General Public
** License as published by the Free Software Foundation; either
** version 2 of the License, or (at your option) any later version.
** 
** This library is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Library General Public License for more details.
** 
** You should have received a copy of the GNU Library General Public
** License along with this library; if not, write to the
** Free Software Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA  02111-1307, USA.
********************************************************************/
#include "gps.h"
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>


/* @func GPS_NMEA_Get_Apb *******************************************
**
** Return a filled Autopilot format B object
**
** @param [w] thys [GPS_PApb *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Apb(GPS_PApb *thys)
{
    if(!gps_nmea->apb->valid)
	return gpsFalse;
    (*thys)->blink   = gps_nmea->apb->blink;
    (*thys)->warn    = gps_nmea->apb->warn;
    (*thys)->edist   = gps_nmea->apb->edist;
    (*thys)->steer   = gps_nmea->apb->steer;
    (*thys)->unit    = gps_nmea->apb->unit;
    (*thys)->alarmc  = gps_nmea->apb->alarmc;
    (*thys)->alarmp  = gps_nmea->apb->alarmp;
    (*thys)->od      = gps_nmea->apb->od;
    strcpy((*thys)->wpt,gps_nmea->apb->wpt);
    (*thys)->pd      = gps_nmea->apb->pd;
    (*thys)->hdg     = gps_nmea->apb->hdg;
    (*thys)->valid   = gps_nmea->apb->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Bod *******************************************
**
** Return a filled Bearing origin to dest wpt object
**
** @param [w] thys [GPS_PBod *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Bod(GPS_PBod *thys)
{
    if(!gps_nmea->bod->valid)
	return gpsFalse;
    (*thys)->tru    = gps_nmea->bod->tru;
    (*thys)->mag     = gps_nmea->bod->mag;
    strcpy((*thys)->dest,gps_nmea->bod->dest);
    strcpy((*thys)->start,gps_nmea->bod->start);
    (*thys)->valid   = gps_nmea->bod->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Bwc *******************************************
**
** Return a filled Bearing distance to wpt object
**
** @param [w] thys [GPS_PBwc *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Bwc(GPS_PBwc *thys)
{
    if(!gps_nmea->bwc->valid)
	return gpsFalse;
    (*thys)->time    = gps_nmea->bwc->time;
    (*thys)->lat     = gps_nmea->bwc->lat;
    (*thys)->lon     = gps_nmea->bwc->lon;
    (*thys)->tru    = gps_nmea->bwc->tru;
    (*thys)->mag     = gps_nmea->bwc->mag;
    (*thys)->dist    = gps_nmea->bwc->dist;
    strcpy((*thys)->wpt,gps_nmea->bwc->wpt);
    (*thys)->valid   = gps_nmea->bwc->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Bwr *******************************************
**
** Return a filled Bearing distance to wpt (rhumb) object
**
** @param [w] thys [GPS_PBwr *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Bwr(GPS_PBwr *thys)
{
    if(!gps_nmea->bwr->valid)
	return gpsFalse;
    (*thys)->time    = gps_nmea->bwr->time;
    (*thys)->lat     = gps_nmea->bwr->lat;
    (*thys)->lon     = gps_nmea->bwr->lon;
    (*thys)->tru    = gps_nmea->bwr->tru;
    (*thys)->mag     = gps_nmea->bwr->mag;
    (*thys)->dist    = gps_nmea->bwr->dist;
    strcpy((*thys)->wpt,gps_nmea->bwr->wpt);
    (*thys)->valid   = gps_nmea->bwr->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Dbt *******************************************
**
** Return a filled depth below transducer object
**
** @param [w] thys [GPS_PDbt *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Dbt(GPS_PDbt *thys)
{
    if(!gps_nmea->dbt->valid)
	return gpsFalse;
    (*thys)->f     = gps_nmea->dbt->f;
    (*thys)->m     = gps_nmea->dbt->m;
    (*thys)->valid = gps_nmea->dbt->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Gga *******************************************
**
** Return a GPS fix data object
**
** @param [w] thys [GPS_PGga *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Gga(GPS_PGga *thys)
{
    if(!gps_nmea->gga->valid)
	return gpsFalse;
    (*thys)->time     = gps_nmea->gga->time;
    (*thys)->lat      = gps_nmea->gga->lat;
    (*thys)->lon      = gps_nmea->gga->lon;
    (*thys)->qual     = gps_nmea->gga->qual;
    (*thys)->nsat     = gps_nmea->gga->nsat;
    (*thys)->hdil     = gps_nmea->gga->hdil;
    (*thys)->alt      = gps_nmea->gga->alt;
    (*thys)->galt     = gps_nmea->gga->galt;
    (*thys)->last     = gps_nmea->gga->last;
    (*thys)->dgpsid   = gps_nmea->gga->dgpsid;
    (*thys)->valid    = gps_nmea->gga->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Gll *******************************************
**
** Return a lat/lon object
**
** @param [w] thys [GPS_PGll *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Gll(GPS_PGll *thys)
{
    if(!gps_nmea->gll->valid)
	return gpsFalse;
    (*thys)->time     = gps_nmea->gll->time;
    (*thys)->lat      = gps_nmea->gll->lat;
    (*thys)->lon      = gps_nmea->gll->lon;
    (*thys)->dv       = gps_nmea->gll->dv;
    (*thys)->valid    = gps_nmea->gll->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Gsa *******************************************
**
** Return a DOP and active satellite object
**
** @param [w] thys [GPS_PGsa *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Gsa(GPS_PGsa *thys)
{
    int32 i;
    
    if(!gps_nmea->gsa->valid)
	return gpsFalse;
    (*thys)->type     = gps_nmea->gsa->type;
    (*thys)->fix      = gps_nmea->gsa->fix;
    (*thys)->nsat     = gps_nmea->gsa->nsat;
    for(i=0;i<(*thys)->nsat;++i)
	(*thys)->prn[i] = gps_nmea->gsa->prn[i];
    (*thys)->pdop     = gps_nmea->gsa->pdop;
    (*thys)->hdop     = gps_nmea->gsa->hdop;
    (*thys)->vdop     = gps_nmea->gsa->vdop;    
    (*thys)->valid    = gps_nmea->gsa->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Gsv *******************************************
**
** Return a satellites in view object
**
** @param [w] thys [GPS_PGsv *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Gsv(GPS_PGsv *thys)
{
    int32 i;
    int32 v;

    if(!gps_nmea->gsv->valid)
	return gpsFalse;
    v = (*thys)->inview = gps_nmea->gsv->inview;

    for(i=0;i<v;++i)
    {
	(*thys)->prn[i]       = gps_nmea->gsv->prn[i];
	(*thys)->elevation[i] = gps_nmea->gsv->elevation[i];
	(*thys)->azimuth[i]   = gps_nmea->gsv->azimuth[i];
	(*thys)->strength[i]  = gps_nmea->gsv->strength[i];
    }
    (*thys)->valid    = gps_nmea->gsv->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Hdm *******************************************
**
** Return a magnetic heading object
**
** @param [w] thys [GPS_PHdm *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Hdm(GPS_PHdm *thys)
{

    if(!gps_nmea->hdm->valid)
	return gpsFalse;
    (*thys)->hdg   = gps_nmea->hdm->hdg;
    (*thys)->valid = gps_nmea->hdm->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Hsc *******************************************
**
** Return a heading to steer object
**
** @param [w] thys [GPS_PHsc *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Hsc(GPS_PHsc *thys)
{

    if(!gps_nmea->hsc->valid)
	return gpsFalse;
    (*thys)->tru  = gps_nmea->hsc->tru;
    (*thys)->mag   = gps_nmea->hsc->mag;
    (*thys)->valid = gps_nmea->hsc->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Mtw *******************************************
**
** Return a water temperature object
**
** @param [w] thys [GPS_PMtw *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Mtw(GPS_PMtw *thys)
{

    if(!gps_nmea->mtw->valid)
	return gpsFalse;
    (*thys)->T     = gps_nmea->mtw->T;
    (*thys)->valid = gps_nmea->mtw->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_R00 *******************************************
**
** Return a list of waypoints in active route object
**
** @param [w] thys [GPS_PR00 *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_R00(GPS_PR00 *thys)
{

    if(!gps_nmea->r00->valid)
	return gpsFalse;
    strcpy((*thys)->wpts,gps_nmea->r00->wpts);
    (*thys)->valid = gps_nmea->r00->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Rmb *******************************************
**
** Return a recommended minimum navigation object
**
** @param [w] thys [GPS_PRmb *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Rmb(GPS_PRmb *thys)
{

    if(!gps_nmea->rmb->valid)
	return gpsFalse;
    (*thys)->warn     = gps_nmea->rmb->warn;
    (*thys)->cross    = gps_nmea->rmb->cross;
    (*thys)->correct  = gps_nmea->rmb->correct;
    strcpy((*thys)->owpt,gps_nmea->rmb->owpt);
    strcpy((*thys)->dwpt,gps_nmea->rmb->dwpt);
    (*thys)->lat      = gps_nmea->rmb->lat;
    (*thys)->lon      = gps_nmea->rmb->lon;
    (*thys)->range    = gps_nmea->rmb->range;
    (*thys)->tru     = gps_nmea->rmb->tru;
    (*thys)->velocity = gps_nmea->rmb->velocity;
    (*thys)->alarm    = gps_nmea->rmb->alarm;
    (*thys)->valid    = gps_nmea->rmb->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Rmc *******************************************
**
** Return a recommended minimum specific GPS/transit object
**
** @param [w] thys [GPS_PRmc *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Rmc(GPS_PRmc *thys)
{

    if(!gps_nmea->rmc->valid)
	return gpsFalse;
    (*thys)->time     = gps_nmea->rmc->time;
    (*thys)->warn     = gps_nmea->rmc->warn;
    strcpy((*thys)->date,gps_nmea->rmc->date);
    (*thys)->lat      = gps_nmea->rmc->lat;
    (*thys)->lon      = gps_nmea->rmc->lon;
    (*thys)->speed    = gps_nmea->rmc->speed;
    (*thys)->cmg      = gps_nmea->rmc->cmg;
    (*thys)->magvar   = gps_nmea->rmc->magvar;
    (*thys)->valid    = gps_nmea->rmc->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Rte *******************************************
**
** Return a waypoints in active route object
**
** @param [w] thys [GPS_PRte *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Rte(GPS_PRte *thys)
{

    if(!gps_nmea->rte->valid)
	return gpsFalse;
    (*thys)->type      = gps_nmea->rte->type;
    (*thys)->rte       = gps_nmea->rte->rte;
    if((*thys)->wpts)
	free((void *)(*thys)->wpts);
    if(!((*thys)->wpts = malloc(strlen(gps_nmea->rte->wpts)+1)))
	return MEMORY_ERROR;
    strcpy((*thys)->wpts,gps_nmea->rte->wpts);
    (*thys)->valid     = gps_nmea->rte->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Vhw *******************************************
**
** Return a water speed and heading object
**
** @param [w] thys [GPS_PVhw *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Vhw(GPS_PVhw *thys)
{

    if(!gps_nmea->vhw->valid)
	return gpsFalse;

    (*thys)->tru      = gps_nmea->vhw->tru;
    (*thys)->mag       = gps_nmea->vhw->mag;
    (*thys)->wspeed    = gps_nmea->vhw->wspeed;
    (*thys)->speed     = gps_nmea->vhw->speed;
    (*thys)->valid     = gps_nmea->vhw->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Vwr *******************************************
**
** Return a relative wind speed and direction object
**
** @param [w] thys [GPS_PVwr *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Vwr(GPS_PVwr *thys)
{

    if(!gps_nmea->vwr->valid)
	return gpsFalse;

    (*thys)->wind      = gps_nmea->vwr->wind;
    (*thys)->wdir      = gps_nmea->vwr->wdir;
    (*thys)->knots     = gps_nmea->vwr->knots;
    (*thys)->ms        = gps_nmea->vwr->ms;
    (*thys)->khr       = gps_nmea->vwr->khr;
    (*thys)->valid     = gps_nmea->vwr->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Vtg *******************************************
**
** Return a track made good and ground speed object
**
** @param [w] thys [GPS_PVtg *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Vtg(GPS_PVtg *thys)
{

    if(!gps_nmea->vtg->valid)
	return gpsFalse;

    (*thys)->tru      = gps_nmea->vtg->tru;
    (*thys)->mag       = gps_nmea->vtg->mag;
    (*thys)->knots     = gps_nmea->vtg->knots;
    (*thys)->khr       = gps_nmea->vtg->khr;
    (*thys)->valid     = gps_nmea->vtg->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Wpl *******************************************
**
** Return a waypoint location object
**
** @param [w] thys [GPS_PWpl *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Wpl(GPS_PWpl *thys)
{

    if(!gps_nmea->wpl->valid)
	return gpsFalse;

    (*thys)->lat       = gps_nmea->wpl->lat;
    (*thys)->lon       = gps_nmea->wpl->lon;
    strcpy((*thys)->wpt,gps_nmea->wpl->wpt);
    (*thys)->valid     = gps_nmea->wpl->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Xte *******************************************
**
** Return a measured cross track error object
**
** @param [w] thys [GPS_PXte *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Xte(GPS_PXte *thys)
{

    if(!gps_nmea->xte->valid)
	return gpsFalse;

    (*thys)->warn      = gps_nmea->xte->warn;
    (*thys)->dist      = gps_nmea->xte->dist;
    (*thys)->cycle     = gps_nmea->xte->cycle;
    (*thys)->steer     = gps_nmea->xte->steer;
    (*thys)->unit      = gps_nmea->xte->unit;
    (*thys)->valid     = gps_nmea->xte->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Xtr *******************************************
**
** Return a dead reckoning cross track error object
**
** @param [w] thys [GPS_PXtr *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Xtr(GPS_PXtr *thys)
{

    if(!gps_nmea->xtr->valid)
	return gpsFalse;

    (*thys)->dist      = gps_nmea->xtr->dist;
    (*thys)->steer     = gps_nmea->xtr->steer;
    (*thys)->unit      = gps_nmea->xtr->unit;
    (*thys)->valid     = gps_nmea->xtr->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Rme *******************************************
**
** Return a Garmin position error object
**
** @param [w] thys [GPS_PRme *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Rme(GPS_PRme *thys)
{

    if(!gps_nmea->rme->valid)
	return gpsFalse;

    (*thys)->hpe      = gps_nmea->rme->hpe;
    (*thys)->vpe      = gps_nmea->rme->vpe;
    (*thys)->spe      = gps_nmea->rme->spe;
    (*thys)->valid    = gps_nmea->rme->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Rmz *******************************************
**
** Return a Garmin altitude object
**
** @param [w] thys [GPS_PRmz *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Rmz(GPS_PRmz *thys)
{

    if(!gps_nmea->rmz->valid)
	return gpsFalse;

    (*thys)->height = gps_nmea->rmz->height;
    (*thys)->dim    = gps_nmea->rmz->dim;
    (*thys)->valid  = gps_nmea->rmz->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Rmm *******************************************
**
** Return a Garmin datum object
**
** @param [w] thys [GPS_PRmm *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Rmm(GPS_PRmm *thys)
{

    if(!gps_nmea->rmm->valid)
	return gpsFalse;

    strcpy((*thys)->datum,gps_nmea->rmm->datum);
    (*thys)->valid  = gps_nmea->rmm->valid;

    return gpsTrue;
}




/* @func GPS_NMEA_Get_Lib *******************************************
**
** Return a Garmin link object
**
** @param [w] thys [GPS_PLib *] object
**
** @return [int32] false if data invalid
************************************************************************/

int32 GPS_NMEA_Get_Lib(GPS_PLib *thys)
{

    if(!gps_nmea->lib->valid)
	return gpsFalse;

    (*thys)->freq  = gps_nmea->lib->freq;
    (*thys)->baud  = gps_nmea->lib->baud;
    (*thys)->rqst  = gps_nmea->lib->rqst;
    (*thys)->valid = gps_nmea->lib->valid;

    return gpsTrue;
}

