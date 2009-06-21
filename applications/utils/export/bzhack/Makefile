CFLAGS=-Wall -O2 -g -D_FILE_OFFSET_BITS=64
OBJ=bzhack.o

all : bzhack status

status : status.o
	$(CC) $< -o $@
bzhack : $(OBJ)
	$(CC) -lexpat $< -o $@ -lbz2 -lm

clean :
	rm -f $(OBJ) bzhack.so bzhack status status.o
