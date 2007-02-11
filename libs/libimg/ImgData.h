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
#ifndef IMGDATA_H
#define IMGDATA_H
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include "time.h"

class ImgData {
 public:
  ImgData();
  virtual void WriteFile(std::ofstream &out);
  virtual int ReadFile(std::ifstream &in) = 0;
  virtual void CreateHeader() = 0;
  tm updatetime;
  void SetUpdateTime(tm timeval);
  void SetUpdateTime(time_t timeval);
    
 protected:
  char xorbyte;
  char *data;
  int blocksize;
  int nblocks;

  //  virtual void CreateHeader();
  void SetByte(int add, int val);
  void SetShort(int add, long val);
  void SetLong(int add, long val);
  void SetInt24(int add, long val);
  void SetByte(int add, int val, int n);
  void SetString(int add, std::string str);
  void SetString(int add, char *str);
  void SetBlockList(int add, int first, int last);
  void SetCoord(int add, double coord);

  int DecodeByte(int add);
  long DecodeShort(int add);
  long DecodeLong(int add);
  long DecodeInt24(int add);
  void DecodeString(int add, int length, std::string &str);
  int DecodeBlockList(int add, int &first, int &last);
  int CoordToMap(double coord);
  double MapToCoord(int map);
  double DecodeCoord(int add);
};

#endif 
