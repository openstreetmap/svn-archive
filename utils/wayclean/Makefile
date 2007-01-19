CXXFLAGS = -I/usr/local/include -I../../libs/libosm 
LDFLAGS = -L/usr/local/lib ../../libs/libosm/libosm.a  -lexpat  -lshp
OBJ = wayclean.o
CXX = g++

wayclean: $(OBJ)  ../../libs/libosm/libosm.a
	$(CXX) -o wayclean $(OBJ) $(LDFLAGS)
