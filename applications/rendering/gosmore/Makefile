# Written by Nic Roets with contributions by Petter Reinholdtsen
# Placed in the public domain
# Don't run mkicons.sh until my patched pnmmontage is part of debian-netpbm

TODAY := `exec date +%Y%m%d`
VERSION = 0.0.0.$(TODAY)

DESTDIR=
prefix = /usr/local
bindir = $(prefix)/bin

USE_FLITE=-DUSE_FLITE -lflite_cmu_us_kal16 -lflite_usenglish -lflite_cmulex \
  -lflite
CFLAGS=-O2
WARNFLAGS= -W -Wall

# enable this to test the experimental route support
#CFLAGS += -DROUTE_TEST

ifneq (${OS},Windows_NT)
EXTRA=`pkg-config --cflags --libs gtk+-2.0 || echo -D HEADLESS`
else
EXTRA=-mms-bitfields -mno-cygwin -I/usr/include/mingw/gtk-2.0 \
  -I/usr/include/mingw/cairo     -I/usr/include/mingw/glib-2.0 \
  -I/usr/include/mingw/pango-1.0 -I/usr/include/mingw/atk-1.0 \
  -I/usr/lib/glib-2.0/include    -I/usr/lib/gtk-2.0/include \
  -lgtk-win32-2.0 -lgdk-win32-2.0 -lglib-2.0 -lgobject-2.0
endif
XMLFLAGS=`pkg-config --cflags libxml-2.0 || echo -I /usr/include/libxml2` \
  `pkg-config --libs libxml-2.0 || echo -l xml2 -lz -lm`

all: gosmore

gosmore:	gosmore.cpp
		g++ ${CFLAGS} ${WARNFLAGS} ${XMLFLAGS} \
                 `[ -d /usr/include/flite ] && echo ${USE_FLITE}` \
                  gosmore.cpp -o gosmore ${EXTRA}

#elemstyles.xml:
#		wget http://josm.openstreetmap.de/svn/trunk/styles/standard/elemstyles.xml


commit:		clean
		rm -f *~; cd ..; tar czf - gosmore | ssh \
		  sabiepark@www.rational.co.za 'cd www/gosmore; \
		  cat >gosmore-`exec date +%Y%m%d`.tar.gz'

install: gosmore
	mkdir -p $(DESTDIR)$(bindir)
	cp gosmore $(DESTDIR)$(bindir)/.

dist:
	mkdir gosmore-$(VERSION)
	cp gosmore.cc Makefile  README gosmore-$(VERSION)
	tar zcf gosmore-$(VERSION).tar.gz gosmore-$(VERSION)
	rm -rf gosmore-$(VERSION)

clean:
	$(RM) gosmore gosmore.pak *.tmp *~
