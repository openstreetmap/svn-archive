# Makefile.am for scripts/perl_lib

gpsdrivedir = $(datadir)/gpsdrive

Geodir   = $(PERL_PACKAGE_DIR)/Geo
Geo_DATA =  Geo/*.pm

Geo_Gpsdrivedir   = $(PERL_PACKAGE_DIR)/Geo/Gpsdrive
Geo_Gpsdrive_DATA =  Geo/Gpsdrive/*.pm

Geo_OSMdir   = $(PERL_PACKAGE_DIR)/Geo/OSM
Geo_OSM_DATA =  Geo/OSM/*.pm

Geo_Filterdir   = $(PERL_PACKAGE_DIR)/Geo/Filter
Geo_Filter_DATA =  Geo/Filter/*.pm

Utilsdir   = $(PERL_PACKAGE_DIR)/Utils
Utils_DATA =  Utils/*.pm

Utils_LWPdir   = $(PERL_PACKAGE_DIR)/Utils/LWP
Utils_LWP_DATA =  Utils/LWP/*.pm


EXTRA_DIST = \
	$(Geo_DATA)\	$(Geo_Gpsdrive_DATA) $(Geo_OSM_DATA)\
	$(Geo_Filter_DATA)  $(Utils_DATA) \
	$(Utils_LWP_DATA)
