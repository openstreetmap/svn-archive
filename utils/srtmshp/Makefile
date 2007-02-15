CXXFLAGS =  -I../../libs/libosm
LDFLAGS = -L../../libs/libosm -L../../libs/ccoord -lshp -losm -lccoord
OBJ = srtm2shp.o SRTMGeneral.o SRTMDataGrid.o SRTMConGen.o 

srtm2shp: $(OBJ)
	$(CXX) -o srtm2shp $(OBJ) $(LDFLAGS) 

srtm2shp.o : srtm2shp.cpp SRTMConGen.h
SRTMGeneral.o : SRTMGeneral.h Map.h EarthPoint.h 
SRTMDataGrid.o : SRTMDataGrid.h Map.h EarthPoint.h
SRTMConGen.o : SRTMConGen.h SRTMDataGrid.h Map.h EarthPoint.h
