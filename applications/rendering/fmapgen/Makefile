CXXFLAGS = -I/home/nick/shapelib/include -I/home/nick/src/libs/libosm  -I/usr/local/include/mapnik -I/usr/include/boost -I/usr/include/freetype2 
LDFLAGS = -L/home/nick/shapelib/lib /home/nick/src/libs/libosm/libosm.a  /home/nick/src/libs/libosm/libosmnet.a /home/nick/shapelib/lib/libshp.a -lexpat -L/home/nick/expat/lib -L/usr/local/lib -lmapnik -lcurl
OBJ = fmapgen.o
CXX = g++

fmapgen: $(OBJ) /home/nick/src/libs/libosm/libosm.a
	$(CXX) -o fmapgen $(OBJ) $(LDFLAGS)

