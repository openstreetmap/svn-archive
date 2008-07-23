# Written by Nic Roets with contributions by Petter Reinholdtsen
# Placed in the public domain
# Don't run mkicons.sh until my patched pnmmontage is part of debian-netpbm

TODAY := `exec date +%Y%m%d`
VERSION = 0.0.0.$(TODAY)

DESTDIR=
prefix = /usr/local
bindir = $(prefix)/bin

CFLAGS=-O2
WARNFLAGS= -W -Wall

#------------------------ Compiling with cegcc : ---------------------------
# tar xzf -C / cygwin-cegcc-mingw32ce-0.51.0-1.tar.gz
# export PATH="$PATH":/opt/mingw32ce/bin/
ARCH=		arm-wince-mingw32ce
WINDRES=	${ARCH}-windres

# enable this to test the experimental route support
#CFLAGS += -DROUTE_TEST

ifneq (${OS},Windows_NT)
EXTRA=`pkg-config --cflags --libs gtk+-2.0 || echo -D HEADLESS`
XMLFLAGS=`pkg-config --cflags libxml-2.0 || echo -I /usr/include/libxml2` \
  `pkg-config --libs libxml-2.0 || echo -l xml2 -lz -lm`
else
EXTRA=-mms-bitfields -mno-cygwin -I/usr/include/mingw/gtk-2.0 \
  -I/usr/include/mingw/cairo     -I/usr/include/mingw/glib-2.0 \
  -I/usr/include/mingw/pango-1.0 -I/usr/include/mingw/atk-1.0 \
  -I/usr/lib/glib-2.0/include    -I/usr/lib/gtk-2.0/include \
  -lgtk-win32-2.0 -lgdk-win32-2.0 -lglib-2.0 -lgobject-2.0 -lcairo
endif

all: gosmore

gosmore:	gosmore.cpp
		g++ ${CFLAGS} ${WARNFLAGS} ${XMLFLAGS} \
		  -D RES_DIR='"$(prefix)/usr/share/"' \
                  gosmore.cpp -o gosmore ${EXTRA}

gosm_arm.exe:	gosmore.cpp ConvertUTF.c ConvertUTF.h gosmore.rsc resource.h
		${ARCH}-g++ ${CFLAGS} -c gosmore.cpp
		${ARCH}-gcc ${CFLAGS} -c ConvertUTF.c
		${ARCH}-gcc ${CFLAGS} -c ceglue.c
		${ARCH}-gcc ${CFLAGS} -o $@ \
		  gosmore.o ConvertUTF.o gosmore.rsc

gosmore.rsc:	gosmore.rc
		${WINDRES} $? $@
		

#elemstyles.xml:
#		wget http://josm.openstreetmap.de/svn/trunk/styles/standard/elemstyles.xml

zip:
	rm -f gosmore.zip
	zip -j gosmore.zip gosmore.exe icons.xpm /bin/libcairo-2.dll \
	/bin/libgtk-win32-2.0-0.dll /bin/libgdk* /bin/libglib-2.0-0.dll \
	/bin/libgobject-2.0-0.dll /bin/libatk-1.0-0.dll /bin/libpango* \
	/bin/libpng13.dll /bin/libgmodule-2.0-0.dll w32/zlib1.dll \
	w32/iconv.dll w32/intl.dll
	zip gosmore.zip /etc/gtk-2.0/* /lib/gtk-2.0/2.10.0/loaders/*
	scp gosmore.zip ARMV4Rel/gosm_arm.exe \
	  sabiepark@www.rational.co.za:www/gosmore/

install: gosmore
	mkdir -p $(DESTDIR)$(bindir)
	cp gosmore $(DESTDIR)$(bindir)/.
	mkdir -p $(DESTDIR)$(prefix)/share/gosmore
	cp -a elemstyles.xml icons.csv icons.xpm $(DESTDIR)$(prefix)/share/gosmore

dist:
	mkdir gosmore-$(VERSION)
	cp gosmore.cpp Makefile elemstyles.xml icons.csv icons.xpm  README \
	  gosmore-$(VERSION)
	tar zcf gosmore-$(VERSION).tar.gz gosmore-$(VERSION)
	rm -rf gosmore-$(VERSION)

clean:
	$(RM) gosmore *.tmp *~ gosmore.zip gosmore.exe \
	  gosmore.aps gosmore.vcl gosmore.vcw
