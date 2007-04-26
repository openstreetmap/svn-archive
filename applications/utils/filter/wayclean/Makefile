CXXFLAGS = -I/usr/local/include -I../../../../lib/libosm 
OSMLIBS = ../../../../lib/libosm/libosm.a ../../../../lib/ccoord/libccoord.a
LDFLAGS = -L/usr/local/lib $(OSMLIBS)  -lexpat  -lshp
OBJ = wayclean.o
CXX = g++

wayclean: $(OBJ)  $(OSMLIBS) 
	$(CXX) -o wayclean $(OBJ) $(LDFLAGS)
