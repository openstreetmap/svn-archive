CXXFLAGS = -I/usr/local/include -I../../../lib/libosm -I/usr/include
LDFLAGS = -L/usr/local/lib ../../../lib/libosm/libosm.a  -lshp -lexpat 
LDFLAGS += ../../../lib/ccoord/libccoord.a
OBJ = osm2shp.o
CXX = g++

osm2shp: $(OBJ)  ../../../lib/libosm/libosm.a
	$(CXX) -o osm2shp $(OBJ) $(CFFFLAGS) $(LDFLAGS)

clean:
	rm -f *.o