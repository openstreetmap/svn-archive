DESTDIR =
prefix  = /usr/local
bindir  = $(prefix)/bin

CXX     = c++
RM      = rm -f
INSTALL = install -m 755

TESTGPSOBJ = testgps.o GPSDevice.o 

OBJ2 = osmeditor2.o MainWindow2.o GPSDevice2.o LandsatManager2.o \
	  Components2.o Node.o Segment.o WaypointDialogue.o WayDialogue.o \
	  RouteMetaDataHandler.o LoginDialogue.o moc_MainWindow2.o \
	  SRTMConGen.o SRTMDataGrid.o SRTMGeneral.o llgr.o GPXParser2.o \
	  OSMParser2.o qmdcodec.o HTTPHandler.o moc_HTTPHandler.o Way.o \
	  NodeHandler.o moc_NodeHandler.o moc_LandsatManager2.o SegSplitter.o \
	  BatchUploader.o moc_SegSplitter.o moc_BatchUploader.o moc_WayDialogue.o \
	  NodeMetaDataHandler.o
JEEPSSRC = jeeps-gpsbabel-1.2.7
LIBS = -ljeeps 
LIBPATH = -L$(JEEPSSRC) -L/usr/local/lib

SRTMOBJ = srtm.o Canvas.o SRTMGeneral.o SRTMDataGrid.o SRTMConGen.o llgr.o

# Comment out if you want USB support in jeeps.
USB='USBOPT=-DNO_USB'

# Configure Qt paths
# Hints if problems: try interchanging -lqt-mt and -lqt.

# Mandrake 10.1
QTINCPATH = /usr/lib/qt3/include
QTLIB = -lqt-mt 
QTLIBPATH = -L/usr/lib/qt3/lib
MOC=/usr/lib/qt3/bin/moc

# Fedora Core 5
#QTINCPATH = /usr/lib/qt-3.3/include
#QTLIB = -lqt-mt
#QTLIBPATH = -L/usr/lib/qt-3.3/lib
#MOC=/usr/lib/qt-3.3/bin/moc

# Debian sarge and testing
#QTINCPATH = /usr/include/qt3
#QTLIB = -lqt 
#QTLIBPATH = -L/usr/lib
#MOC=moc

# Gentoo
#QTINCPATH = /usr/qt/3/include
#QTLIB = -lqt 
#QTLIBPATH = -L/usr/qt/3/lib
#MOC=/usr/qt/3/bin/moc

#set OPTS to -DXMLRPC to compile in XMLRPC stuff
#OPTS       = -DXMLRPC
#WARNFLAGS  = -W -Wall
WARNFLAGS = -w
DEBUGFLAGS = -g
CXXFLAGS = $(WARNFLAGS) $(DEBUGFLAGS) -I$(QTINCPATH) \
	-I/usr/local/include $(OPTS) -I$(JEEPSSRC)


# Uncomment for XMLRPC. You need xmlrpc-c for XMLRPC support.
#XMLRPCLIBS = `xmlrpc-c-config c++ libwww-client --libs`

moc_%.cpp : %.h
	${MOC} $< -o $@

osmeditor2: $(OBJ2)
	$(MAKE) $(USB) -C $(JEEPSSRC)
	$(CXX) -o osmeditor2 $(OBJ2) $(LIBPATH) $(QTLIBPATH) $(LIBS) $(QTLIB) $(XMLRPCLIBS)

install: osmeditor2
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

MainWindow2.o : WaypointDialogue.h Components2.h \
	functions.h MainWindow2.h RouteMetaDataHandler.h LoginDialogue.h\
	Map.h LandsatManager2.h Node.h Segment.h SRTMConGen.h SRTMDataGrid.h\
	SRTMGeneral.h OSMParser2.h GPSDevice2.h Way.h WayDialogue.h NodeHandler.h\
	HTTPHandler.h SegSplitter.h BatchUploader.h
Components2.o : Components2.h Node.h Segment.h functions.h
Polygon.o : Polygon.h functions.h
WaypointDialogue.o : WaypointDialogue.h
WayDialogue.o : WayDialogue.h
OSMParser2.o: OSMParser2.h Components2.h 
GPXParser2.o: GPXParser2.h Components2.h 
GPSDevice2.o: Components2.h 
RouteMetaDataHandler.o: RouteMetaDataHandler.h
LoginDialogue.o : LoginDialogue.h
Polygon.o : Polygon.h
RemoveExcessDialogue.o : RemoveExcessDialogue.h
srtm.o : srtm.cpp Canvas.h
Canvas.o : Canvas.cpp Map.h Canvas.h 
SRTMGeneral.o : SRTMGeneral.h Map.h EarthPoint.h 
SRTMDataGrid.o : SRTMDataGrid.h Map.h EarthPoint.h
SRTMConGen.o : SRTMConGen.h SRTMDataGrid.h Map.h EarthPoint.h
llgr.o : llgr.h EarthPoint.h
Node.o : Node.h
Segment.o : Segment.h
Way.o : Way.h
qmdcodec.o: qmdcodec.h
HTTPHandler.o : HTTPHandler.h
NodeHandler.o : NodeHandler.h
SegSplitter.o : SegSplitter.h Segment.h Node.h Way.h Components2.h HTTPHandler.h
BatchUploader.o : BatchUploader.h Segment.h Node.h Components2.h HTTPHandler.h
NodeMetaDataHandler.o : NodeMetaDataHandler.h
