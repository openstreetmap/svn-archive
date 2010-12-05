OBJ = guidise.o Parser.o

guidise: $(OBJ) libccoord.a 
	g++ -o guidise $(OBJ) ccoord/libccoord.a -lexpat

libccoord.a:
	cd ccoord; make; cd ..
