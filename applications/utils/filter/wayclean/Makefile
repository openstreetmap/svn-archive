CXXFLAGS = -I/usr/local/include -I../../libs/libosm 
OSMLIBS = ../../libs/libosm.a ../../libs/libccoord.a
LDFLAGS = -L/usr/local/lib $(OSMLIBS)  -lexpat  -lshp
OBJ = wayclean.o
CXX = g++

wayclean: $(OBJ)  $(OSMLIBS) 
	$(CXX) -o wayclean $(OBJ) $(LDFLAGS)
