XX = c++

TESTGPSOBJ = testgps.o GPSDevice.o 
OBJ = osmeditor.o MainWindow.o GPSDevice.o LandsatManager.o \
	  Track.o Waypoint.o Components.o  GPXParser.o WaypointDialogue.o \
	  Permissions.o LoginDialogue.o moc_MainWindow.o grab_landsat.o \
	  Polygon.o TrackSeg.o SRTMConGen.o SRTMDataGrid.o SRTMGeneral.o llgr.o \
	  RemoveExcessDialogue.o
LIBS = -ljeeps 
LIBPATH = -L/usr/local/lib

SRTMOBJ = srtm.o Canvas.o SRTMGeneral.o SRTMDataGrid.o SRTMConGen.o llgr.o



# Configure Qt paths
QTINCPATH = /usr/include/qt3
QTLIB = -lqt 
QTLIBPATH = -L/usr/lib
MOC=/usr/bin/moc

CURLLIBS = `curl-config --libs`
CURLCFLAGS = `curl-config --cflags`

#set OPTS to -DXMLRPC to compile in XMLRPC stuff
#OPTS       = -DXMLRPC
WARNFLAGS  = -W -Wall
DEBUGFLAGS = -g
JEEPSSRC = jeeps-0.1.4-nick
CXXFLAGS = $(WARNFLAGS) $(DEBUGFLAGS) -I$(QTINCPATH) $(CURLCFLAGS) \
	-I/usr/local/include $(OPTS) -I$(JEEPSSRC)


# Uncomment for XMLRPC. You need xmlrpc-c for XMLRPC support.
#XMLRPCLIBS= -lxmlrpc_cpp  -lxmlrpc -lwwwxml -lxmltok -lxmlparse\
#			-lwwwzip -lwwwinit -lwwwapp -lwwwhtml -lwwwtelnet -lwwwnews \
#			-lwwwhttp -lwwwmime -lwwwgopher -lwwwftp -lwwwfile -lwwwdir \
#			-lwwwcache -lwwwstream -lwwwmux -lwwwtrans -lwwwcore -lwwwutils\
#			-lmd5 -ldl -lz -lssl -lcrypto -lxmlrpc_client

moc_%.cpp : %.h
	${MOC} $< -o $@

osmeditor: $(OBJ)
	cd $(JEEPSSRC); make INSTROOT=/usr/local; make install
	$(CXX) -o osmeditor $(OBJ) $(LIBPATH) $(QTLIBPATH) $(LIBS) $(QTLIB) $(CURLLIBS) $(XMLRPCLIBS) 

srtm: $(SRTMOBJ)
	$(CXX) -o srtm $(SRTMOBJ) -lgd -lpng -lz -ljpeg -lfreetype -lm 

testgps: $(TESTGPSOBJ)
	$(CXX) -o testgps $(TESTGPSOBJ) $(LIBS)

testparser : $(PARSEROBJ)
	$(CXX) -o testparser $(PARSEROBJ) $(QTLIBPATH) $(LIBS) $(QTLIB)

clean:
	rm -f *.o *~

osmeditor.o : MainWindow.h
GPSDevice.o : GPSDevice.h Track.h Waypoint.h functions.h
MainWindow.o : WaypointDialogue.h GPXParser.h Components.h Track.h Polygon.h\
	Waypoint.h functions.h MainWindow.h Permissions.h LoginDialogue.h Map.h\
	LandsatManager.h TrackSeg.h SRTMConGen.h SRTMDataGrid.h SRTMGeneral.h
Components.o : Components.h Track.h Waypoint.h Polygon.h functions.h
Track.o : Track.h TrackSeg.h
Waypoint.o : Waypoint.h
Polygon.o : Polygon.h functions.h
WaypointDialogue.o : WaypointDialogue.h
GPXParser.o: GPXParser.h Components.h 
GPSDevice.o: Track.h Waypoint.h 
Permissions.o: Permissions.h
LoginDialogue.o : LoginDialogue.h
Polygon.o : Polygon.h
LandsatManager.o : LandsatManager.h landsat.h
TrackSeg.o : TrackSeg.h functions.h
RemoveExcessDialogue.o : RemoveExcessDialogue.h
srtm.o : srtm.cpp Canvas.h
Canvas.o : Canvas.cpp Map.h Canvas.h 
SRTMGeneral.o : SRTMGeneral.h Map.h EarthPoint.h 
SRTMDataGrid.o : SRTMDataGrid.h Map.h EarthPoint.h
SRTMConGen.o : SRTMConGen.h SRTMDataGrid.h Map.h EarthPoint.h
llgr.o : llgr.h EarthPoint.h
grab_landsat.o : landsat.h
