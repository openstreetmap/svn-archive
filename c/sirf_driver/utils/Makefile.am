SUBDIRS=

BUILT_SOURCES = packet_yacc.h packet_yacc.c packet_lex.c
AM_YFLAGS = -d

bin_PROGRAMS = mkpacket
nodist_mkpacket_SOURCES = packet.c packet_yacc.y packet_lex.l
mkpacket_LDADD = @LEXLIB@

