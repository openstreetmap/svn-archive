DESTDIR
prefix = /usr/local
bindir = $(prefix)/bin

USE_FLITE=-DUSE_FLITE -lflite_cmu_us_kal16 -lflite_usenglish -lflite_cmulex \
  -lflite
CFLAGS=-O2 -W -Wall

ifneq (${OS},Windows_NT)
EXTRA=`pkg-config --cflags --libs gtk+-2.0`
else
EXTRA=-mms-bitfields -mno-cygwin -I/usr/include/mingw/gtk-2.0 \
  -I/usr/include/mingw/cairo     -I/usr/include/mingw/glib-2.0 \
  -I/usr/include/mingw/pango-1.0 -I/usr/include/mingw/atk-1.0 \
  -I/usr/lib/glib-2.0/include    -I/usr/lib/gtk-2.0/include \
  -lgtk-win32-2.0 -lgdk-win32-2.0 -lglib-2.0 -lgobject-2.0
endif

gosmore:	gosmore.cc
		g++ ${CFLAGS} \
                 `[ -e /usr/include/gps.h ] && echo -DUSE_GPSD -lgps` \
                 `[ -d /usr/include/flite ] && echo ${USE_FLITE}` \
                  gosmore.cc -o gosmore ${EXTRA}

commit:		gosmore
		tar cf - Makefile gosmore.cc | ssh \
		  sabiepark@www.rational.co.za 'cd www; tar xvf -'

CountColours:	CountColours.c
		gcc `pkg-config --cflags --libs gtk+-2.0` CountColours.c -o CountColours

install:
	mkdir -p $(DESTDIR)$(bindir)
	cp gosmore $(DESTDIR)$(bindir)/.
