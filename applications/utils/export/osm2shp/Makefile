CXXFLAGS = -I/usr/local/include -I../../../libs/libosm -I/usr/include
LDFLAGS = -L/usr/local/lib ../../../libs/libosm/libosm.a  -lshp -lexpat 
OBJ = osm2shp.o
CXX = g++

osm2shp: $(OBJ)  ../../../libs/libosm/libosm.a
	$(CXX) -o osm2shp $(OBJ) $(CFFFLAGS) $(LDFLAGS)

clean:
	rm -f *.o