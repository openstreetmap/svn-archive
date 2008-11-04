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

#include <cstring>

ImgData::ImgData(){
  timeval tv;
  int retval;

  blocksize = 512;
  nblocks = 3;
  data = new char[nblocks * blocksize];
  xorbyte = 0; 
  retval = gettimeofday(&tv, NULL);
  SetUpdateTime(tv.tv_sec);

}

void ImgData::SetUpdateTime(tm timeval){
  updatetime = timeval;
}

void ImgData::SetUpdateTime(time_t timeval){
  gmtime_r(&timeval, &updatetime);
}

void ImgData::WriteFile(std::ofstream &out){
  CreateHeader();
  out.write(data,blocksize * nblocks);
}


void ImgData::SetByte(int add, int val){
  data[add] = (char)val^xorbyte;
}

int ImgData::DecodeByte(int add)
{
  return data[add]^xorbyte;
}

void ImgData::SetByte(int add, int val, int n){
  for(int i=0;i<n;i++){
    data[add+i] = (char)val^xorbyte;
  }
}

void ImgData::SetShort(int add, long val){
  data[add] = (char)val^xorbyte;
  data[add+1] = (char)(val >> 8)^xorbyte;
}

long ImgData::DecodeShort(int add){
  long v1, v2;
  v1 = ((unsigned char)(data[add+1])^xorbyte)<<8;
  v2 = (long)((unsigned char)(data[add])^xorbyte);
  return v1 + v2;
}

void ImgData::SetLong(int add, long val){
  data[add] = (char)val^xorbyte;
  data[add+1] = (char)(val >> 8)^xorbyte;
  data[add+2] = (char)(val >> 16)^xorbyte;
  data[add+3] = (char)(val >> 24)^xorbyte;
}

void ImgData::SetInt24(int add, long val){
  data[add] = (char)val^xorbyte;
  data[add+1] = (char)(val >> 8)^xorbyte;
  data[add+2] = (char)(val >> 16)^xorbyte;
}

long ImgData::DecodeLong(int add)
{
  long v1, v2, v3, v4;
  v4 = ((unsigned char)(data[add+3])^xorbyte)<<24;
  v3 = ((unsigned char)(data[add+2])^xorbyte)<<16;
  v2 = ((unsigned char)(data[add+1])^xorbyte)<<8;
  v1 = (long)((unsigned char)(data[add])^xorbyte);
  return v1 + v2 + v3 + v4;
}

long ImgData::DecodeInt24(int add)
{
  long v1, v2, v3;
  v3 = ((unsigned char)(data[add+2])^xorbyte)<<16;
  v2 = ((unsigned char)(data[add+1])^xorbyte)<<8;
  v1 = (long)((unsigned char)(data[add])^xorbyte);
  return v1 + v2 + v3;
}

void ImgData::SetString(int add, std::string str){
  //  cout << "SetString: " << str << endl;
  //  cout << "  is " << str.length() << "bytes long\n";
  for(int i=0;i<str.length();i++){
    data[add+i]=(char)str.at(i)^xorbyte;
  }
}

void ImgData::SetString(int add, char* str){
  for(int i=0;i<strlen(str);i++){
    data[add+i]=(char)str[i]^xorbyte;
  }
}

void ImgData::DecodeString(int add, int length, std::string &decoded){
  for(int i=0;i<length;i++) {
    char c[2] = { data[add+i]^xorbyte, 0 };
    //    cout << "appending " << c << endl;
    decoded.append( c );
  }
}

void ImgData::SetBlockList(int add, int first, int last){
  int i;
  for(i=0; i<(last-first); i++){
    SetShort(add+2*i, first+i);
  }
  for(; i<240; i++){
    SetShort(add+2*i, 65535);
  }
}

int ImgData::DecodeBlockList(int add, int &first, int &last){
  int i, j, k;
  first = j = DecodeShort(add);
  cout << j;
  for(i=1; i<240; i++){
    k = DecodeShort(add+2*i);
    //   cout << ", " << k;
    if(k!=j+1)
      break;
    j=k;
  }
  //cout << endl;
  return last = j;
}

//FIXME: need to deal with negative values.
double ImgData::DecodeCoord(int add)
{
    long v1, v2, v3;
  v3 = ((unsigned char)(data[add+2])^xorbyte)<<16;
  v2 = ((unsigned char)(data[add+1])^xorbyte)<<8;
  v1 = (long)((unsigned char)(data[add])^xorbyte);
  return MapToCoord(v1 + v2 + v3);
}

void ImgData::SetCoord(int add, double coord)
{
  long val = CoordToMap(coord);
  data[add] = (char)val^xorbyte;
  data[add+1] = (char)(val >> 8)^xorbyte;
  data[add+2] = (char)(val >> 16)^xorbyte;
}

int ImgData::CoordToMap(double coord)
{
  return (int)(coord/360.0*(1<<24));
}

double ImgData::MapToCoord(int map)
{
  return (double)map*360.0/(1<<24);
}
