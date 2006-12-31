CXXFLAGS = -I/home/nick/shapelib/include -I../../libs/libosm 
LDFLAGS = -L/home/nick/shapelib/lib ../../libs/libosm/libosm.a  /home/nick/shapelib/lib/libshp.a -lexpat -L/home/nick/expat/lib
OBJ = osm2shp.o
CXX = g++

osm2shp: $(OBJ)  ../../libs/libosm/libosm.a
	$(CXX) -o osm2shp $(OBJ) $(LDFLAGS)

