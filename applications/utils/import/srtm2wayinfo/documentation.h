/* This file contains the main documentation. It's easier to read if you first
 * process it using doxygen.Just run the command "doxygen" in this directory.
 * You'll find the final documentation (which includes the code documentation
 * embedded in the other files) in the directory "docs/html".
 */
/** \mainpage
\author Hermann Kraus

\section introduction Introduction
This program reads OpenStreetMap files and calculates the altitude differences
along each way that has a \e "highway" tag. The "up" and "down" values are
output separately, so you can decide if it is a hilly way where just the start
and end node are at almost the same altitude or if the way is really flat.

This manual is divided in the following sections:
- \subpage installation
- \subpage usage
- \subpage output
- \subpage structure

If you have any problems using this program contact me at <b>h e r m (at) s c r i b u s (dot) i n f o</b>.

\page installation Installation
\section Dependencies
This program uses the following libraries:
\li \b libcurl (debian packages: \e libcurl3 and \e libcurl4-gnutls-dev or
    \e libcurl4-openssl-dev)
\li \b zziplib (debian packages: \e libzzip-0-13 and \e libzzip-dev)
\li \b QT4 (debian packages: \e libqtcore4 and \e libqt4-dev)

and it uses

\li \b cmake (debian package: \e cmake)

as a build tool.
Please make sure you have them installed.

\section Compiling
Just run the following sequence of commands:
\verbatim
mkdir build
cd build
cmake ..
make
\endverbatim
optionally you can do
\verbatim
make install
\endverbatim
as root.

More information about cmake's out-of-source builds can be found at
http://www.cmake.org/Wiki/CMake_FAQ#What_is_an_.22out-of-source.22_build.3F

\page usage Usage
Typical usage:
\verbatim
bzcat planet.osm.bz2 | srtm2wayinfo -o altitude.osm
\endverbatim

For more details see the output of
\verbatim
srtm2wayinfo --help
\endverbatim

\page output Output format
Only ways which have a \e "highway" tag are considered by this program. For
example it would be quite useless to calculate the altitude differences along
the perimeter of a forest.

Each highway is split at each junction with another highway. Then for each
segment a relation is created:

\verbatim
<relation id="-1" visible="true">
        <member type="way" ref="100" role=""/>
        <member type="node" ref="21" role="from"/>
        <member type="node" ref="42" role="to"/>
        <tag k="length" v="0.678"/>
        <tag k="up" v="23.4"/>
        <tag k="down" v="12.3"/>
</relation>
\endverbatim

\li The \e way member refers to the way this relation was created for.
\li The \e from node is either the first node of the way or the node of the
    intersection where the last relation ended.
\li The \e to node is either the last node of the way or a intersection node.
\li The \e length, \e up, \e down values only apply to the part of the way, that
    is between the \e from and the \e to node.


\page structure Program structure
\li SrtmDownloader is responsible for downloading the SRTM tiles (if available) and returning the
altitude data.
SrtmDownloader::getAltitudeFromLatLon() should be the only function you have to use after initialization, but of if you want to do other things with the tiles you can get the SrtmTile objects directly. An
enhancement in the future might to be to download SRTM1 tiles where available. This is possible with the current achitecture, but not implemented yet. SrtmDownloader does not depend on other parts of the project and could easily be reused. It only need the server's address, which it gets in the main() function, by calling the functions from the Settings object.
\li An OsmData object is used to parse the input file and create an internal representation using OsmNode and OsmWay. The format for storing nodes and ways depends on a Settings parameter and influences which OsmNodeStorage and OsmWayStorage class is used. This is a critical parameter, as memory usage and speed varies.
\li A RelationWriter object then is used to produce the final output. This class could be customized by changing the RelationWriter::writeRelation() function to produce different output formats. It should be noted that the ways can only be accessed sequentially and therefore this class must not try to use random access.
\li SrtmZipFile is just a helper class the does the zip file decompression using zziplib.

Dependency graph: (connection from Settings to Output not shown for clarity)
\dot
  digraph structure {
    srtmserver [label="SRTM server", shape=ellipse]
    SrtmDownloader [URL="\ref SrtmDownloader", shape=box]
    Settings [URL="\ref Settings", shape=box, color=blue]
    Input [color=red]
    Output [color=red]
    OsmData [URL="\ref OsmData", shape=box]
    OsmWayStorage [URL="\ref OsmWayStorage", shape=box]
    OsmNodeStorage [URL="\ref OsmNodeStorage", shape=box]
    RelationWriter [URL="\ref RelationWriter", shape=box]
    SrtmZipFile [URL="\ref SrtmZipFile", shape=box]
    srtmserver -> SrtmZipFile -> SrtmDownloader
    Settings -> srtmserver [dir = none, color=blue]
    Settings -> OsmWayStorage [dir = none, color=blue]
    Settings -> OsmNodeStorage [dir = none, color=blue]
    Settings -> Input [dir = none, color=blue]
#    Settings -> Output [dir = none, color=blue]
    Settings -> SrtmZipFile [dir = none, color=blue]
    OsmNodeStorage -> OsmData [dir = both]
    OsmWayStorage -> OsmData [dir = both]
    Input -> OsmData [color=red]
    RelationWriter -> Output [color=red]
    OsmData -> RelationWriter
    SrtmDownloader -> RelationWriter
 }
\enddot
*/