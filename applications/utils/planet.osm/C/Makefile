MYSQL_CFLAGS += -g -O2 -Wall
MYSQL_CFLAGS += $(shell mysql_config --cflags)

MYSQL_LDFLAGS += $(shell mysql_config --libs)

.PHONY: all clean

all: planet UTF8Sanitizer

clean:
	rm -f UTF8Sanitizer planet

planet: planet.c keyvals.c
	$(CC) $(MYSQL_CFLAGS) $(MYSQL_LDFLAGS) -o $@ $^

UTF8Sanitizer: UTF8sanitizer.c
	$(CC) UTF8sanitizer.c -o UTF8Sanitizer


