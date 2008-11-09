from tilenames import *;
from urllib import *
from tilesetToFile import *
import os
import sys

Kosmos = 'Kosmos\Console\Kosmos.Console.exe'


def getMapData(N,W,S,E,Filename):
    URL = "http://%s/api/0.5/map?bbox=%f,%f,%f,%f" % (
	"xapi.openstreetmap.org",
        W,S,E,N)
    print URL
    urlretrieve(URL, Filename)

def tileGen(N,W,S,E,Filename,Tiledir):
    # Create Kosmos project
    ProjectFile = "render2.kpr"
    createProject("Kosmos_AirNav_Rules",Filename,ProjectFile)

    # Render it
    Cmd = "%s tilegen %s %f %f %f %f %d %d -ts %s" % (
        Kosmos,
        ProjectFile,
        S,W,N,E,
        12,
        17,
        Tiledir)
    os.system(Cmd)

def createProject(Rules,DataFile,ProjectFile):
    f = open(ProjectFile, "w")
    f.write("<KosmosProject Version=\"2.2\">\n")
    f.write("<RulesSource><WikiPage>http://wiki.openstreetmap.org/index.php/%s</WikiPage></RulesSource>\n" % Rules)
    f.write("<DataFiles><Osm><FilePath>%s</FilePath></Osm></DataFiles>\n" % DataFile)
    f.write("</KosmosProject>\n")
    f.close()

def uploadTiles(Dir,x,y,z,layer):
    packedFile = "packed.dat"
    packTileset(Dir,x,y,z,packedFile)

    data = urlencode({
      "x":x,
      "y":y,
      "z":z,
      "layer":layer,
      "user":"ojw",
      "password":"478",
      "data":readfile(packedFile)})
    f = urlopen("http://dev.openstreetmap.org/~ojw/kah/upload.php", data)
    print(f.read())
    f.close()


if(0):
  (z,x,y) = (12, 2042, 1362)
  (S,W,N,E) = tileEdges(x,y,z)

  Filename = "data.osm"
  Tiledir = "tiles"

  if(not os.path.exists(Filename)): # just for testing
      getMapData(N,W,S,E, Filename)
  tileGen(N,W,S,E, Filename, Tiledir)
  #uploadTiles(Tiledir,x,y,z)

for layer in ("layer1","layer2","layer3"):
  for x in range(1280, 1284):
    for y in range(1000, 1004):
      uploadTiles("files/%s"%layer, x, y, 12, layer)
