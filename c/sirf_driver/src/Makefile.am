SUBDIRS = 

bin_PROGRAMS = sirf2gpx sirfdebug sirfheadless

INCLUDES = -I$(top_srcdir)/include \
	-I$(top_srcdir)/include/Packets/Input \
	-I$(top_srcdir)/include/Packets/Output

LDADD = $(top_srcdir)/libSiRF/libSiRF.la \
	$(top_srcdir)/libSiRF/Packets/Input/libPacketInput.la \
	$(top_srcdir)/libSiRF/Packets/Output/libPacketOutput.la

sirf2gpx_SOURCES = AuthorInfo.cpp AuthorInfo.hpp \
	GPXHandler.cpp GPXHandler.hpp \
	SetupGPX.cpp SetupGPX.hpp \
	Coordinates.cpp Coordinates.hpp \
	GPXReporterUI.cpp GPXReporterUI.hpp \
	main.cpp

sirfdebug_SOURCES = sirfdebug.cpp

sirfheadless_SOURCES = headless.cpp \
	AuthorInfo.cpp AuthorInfo.hpp \
	GPXHandler.cpp GPXHandler.hpp \
	Coordinates.cpp Coordinates.hpp
