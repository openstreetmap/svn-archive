SUBDIRS = Packets

lib_LTLIBRARIES = libSiRF.la
INCLUDES = -I$(top_srcdir)/include \
	-I$(top_srcdir)/include/Packets/Input \
	-I$(top_srcdir)/include/Packets/Output

libSiRF_la_SOURCES = DumpPacketHandler.cpp \
	OutputStream.cpp \
	SiRFDevice.cpp \
	InputPacket.cpp \
	Packet.cpp \
	UnknownPacket.cpp \
	InputStream.cpp \
	PacketFactory.cpp \
	WarnPacketHandler.cpp \
	OutputPacket.cpp \
	sirf_lowlevel.c \
	Packets/Input/libPacketInput.la \
	Packets/Output/libPacketOutput.la

libSiRF_la_LIBADD = -lpthread
