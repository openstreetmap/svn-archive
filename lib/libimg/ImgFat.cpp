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

ImgFat::ImgFat(){
  filesize = 0;
  filename = "";
  filetype = "";
  nblocks = 0;
}

void ImgFat::CreateHeader(){
  data = new char[GetNumFatBlocks() * 512];
  SetByte(0x0, 1);              //True sub-file
  SetString(0x1, filename);   //File name
  SetString(0x9, filetype);      //sub-type
  SetLong(0xC, filesize);       //filesize
  SetLong(0x10, 0);             //sequence number
  SetByte(0x12, 0, 14);         //padding
  SetBlockList(0x20, firstblock, firstblock+nblocks);
}

int ImgFat::GetNumFatBlocks(){
  return (int)((nblocks/240)+1);
}

int ImgFat::GetNumBlocks(){
  return (nblocks);
}

void ImgFat::SetNumBlocks(int n){
  cout << "Number of blocks set to " << n <<endl;
  nblocks = n;
}

void ImgFat::SetFileSize(int n){
  filesize = n;
  nblocks = n/blocksize + 1;
}

void ImgFat::SetFirstBlock(int n){
  firstblock = n;
}

void ImgFat::SetFileName(char* fn){
  filename.assign(fn);
}

void ImgFat::SetFileType(char* fn){
  filetype.assign(fn);
}

void ImgFat::GetFileName(std::string &fn){
  fn.assign(filename);
}

void ImgFat::GetFileType(std::string &fn){
  fn.assign(filetype);
}

int ImgFat::GetFileSize(){
  return filesize;
}

void ImgFat::WriteFile(std::ofstream &out){
  CreateHeader();
  out.write(data, 512*GetNumFatBlocks());
}

int ImgFat::ReadFile(std::ifstream &in)
{
  int subfile, part;
  filename = "";
  filetype = "";
  in.read(data, blocksize);
  subfile = DecodeByte(0x0);
  DecodeString(0x1, 8, filename);
  DecodeString(0x9, 3, filetype);
  filesize = DecodeLong(0xC);
  DecodeBlockList(0x20, firstblock, nblocks);
  //  firstblock = DecodeShort(0x20);
  cout << "Found fat block for " << filename << "(" << filetype << ")" << endl;
  cout << "Filesize " << filesize << " spanning blocks " << firstblock << " to " << nblocks << endl;
  nblocks -= firstblock;
  return blocksize;
}
