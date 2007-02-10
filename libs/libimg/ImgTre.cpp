/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#include "Img.h"
#include "ImgTre.h"

ImgTreFile::ImgTreFile()
{
  Fat.SetFileType("TRE");
  Fat.SetFileName("TRE00001");
}

int ImgTreFile::ReadFile(std::ifstream &in)
{
  int oLevel, oSub, oCopy, oPolyL, oPolyG, oPOI;
  int sLevel, sSub, sCopy, sPolyL, sPolyG, sPOI;
  int rsCopy, rsPolyL, rsPolyG, rsPOI, poiflags;

  int headerlen = ImgSubfile::ReadFile(in);
  bound_n = DecodeCoord(0x15);
  bound_e = DecodeCoord(0x18);
  bound_s = DecodeCoord(0x1B);
  bound_w = DecodeCoord(0x1E);
  cout << "Tre BB: (" << bound_s << "," << bound_e << ")-("
       << bound_n << "," << bound_w << ")" << endl;
  oLevel = DecodeLong(0x21);
  sLevel = DecodeLong(0x25);
  oSub   = DecodeLong(0x29);
  sSub   = DecodeLong(0x2D);
  oCopy   = DecodeLong(0x31);
  sCopy   = DecodeLong(0x35);
  rsCopy  = DecodeShort(0x39);
  poiflags= DecodeByte(0x3F);
  oPolyL   = DecodeLong(0x4A);
  sPolyL   = DecodeLong(0x4E);
  rsPolyL  = DecodeShort(0x52);
  oPolyG   = DecodeLong(0x58);
  sPolyG   = DecodeLong(0x5C);
  rsPolyG  = DecodeShort(0x60);
  oPOI   = DecodeLong(0x66);
  sPOI   = DecodeLong(0x6A);
  rsPOI  = DecodeShort(0x6E);
  cout << "Levels: off = " << oLevel << " len = " << sLevel << endl;
  ReadLevels(oLevel, sLevel);

  cout << "Subs  : off = " << oSub  << " len = " << sSub << endl;
  ReadSubs(oSub, sSub);

  cout << "Copy  : off = " << oCopy << " len = " << sCopy << endl;
  ReadCopy(oCopy, sCopy, rsCopy);

  cout << "PolyL  : off = " << oPolyL << " len = " << sPolyL << " rs = " << rsPolyL << endl;
  ReadCopy(oPolyL, sPolyL, rsPolyL);

  cout << "PolyG  : off = " << oPolyG << " len = " << sPolyG << " rs = " << rsPolyG << endl;
  ReadCopy(oPolyG, sPolyG, rsPolyG);

  cout << "POI  : off = " << oPOI << " len = " << sPOI << " rs = " << rsPOI << endl;
  ReadCopy(oPOI, sPOI, rsPOI);
  return headerlen;
}

int ImgTreFile::ReadLevels(int add, int size)
{
  int i;
  int zoom, inherited;
  for(i=0;i<16;i++){
    Levels[i].present=FALSE;
    Levels[i].last=FALSE;
  }
  for(i=0;i<(size>>2);i++){
    zoom = DecodeByte(add+i*4+0);
    inherited = zoom>>7;
    zoom &= 0xF;
    Levels[zoom].present=TRUE;
    Levels[zoom].bits_per_coord = DecodeByte(add+i*4+1);
    Levels[zoom].subs = DecodeByte(add+i*4+2);
    cout << (zoom&0xF) << " = " << Levels[zoom].bits_per_coord << " bits per coord and " << Levels[zoom].subs << " subdivisions" << endl;
  }
  Levels[zoom].last=TRUE;
  return size;
}

int ImgTreFile::ReadSubs(int add, int size)
{
  int i, j, off, c, next;
  int oRGN, objType, width, height, term;
  double origin_Lon, origin_Lat;
  off = add;
  c = 1; 
  for(i=MAX_LEVEL-1;i>=0;i--){
    if(Levels[i].present){
      cout << "Level: " << i << endl;
      for(j=0;j<Levels[i].subs;j++){
	cout << "Sub: " << c; 
	oRGN = DecodeInt24(off + 0);
	objType = DecodeByte(off + 3);
	origin_Lon = DecodeCoord(off + 4);
	origin_Lat = DecodeCoord(off + 7);
	width = DecodeShort(off + 10);
	term = width >> 15;
	width -= term << 15;
	height = DecodeShort(off + 12);
	//width = width << Levels[i].bits_per_coord;
	//height = height << Levels[i].bits_per_coord;
	cout << ", " << origin_Lon << ", " << origin_Lat;
	cout << ", " << MapToCoord(width*2+1);
	cout << ", " << MapToCoord(height*2+1);
	if(Levels[i].last){
	  off+=14;
	} else {
	  next = DecodeShort(off + 14);
	  off+=16;
	  cout << ", " << next;
	}
	c++;
	cout << endl;
      }
    }
  }
  return size;
}

int ImgTreFile::ReadCopy(int add, int size, int rec_size)
{
  return size;
}
int ImgTreFile::ReadPolyL(int add, int size, int rec_size)
{
  return size;
}
int ImgTreFile::ReadPolyG(int add, int size, int rec_size)
{
  return size;
}
int ImgTreFile::ReadPOI(int add, int size, int rec_size)
{
  return size;
}
