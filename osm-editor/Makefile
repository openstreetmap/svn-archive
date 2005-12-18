DESTDIR =
prefix  = /usr/local
bindir  = $(prefix)/bin

CXX     = c++
RM      = rm -f
INSTALL = install -m 755

TESTGPSOBJ = testgps.o GPSDevice.o 
OBJ = osmeditor.o MainWindow.o GPSDevice.o LandsatManager.o \
	  Track.o Waypoint.o Components.o  GPXParser.o WaypointDialogue.o \
	  RouteMetaDataHandler.o LoginDialogue.o moc_MainWindow.o curlstuff.o \
	  Polygon.o TrackSeg.o SRTMConGen.o SRTMDataGrid.o SRTMGeneral.o llgr.o \
	  RemoveExcessDialogue.o OSMParser.o

OBJ2 = osmeditor2.o MainWindow2.o GPSDevice2.o LandsatManager2.o \
	  Components2.o Node.o Segment.o WaypointDialogue.o \
	  RouteMetaDataHandler.o LoginDialogue.o moc_MainWindow2.o curlstuff.o \
	  SRTMConGen.o SRTMDataGrid.o SRTMGeneral.o llgr.o \
	  OSMParser2.o qmdcodec.o HTTPHandler.o moc_HTTPHandler.o
JEEPSSRC = jeeps-gpsbabel-1.2.7
LIBS = -ljeeps 
LIBPATH = -L$(JEEPSSRC) -L/usr/local/lib

SRTMOBJ = srtm.o Canvas.o SRTMGeneral.o SRTMDataGrid.o SRTMConGen.o llgr.o

# Comment out if you want USB support in jeeps.
USB='USBOPT=-DNO_USB'

# Configure Qt paths
QTINCPATH = /usr/include/qt3
QTLIB = -lqt
QTLIBPATH = -L/usr/lib
MOC=moc

CURLLIBS = `curl-config --libs`
CURLCFLAGS = `curl-config --cflags`

#set OPTS to -DXMLRPC to compile in XMLRPC stuff
#OPTS       = -DXMLRPC
#WARNFLAGS  = -W -Wall
WARNFLAGS = -w
DEBUGFLAGS = -g
CXXFLAGS = $(WARNFLAGS) $(DEBUGFLAGS) -I$(QTINCPATH) $(CURLCFLAGS) \
	-I/usr/local/include $(OPTS) -I$(JEEPSSRC)


# Uncomment for XMLRPC. You need xmlrpc-c for XMLRPC support.
#XMLRPCLIBS = `xmlrpc-c-config c++ libwww-client --libs`

moc_%.cpp : %.h
	${MOC} $< -o $@

osmeditor: $(OBJ)
	$(MAKE) $(USB) -C $(JEEPSSRC)
	$(CXX) -o osmeditor $(OBJ) $(LIBPATH) $(QTLIBPATH) $(LIBS) $(QTLIB) $(CURLLIBS) $(XMLRPCLIBS)

osmeditor2: $(OBJ2)
	$(MAKE) $(USB) -C $(JEEPSSRC)
	$(CXX) -o osmeditor2 $(OBJ2) $(LIBPATH) $(QTLIBPATH) $(LIBS) $(QTLIB) $(CURLLIBS) $(XMLRPCLIBS)

install: osmeditor
	$(INSTALL) -d $(DESTDIR)$(bindir)
	$(INSTALL) osmeditor $(DESTDIR)$(bindir)/osmeditor

srtm: $(SRTMOBJ)
	$(CXX) -o srtm $(SRTMOBJ) -lgd -lpng -lz -ljpeg -lfreetype -lm 

testgps: $(TESTGPSOBJ)
	$(CXX) -o testgps $(TESTGPSOBJ) $(LIBS)

testparser : $(PARSEROBJ)
	$(CXX) -o testparser $(PARSEROBJ) $(QTLIBPATH) $(LIBS) $(QTLIB)

clean:
	$(RM) *.o *~

distclean: clean
	$(RM) osmeditor

osmeditor.o : MainWindow.h
GPSDevice.o : GPSDevice.h Track.h Waypoint.h functions.h
MainWindow.o : WaypointDialogue.h GPXParser.h Components.h Track.h Polygon.h\
	Waypoint.h functions.h MainWindow.h RouteMetaDataHandler.h LoginDialogue.h\
	Map.h LandsatManager.h TrackSeg.h SRTMConGen.h SRTMDataGrid.h\
	SRTMGeneral.h OSMParser.h
MainWindow2.o : WaypointDialogue.h Components2.h \
	functions.h MainWindow2.h RouteMetaDataHandler.h LoginDialogue.h\
	Map.h LandsatManager.h Node.h Segment.h SRTMConGen.h SRTMDataGrid.h\
	SRTMGeneral.h OSMParser2.h GPSDevice2.h
Components.o : Components.h Track.h Waypoint.h Polygon.h functions.h
Components2.o : Components2.h Node.h Segment.h functions.h
Track.o : Track.h TrackSeg.h
Waypoint.o : Waypoint.h
Polygon.o : Polygon.h functions.h
WaypointDialogue.o : WaypointDialogue.h
GPXParser.o: GPXParser.h Components.h 
OSMParser.o: OSMParser.h Components.h 
OSMParser2.o: OSMParser2.h Components2.h 
GPSDevice.o: Track.h Waypoint.h 
GPSDevice2.o: Components2.h 
RouteMetaDataHandler.o: RouteMetaDataHandler.h
LoginDialogue.o : LoginDialogue.h
Polygon.o : Polygon.h
LandsatManager.o : LandsatManager.h curlstuff.h
TrackSeg.o : TrackSeg.h functions.h
RemoveExcessDialogue.o : RemoveExcessDialogue.h
srtm.o : srtm.cpp Canvas.h
Canvas.o : Canvas.cpp Map.h Canvas.h 
SRTMGeneral.o : SRTMGeneral.h Map.h EarthPoint.h 
SRTMDataGrid.o : SRTMDataGrid.h Map.h EarthPoint.h
SRTMConGen.o : SRTMConGen.h SRTMDataGrid.h Map.h EarthPoint.h
llgr.o : llgr.h EarthPoint.h
curlstuff.o : curlstuff.h
Node.o : Node.h
Segment.o : Segment.h
qmdcodec.o: qmdcodec.h
HTTPHandler.o : HTTPHandler.h
