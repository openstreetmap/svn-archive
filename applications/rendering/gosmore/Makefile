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

gosmore:	gosmore.cpp ceglue.c ceglue.h translations.c
		g++ ${CFLAGS} ${WARNFLAGS} ${XMLFLAGS} \
		  -D RES_DIR='"$(prefix)/usr/share/"' \
                  gosmore.cpp -o gosmore ${EXTRA}

gosm_arm.exe:	gosmore.cpp gosmore.rsc resource.h translations.c
		${ARCH}-g++ ${CFLAGS} -c gosmore.cpp
		${ARCH}-gcc ${CFLAGS} -c ConvertUTF.c
		${ARCH}-gcc ${CFLAGS} -c ceglue.c
		${ARCH}-gcc ${CFLAGS} -o $@ \
		  gosmore.o ceglue.o ConvertUTF.o gosmore.rsc

gosmore.rsc:	gosmore.rc
		${WINDRES} $? $@
		
WIKIPAGE=http://wiki.openstreetmap.org/index.php/Special:Export/Gosmore
translations.c: extract
		wget -O - ${WIKIPAGE}/Translations |./extract >translations.c

extract:	extract.c
		${CC} ${CFLAGS} ${XMLFLAGS} extract.c -o extract

voices:
		echo '(voice_rab_diphone)' >/tmp/voice_rab_diphone
		echo 'At the junction, turn left.' | festival_client \
		  --prolog /tmp/voice_rab_diphone --output turnleft.wav --ttw
		echo 'At the junction, turn right.' | festival_client \
		  --prolog /tmp/voice_rab_diphone --output turnright.wav --ttw
		echo 'Keep left.' | festival_client \
		  --prolog /tmp/voice_rab_diphone --output keepleft.wav --ttw
		echo 'Keep right.' | festival_client \
		  --prolog /tmp/voice_rab_diphone --output keepright.wav --ttw
		echo 'If possible make a U turn.' | festival_client \
		  --prolog /tmp/voice_rab_diphone --output uturn.wav --ttw
		echo 'You have reached your destination.' | festival_client \
		  --prolog /tmp/voice_rab_diphone --output stop.wav --ttw
		echo 'At the roundabout take the first exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round1.wav --ttw
		echo 'At the roundabout take the second exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round2.wav --ttw
		echo 'At the roundabout take the third exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round3.wav --ttw
		echo 'At the roundabout take the fourth exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round4.wav --ttw
		echo 'At the roundabout take the fifth exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round5.wav --ttw
		echo 'At the roundabout take the sixth exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round6.wav --ttw
		echo 'At the roundabout take the seventh exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round7.wav --ttw
		echo 'At the roundabout take the eight exit.' | \
  festival_client --prolog /tmp/voice_rab_diphone --output round8.wav --ttw

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
	zip -j gosm_arm.zip ARMV4Rel/gosm_arm.exe *.wav
	scp gosm_arm.zip gosmore.zip \
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
	  gosmore.aps gosmore.vcl gosmore.vcw extract *.o gosm_arm.exe
