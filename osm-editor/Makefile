CXX = g++

TESTGPSOBJ = testgps.o GPSDevice.o 
OBJ = main.o MainWindow.o GPSDevice.o Segment.o functions.o \
	  Track.o Waypoint.o Components.o  GPXParser.o WaypointDialogue.o \
	  Permissions.o LoginDialogue.o moc_MainWindow.o
LIBS = -ljeeps 
LIBPATH = -L/usr/local/lib

# Configure Qt paths
QTINCPATH = /usr/lib/qt3/include
QTLIB = -lqt-mt 
QTLIBPATH = -L/usr/lib/qt3/lib
MOC=/usr/lib/qt3/bin/moc

#set OPTS to -DXMLRPC to compile in XMLRPC stuff
CXXFLAGS = -I$(QTINCPATH) -I/usr/local/include $(OPTS)

JEEPSSRC = jeeps-0.1.4-nick

# Uncomment for XMLRPC. You need xmlrpc-c for XMLRPC support.
#XMLRPCLIBS= -lxmlrpc_cpp -lxmlrpc_server -lxmlrpc -lwwwxml -lxmltok -lxmlparse\
			-lwwwzip -lwwwinit -lwwwapp -lwwwhtml -lwwwtelnet -lwwwnews \
			-lwwwhttp -lwwwmime -lwwwgopher -lwwwftp -lwwwfile -lwwwdir \
			-lwwwcache -lwwwstream -lwwwmux -lwwwtrans -lwwwcore -lwwwutils\
			-lmd5 -ldl -lz -lssl -lcrypto -lxmlrpc_client

moc_%.cpp : %.h
	${MOC} $< -o $@

osmeditor: $(OBJ)
	#cd $(JEEPSSRC); make INSTROOT=/usr/local; make install
	$(CXX) -o osmeditor $(OBJ) $(LIBPATH) $(QTLIBPATH) $(LIBS) $(QTLIB) $(XMLRPCLIBS)

testgps: $(TESTGPSOBJ)
	$(CXX) -o testgps $(TESTGPSOBJ) $(LIBS)

testparser : $(PARSEROBJ)
	$(CXX) -o testparser $(PARSEROBJ) $(QTLIBPATH) $(LIBS) $(QTLIB)

clean:
	rm -f *.o *~

main.o : MainWindow.h
GPSDevice.o : GPSDevice.h Track.h Waypoint.h functions.h
MainWindow.o : WaypointDialogue.h GPXParser.h Components.h Track.h Polygon.h\
	Waypoint.h functions.h MainWindow.h Permissions.h LoginDialogue.h Map.h
Components.o : Components.h Track.h Waypoint.h Segment.h functions.h
Track.o : Track.h Segment.h
Segment.o : Track.h Segment.h
Waypoint.o : Waypoint.h
Polygon.o : Polygon.h functions.h
WaypointDialogue.o : WaypointDialogue.h
functions.o: functions.h
GPXParser.o: GPXParser.h Components.h 
GPSDevice.o: Track.h Waypoint.h Segment.h
Permissions.o: Permissions.h
LoginDialogue.o : LoginDialogue.h
