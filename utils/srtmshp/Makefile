CXXFLAGS = -I/home/nick/shapelib/include  -I/home/nick/src/libs/libosm
LDFLAGS = -L/home/nick/shapelib/lib /home/nick/shapelib/lib/libshp.a  /home/nick/src/libs/libosm/libosm.a /home/nick/src/libs/ccoord/libccoord.a
OBJ = srtm2shp.o SRTMGeneral.o SRTMDataGrid.o SRTMConGen.o 

srtm2shp: $(OBJ)
	$(CXX) -o srtm2shp $(OBJ) $(LDFLAGS) 

srtm2shp.o : srtm2shp.cpp SRTMConGen.h
SRTMGeneral.o : SRTMGeneral.h Map.h EarthPoint.h 
SRTMDataGrid.o : SRTMDataGrid.h Map.h EarthPoint.h
SRTMConGen.o : SRTMConGen.h SRTMDataGrid.h Map.h EarthPoint.h
