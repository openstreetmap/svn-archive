#!/usr/bin/make
LDFLAGS = `pkg-config --libs --cflags QtCore QtGui QtWebKit`

compile: webkit-image.cpp webkit-image.h
	g++ -W -o webkit-image -I/usr/include/qt4/ -O2 $(LDFLAGS) webkit-image.cpp

webkit-image.h: webkit-image.cpp
	/usr/share/qt4/bin/moc webkit-image.cpp >webkit-image.h 

clean:
	rm -f *.o webkit-image webkit-image.h
