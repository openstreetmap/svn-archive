# needed by the Apache2 build system

MOD_MAPNIK_WMS = mod_mapnik_wms

mod_mapnik_wms.la: ${MOD_MAPNIK_WMS:=.slo}
	$(SH_LINK) -rpath $(libexecdir) -module -avoid-version ${MOD_MAPNIK_WMS:=.lo}

DISTCLEAN_TARGETS = modules.mk

shared =  mod_mapnik_wms.la

