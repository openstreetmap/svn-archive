#!/usr/bin/env python
#-------------------------------------------------------------------------
# Packs a directory of PNG map images into one file, as described in:
# 
# http://wiki.openstreetmap.org/index.php/Tiles%40home/Tileset_as_one_file
# 
# Written by Oliver White, 2008
# This file is public-domain
#-------------------------------------------------------------------------
import os
import struct

def readfile(filename):
    """Reads a binary file"""
    f = open(filename, "rb")
    data = f.read()
    f.close()
    return(data)

def packTileset(directory, baseX, baseY, baseZ, outputFile):
    """Create outputFile, containing images from directory"""
    
    Blank = readfile("blank/blank.png")
    
    f = open(outputFile, "wb+")
    for i in range(0,1366):
        f.write("*---")

    index = 0
    offset = 1366*4
    sizes = []
    num_blank = 0

    for zi in range(0,6): # increasing zoom level from baseZ
        size = 2 ** zi
        z = baseZ + zi
        for yi in range(0,size):
            for xi in range(0,size):
                x = baseX * size + xi
                y = baseY * size + yi
                filename = "%s/%d/%d/%d.png" % (directory,z,x,y)
                data = ''
                filesize = 0
                if(os.path.exists(filename)):
                    data = readfile(filename)
                    filesize = len(data)
                else:
                    print "No %s" % filename
                
                if(data == Blank):
                  sizes.append(1)
                  num_blank += 1
                else:
                  if(filesize):
                      f.write(data)
                  sizes.append(offset) # will be written to beginning of file later
                  offset += filesize
                index += 1

    print "%d blank of %d total" % (num_blank, index)

    # Add the final offset, to mark the size of the last tile
    sizes.append(offset)

    # Write offsets to beginning of file
    f.seek(0, 0)
    for i in range(0,1366):
        f.write(struct.pack("I", (sizes[i])))
    f.close()


if(__name__ == "__main__"):
    packTileset(".", 2042, 1362, 12, "output.dat")
