GCC=gcc
CFLAGS=-g -ggdb
LDFLAGS=$(CFLAGS)
BISON=bison
FLEX=flex

all: mk_packet

clean:
	rm -f mk_packet packet.o mk_packet.tab.o lex.yy.o \
		lex.yy.c mk_packet.tab.c mk_packet.output \
		mk_packet.tab.h

mk_packet: packet.o mk_packet.tab.o lex.yy.o
	$(GCC) $(LDFLAGS) -o $@ $^

%.o: %.c
	$(GCC) $(CFLAGS) -c $<

mk_packet.tab.c: mk_packet.y
	$(BISON) -dv $<

lex.yy.c: mk_packet.l
	$(FLEX) $<

packet.c: packet.h
