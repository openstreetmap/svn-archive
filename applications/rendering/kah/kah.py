from tilenames import *;
from urllib import *
from tilesetToFile import *
import os
import sys

Kosmos = 'Kosmos\Console\Kosmos.Console.exe'


def getMapData(N,W,S,E,Filename):
    Servers = ("http://osmxapi.hypercube.telascience.org",
               "http://xapi.openstreetmap.org",
               "http://www.informationfreeway.org",
               "http://osm.bearstech.com/osmxapi")

    for Server in Servers:
        URL = "%s/api/0.5/map?bbox=%f,%f,%f,%f" % (
            Server,
            W,S,E,N)
        print URL
        (Filename, headers) = urlretrieve(URL, Filename)
        print headers
        if(os.path.getsize(Filename) != 0):
          return(1)
    return(0)

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
    return(1)

def createProject(Rules,DataFile,ProjectFile):
    f = open(ProjectFile, "w")
    f.write("<KosmosProject Version=\"2.2\">\n")
    f.write("<RulesSource><WikiPage>http://wiki.openstreetmap.org/index.php/%s</WikiPage></RulesSource>\n" % Rules)
    f.write("<DataFiles><Osm><FilePath>%s</FilePath></Osm></DataFiles>\n" % DataFile)
    f.write("</KosmosProject>\n")
    f.close()

def uploadTiles(Dir,x,y,z,layer, user, password):
    packedFile = "packed.dat"
    packTileset(Dir,x,y,z,packedFile)

    data = urlencode({
      "x":x,
      "y":y,
      "z":z,
      "layer":layer,
      "user":user,
      "password":password,
      "data":readfile(packedFile)})
    f = urlopen("http://dev.openstreetmap.org/~ojw/kah/upload.php", data)
    print(f.read())
    f.close()


if(__name__ == "__main__"):
  user = sys.argv[0]
  password = sys.argv[1]
  (z,x,y) = (12, 2042, 1362)
  (S,W,N,E) = tileEdges(x,y,z)

  Filename = "data.osm"
  Tiledir = "tiles"
  Layer = "layer1"

  if(getMapData(N,W,S,E, Filename)):
    if(tileGen(N,W,S,E, Filename, Tiledir)):
      if(uploadTiles(Tiledir,x,y,z, Layer, user, password)):
        pass
