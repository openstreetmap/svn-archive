# Written by Nic Roets with contributions by Petter Reinholdtsen
# Placed in the public domain

TODAY := `exec date +%Y%m%d`
VERSION = 0.0.0.$(TODAY)

DESTDIR=
prefix = /usr/local
bindir = $(prefix)/bin
datarootdir = $(prefix)/share

CFLAGS=-O2 -DRES_DIR=\"$(prefix)/share/gosmore/\"
WARNFLAGS= -W -Wall

#"------------------------ Compiling with cegcc : ---------------------------
# tar xzf -C / cygwin-cegcc-mingw32ce-0.51.0-1.tar.gz
# export PATH="$PATH":/opt/mingw32ce/bin/

WINDRES=	${ARCH}windres

# enable this to test the experimental route support
#CFLAGS += -DROUTE_TEST

# enable this to force gosmore into headless mode (gosmore will also
# be put in headless mode if gtk+-2.0 isn't available)
#CFLAGS += -DHEADLESS

ifneq (${OS},Windows_NT)
EXTRA=`pkg-config --cflags --libs gtk+-2.0 || echo -D HEADLESS` \
  `pkg-config --libs libcurl` `pkg-config --libs pkg-config --libs gthread-2.0`
XMLFLAGS=`pkg-config --cflags libxml-2.0 || echo -I /usr/include/libxml2` \
  `pkg-config --libs libxml-2.0 || echo -l xml2 -lz -lm`
ARCH=arm-mingw32ce-
else
# To compile with mingw, install MSYS and mingw, and then download
# the "all-in-one bundle" from http://www.gtk.org/download-windows.html
# and unzip it to C:\msys\1.0.
EXTRAC=-mms-bitfields -mno-cygwin -mwindows \
  `pkg-config --cflags gtk+-2.0 || echo -D NOGTK`
EXTRAL=`pkg-config --libs gtk+-2.0`
EXE=.exe
endif

all: gosmore$(EXE)

# The planet is too big to fit into the address space of a single process on
# a 32 bit CPUs. So we break it up into pieces (overlapping rectangles and
# some lowzoom extracts) and then run one process for each piece. The parent
# task then chooses the most
# appropriate process and forwards the expose, search or routing request to
# it. THE CODE IS NOT FINISHED. Linux version looks promising.
gosmore:	gosmore.cpp libgosm.cpp libgosm.h bboxes.c
		g++ -DCHILDREN=16 ${CFLAGS} ${WARNFLAGS} ${XMLFLAGS} \
                  gosmore.cpp libgosm.cpp -o gosmore ${EXTRA}

gosmore16:	gosmore.cpp libgosm.cpp libgosm.h
		g++ -DGOSMZ=16 ${CFLAGS} ${WARNFLAGS} ${XMLFLAGS} \
                  gosmore.cpp libgosm.cpp -o gosmore16 ${EXTRA}

$(ARCH)gosmore.exe:	gosmore.cpp libgosm.cpp gosmore.rsc resource.h \
                    libgosm.h ceglue.h ceglue.c bboxes.c
		${ARCH}g++ ${CFLAGS} ${EXTRAC} -c gosmore.cpp
		${ARCH}g++ ${CFLAGS} ${EXTRAC} -c libgosm.cpp
		${ARCH}gcc ${CFLAGS} ${EXTRAC} -c ConvertUTF.c
		${ARCH}gcc ${CFLAGS} ${EXTRAC} -c ceglue.c
		${ARCH}g++ -static ${CFLAGS} ${EXTRAC} -o $@ \
		  gosmore.o libgosm.o ceglue.o ConvertUTF.o gosmore.rsc

gosmore.rsc:	gosmore.rc icons.bmp icons-mask.bmp gosmore.ico
		$(WINDRES) $< $@

WIKIPAGE=http://wiki.openstreetmap.org/index.php/Special:Export/Gosmore
translations.c: extract
		wget -O - ${WIKIPAGE}/Translations |./extract >translations.c

extract:	extract.c
		${CC} ${CFLAGS} ${XMLFLAGS} extract.c -o extract

bboxes.c:	density.c density.txt
		gcc -lm density.c -o density
		./density <density.txt >density.sh
# wget http://www.openstreetmap.org/api/0.6/changeset/1707270/download -O \
#   countries.osm

osmunda:	osmunda.cpp libgosm.cpp libgosm.h
		g++ ${CFLAGS} ${WARNFLAGS} ${XMLFLAGS} \
		  osmunda.cpp libgosm.cpp -o osmunda
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
	(cd /msys; zip - etc/gtk-2.0/* lib/gtk-2.0/2.10.0/loaders/*) >gosmore.zip
	zip -j gosmore.zip gosmore.exe icons.xpm /msys/bin/libcairo-2.dll \
	/msys/bin/lib*.dll /msys/bin/intl*.dll /msys/bin/zlib*.dll
	zip -j gosm_arm.zip ARMV4Rel/gosm_arm.exe *.wav
	# scp -P 100 gosm_arm.zip gosmore.zip \
	#  nroets@nroets.openhost.dk:nroets.openhost.dk/htdocs/

install: gosmore default.pak
	mkdir -p $(DESTDIR)$(bindir)
	cp gosmore $(DESTDIR)$(bindir)/.
	mkdir -p $(DESTDIR)$(datarootdir)/gosmore
	cp -a default.pak elemstyles.xml icons.csv icons.xpm $(DESTDIR)$(datarootdir)/gosmore
	mkdir -p $(DESTDIR)$(datarootdir)/man/man1
	gzip <gosmore.1 >$(DESTDIR)$(datarootdir)/man/man1/gosmore.1.gz
	mkdir -p $(DESTDIR)$(datarootdir)/pixmaps
	cp -a gosmore.xpm $(DESTDIR)$(datarootdir)/pixmaps
	mkdir -p $(DESTDIR)$(datarootdir)/applications
	cp -a gosmore.desktop $(DESTDIR)$(datarootdir)/applications

default.pak: gosmore
	! [ -e gosmore.pak ]
	(echo '<?xml version="1.0" encoding="UTF-8" ?><osm version="0.6">'; \
	 bzcat lowres.osm.bz2; \
	 egrep -v '?xml|<osmCha' countries.osm | sed -e 's|/osmChange|/osm|') | \
	     QUERY_STRING=suppressGTK ./gosmore rebuild
	mv gosmore.pak default.pak

dist:
	mkdir gosmore-$(VERSION)
	cp gosmore.cpp Makefile elemstyles.xml icons.csv icons.xpm  README \
	  gosmore-$(VERSION)
	tar zcf gosmore-$(VERSION).tar.gz gosmore-$(VERSION)
	rm -rf gosmore-$(VERSION)

clean:
	$(RM) gosmore *.tmp *~ gosmore.zip $(ARCH)gosmore.exe gosmore.rsc \
	  gosmore.aps gosmore.vcl gosmore.vcw extract *.o
