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

ImgSubfile::ImgSubfile(){
  subtype = "???";
}

void ImgSubfile::CreateHeader(){
  cout << "ImgSubfile::CreateHeader() called\n";
}

int ImgSubfile::ReadFile(std::ifstream &in){
  int headerlen, locked;
  std::string typedesc("");

  if(data) delete data;
  nblocks = Fat.GetNumBlocks();
  data = new char[nblocks * blocksize];
  in.read(data, nblocks * blocksize);
  cout << "reading " << nblocks << " blocks (" << nblocks * blocksize << " bytes)\n";
  headerlen = DecodeShort(0x0);
  DecodeString(0x2, 10, typedesc);
  locked = DecodeByte(0xD);
  updatetime.tm_year= DecodeShort(0xE)-1900;
  updatetime.tm_mon = DecodeByte(0x10);
  updatetime.tm_mday= DecodeByte(0x11);
  updatetime.tm_hour= DecodeByte(0x12);
  updatetime.tm_min = DecodeByte(0x13);
  updatetime.tm_sec = DecodeByte(0x14);
  cout << "Reading file of type : " << typedesc << endl;
  cout << "update time :" << asctime(&updatetime) << endl;
  cout << "headerlen :" << headerlen << endl;
  cout << "locked :" << locked << endl;
  return headerlen;
}

