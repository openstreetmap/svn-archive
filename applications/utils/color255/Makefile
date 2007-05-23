#CFLAGS += -g
CFLAGS += -O2
CFLAGS += $(shell Wand-config --cflags --cppflags)
LDFLAGS += $(shell Wand-config --ldflags --libs)

APPS:=color255

.PHONY: all clean

all: $(APPS)

clean: 
	rm -f  $(APPS)
