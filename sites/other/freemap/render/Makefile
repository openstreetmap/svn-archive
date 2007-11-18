CXX = g++
OBJ = render.o Tile.o
CXXFLAGS = -O3 -I/usr/include/boost -I/usr/include/freetype2
LDFLAGS = -O3 -L/usr/local/lib -lmapnik  -lboost_filesystem -lgd

render : $(OBJ) 
	g++ -o render $(OBJ) $(LDFLAGS)

Tile.o: Tile.h
render.o : Tile.h
