CXX = g++
OBJ = render.o tomerc.o
CXXFLAGS = -O3 -I/usr/include/boost -I/usr/include/freetype2
LDFLAGS = -O3 -L/usr/local/lib -lmapnik  -lboost_filesystem -lgd

render2 : $(OBJ) 
	g++ -o render2 $(OBJ) $(LDFLAGS)

render.o : Tile.h GoogleProjection.h EarthPoint.h tomerc.h
tomerc.o : tomerc.h
