
TESTGPSOBJ = testgps.o GPSDevice.o 
OBJ = main.o MainWindow.o GPSDevice.o Segment.o functions.o \
	  Track.o Waypoint.o Components.o  GPXParser.o WaypointDialogue.o \
	  moc_MainWindow.o
PARSEROBJ = testparser.o GPXParser.o Components.o Track.o Waypoint.o
LIBS = -ljeeps 

QTINCPATH = /usr/lib/qt3/include
QTLIB = -lqt-mt 
QTLIBPATH = -L/usr/lib/qt3/lib
CXXFLAGS = -I$(QTINCPATH)

MOC=/usr/lib/qt3/bin/moc

moc_%.cpp : %.h
	${MOC} $< -o $@

osmeditor: $(OBJ)
	g++ -o osmeditor $(OBJ) $(QTLIBPATH) $(LIBS) $(QTLIB) 

testgps: $(TESTGPSOBJ)
	g++ -o testgps $(TESTGPSOBJ) $(LIBS)

testparser : $(PARSEROBJ)
	g++ -o testparser $(PARSEROBJ) $(QTLIBPATH) $(LIBS) $(QTLIB)

clean:
	rm -f *.o *~

main.o : MainWindow.h
GPSDevice.o : GPSDevice.h Track.h Waypoint.h
MainWindow.o : WaypointDialogue.h GPXParser.h Components.h Track.h Polygon.h\
	Waypoint.h functions.h MainWindow.h
Components.o : Components.h Track.h Waypoint.h Segment.h functions.h
Track.o : Track.h Segment.h
Segment.o : Track.h Segment.h
Waypoint.o : Waypoint.h
Polygon.o : Polygon.h functions.h
WaypointDialogue.o : WaypointDialogue.h
functions.o: functions.h
GPXParser.o: GPXParser.h Components.h 
GPSDevice.o: Track.h Waypoint.h Segment.h
