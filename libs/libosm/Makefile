CXXFLAGS = -I/home/nick/expat -I/home/nick/shapelib/include -I../ccoord
LDFLAGS = -L/home/nick/expat/lib -lexpat -lcurl  /home/nick/shapelib/lib/libshp.a
OBJ = Object.o Way.o Parser.o Components.o functions.o llgr.o RulesParser.o
NETOBJ = Client.o
TESTOBJ = test.o
RULESTESTOBJ = rulestest.o
CXX = g++

libosm.a: $(OBJ)
	ar rv libosm.a $(OBJ) 

libosmnet.a: $(NETOBJ)
	ar rv libosmnet.a $(NETOBJ) 

test: $(TESTOBJ) libosm.a libosmnet.a
	$(CXX) -o test $(TESTOBJ) libosm.a libosmnet.a $(LDFLAGS)

rulestest: $(RULESTESTOBJ) libosm.a 
	$(CXX) -o rulestest $(RULESTESTOBJ) libosm.a $(LDFLAGS)

test2: test2.o libosm.a 
	$(CXX) -o test2 test2.o libosm.a libosmnet.a $(LDFLAGS)

wayclean: wayclean.o libosm.a 
	$(CXX) -o wayclean wayclean.o libosm.a $(LDFLAGS) 

Object.o : Object.h
Way.o: Way.h Object.h
Parser.o : Parser.h Way.h Object.h Node.h Segment.h
Client.o : Client.h
llgr.o: llgr.h
RulesParser.o : RulesParser.h ElemStyle.h AreaElemStyle.h LineElemStyle.h \
IconElemStyle.h ElemStyles.h
