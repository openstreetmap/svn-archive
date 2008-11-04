/*
    Copyright (C) 2007 Robert Hart, rob@bathterror.free-online.co.uk

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

ImgHeader::ImgHeader() : ImgData() {
  chksum = 0 ; 
  SetDescription("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
}

void ImgHeader::SetDescription(const char *desc){
  description.assign(desc);
  if (description.length()<50){
    description += std::string (50-description.length(), ' ');
  }
  if (description.length()>50){
    description = description.substr(0, 50);
  }
  description+='\0';
}

void ImgHeader::SetNumFatBlocks(int n){
  nFatBlocks = n;
}

int ImgHeader::GetNumFatBlocks(){
  return nFatBlocks;
}

void ImgHeader::CreateHeader(){
  SetByte(0x00, xorbyte);       //XOR byte
  SetByte(0x01, 0, 9);    //padding
  SetByte(0x0A, updatetime.tm_mon);   //update month
  SetByte(0x0B, (updatetime.tm_year>=100)?updatetime.tm_year-100:updatetime.tm_year); //update year
  //SetByte(0x0B, updatetime.tm_year); //update year
  SetByte(0x0C, 0, 3);    //padding
  SetByte(0x0F, chksum);  //checksum - will need to calculate later
  SetString(0x10, "DSKIMG\0"); // Signature
  SetByte(0x17, 2);       //unknown
  SetShort(0x18,  4);      //sectors
  SetShort(0x1A, 16);      //heads
  SetShort(0x1C, 32);      //cylinders
  SetShort(0x1E,  0);      //unknown
  SetByte(0x20, 0, 25);    //padding
  SetShort(0x39, updatetime.tm_year+1900);    //creation year
  SetByte(0x3B, updatetime.tm_mon);    //creation month
  SetByte(0x3C, updatetime.tm_mday);      //creation day
  SetByte(0x3D, updatetime.tm_hour);     //creation hour
  SetByte(0x3E, updatetime.tm_min);   //creation minute
  SetByte(0x3F, updatetime.tm_sec);   //creation second
  SetByte(0x40, 2);        //Unknown
  SetString(0x41, IDENT);  //Map file identifier
  SetByte(0x48, 0);        //Padding?
  SetString(0x49, description.substr(0, 20));
  SetShort(0x5D, 16);      //heads?
  SetShort(0x5F, 4);       //sectors?
  SetByte(0x61, 9);        //block size E1
  SetByte(0x62, 0);        //block size E2 (force block size to 512)
  SetShort(0x63, 2048);        // (heads*sectors*cylinders)/2^(E2) 
  SetString(0x65, description.substr(20, 51));    //should be rest of description
  SetByte(0x84, 0, 314);     // loads of padding!
  //Partition table
  SetByte(0x1BE, 0);       //boot?
  SetByte(0x1BF, 0);       //start-head?
  SetByte(0x1C0, 1);       //start-sector?
  SetByte(0x1C1, 0);       //start-cylinder?
  SetByte(0x1C2, 0);       //system-type?
  SetByte(0x1C3, 15);      //end-head?
  SetByte(0x1C4, 4);       //end-sector?
  SetByte(0x1C5, 31);      //end-cylinder?
  SetLong(0x1C6, 0);       //rel-cylinder?
  SetLong(0x1CA, 2048);// number of sectors?
  SetByte(0x1CE, 0, 48);   //padding?
  SetShort(0x1FE, 0x55);
  SetShort(0x1FF, 0xAA);

  SetByte(0x200, 0, 512);  //padding?
  SetByte(0x400, 1);       //?
  SetByte(0x401, 0x20, 11);//?
  SetLong(0x40C, blocksize*(nblocks+nFatBlocks)); //first file offset
  SetByte(0x410, 3);       //?
  SetByte(0x411, 0, 15);   //?
  SetBlockList(0x420, 0, nblocks+nFatBlocks-1); //write out block count list

}

int ImgHeader::ReadFile(std::ifstream &in)
{
  int first;
  blocksize = 512;
  nblocks = 3;
  xorbyte = 0;
  in.read(data, blocksize * nblocks);

  xorbyte           = DecodeByte(0x00);
  cout << "xorbyte :" << (int)xorbyte << endl;
  chksum            = DecodeByte(0x0F);
  cout << "chksum :" << chksum << endl;

  updatetime.tm_year= DecodeShort(0x39)-1900;
  updatetime.tm_mon = DecodeByte(0x3B);
  updatetime.tm_mday= DecodeByte(0x3C);
  updatetime.tm_hour= DecodeByte(0x3D);
  updatetime.tm_min = DecodeByte(0x3E);
  updatetime.tm_sec = DecodeByte(0x3F);
  cout << "update time is :" << asctime(&updatetime) << endl;
  blocksize = 1<<   ( DecodeByte(0x61) +
		      DecodeByte(0x62));

  cout << "blocksize :" << blocksize << endl;

  description.assign("");
  DecodeString(0x49,20, description);
  DecodeString(0x65,31, description);
  DecodeBlockList(0x420, first, nFatBlocks);
  cout << "header spans " << first << " to " << nFatBlocks << endl;
  nFatBlocks-=nblocks-1;
  cout << "description :" << description << endl;
  return nblocks * blocksize;
}
