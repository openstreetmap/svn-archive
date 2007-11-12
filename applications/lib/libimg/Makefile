CXXFLAGS = -I../libosm/
LDFLAGS = -L../libosm/ -L./ -losm -limg -lexpat -lcurl -lshp
OBJ = Img.o ImgHeader.o ImgSubfile.o ImgData.o ImgFat.o ImgTre.o
OSM2IMGOBJ = osm2img.o 
IMG2IMGOBJ = img2img.o
CXX = g++

ALL: osm2img img2img 

libimg.a: $(OBJ)
	ar rv libimg.a $(OBJ) 

osm2img: $(OSM2IMGOBJ) libimg.a
	$(CXX) -o osm2img $(OSM2IMGOBJ) $(LDFLAGS) $(CXXFLAGS) 

img2img: $(IMG2IMGOBJ) libimg.a
	$(CXX) -o img2img $(IMG2IMGOBJ) $(LDFLAGS) $(CXXFLAGS) 

osm2img.o: Img.h
Img.o: Img.h
ImgData.o: Img.h ImgData.h
ImgHeader.o: ImgHeader.h ImgData.h Img.h
ImgSubfile.o: ImgData.h Img.h ImgSubfile.h ImgFat.h
img2img.o: Img.h

clean:
	rm -f *.a *.o
