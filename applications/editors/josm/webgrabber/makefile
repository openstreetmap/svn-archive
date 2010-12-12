#!/usr/bin/make

ifneq ($(windir),)
MINGWPATH = C:\\PROGRA~1\\mingw\\bin\\
QTPATH    = C:\\PROGRA~1\\qt\\

CPP       = ${MINGWPATH}c++.exe
MOC       = ${QTPATH}bin\\moc.exe
RM        = del
CFLAGS    = -W -O2 -I${QTPATH}include
LDFLAGS   = -L${QTPATH}lib
LDLIBS    = -lQtCore4 -lQtWebKit4 -lQtGui4 -lQtNetwork4
else
MOC       = moc
#MOC       = /usr/share/qt4/bin/moc
CPP       = gcc

PACKAGES  = QtCore QtGui QtWebKit QtNetwork
CFLAGS    = -W -O2 `pkg-config --cflags ${PACKAGES}`
LDFLAGS   = `pkg-config --libs-only-L ${PACKAGES}`
LDLIBS    = `pkg-config --libs-only-l ${PACKAGES}`
endif

webkit-image: webkit-image.cpp webkit-image.h
	$(CPP) -o $@ $(CFLAGS) $(LDFLAGS) webkit-image.cpp $(LDLIBS)

webkit-image.h: webkit-image.cpp
	$(MOC) webkit-image.cpp >$@

clean:
	${RM} *.o webkit-image webkit-image.h
