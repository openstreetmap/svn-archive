compile: webkit-image.cpp webkit-image.h
	g++ -o webkit-image -I/usr/include/qt4  -O2 $(pkg-config --libs --cflags QtGui QtWebKit) webkit-image.cpp

webkit-image.h: webkit-image.cpp
	/usr/share/qt4/bin/moc webkit-image.cpp >webkit-image.h 

clean:
	rm -f *.o webkit-image webkit-image.h
