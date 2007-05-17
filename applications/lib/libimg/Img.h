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
#ifndef IMG_H
#define IMG_H

#include "ImgData.h"
#include "ImgHeader.h"
#include "ImgSubfile.h"
#include "ImgFat.h"
#include "ImgTre.h"

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include "time.h"
#include "sys/time.h"

using std::cout;
using std::cerr;
using std::endl;

class ImgLblFile : public ImgSubfile {
 public:
  ImgLblFile();
};

class ImgRgnFile : public ImgSubfile {
 public:
  ImgRgnFile();
};


class Img
{
 public:
  Img();
  void WriteFile(std::ofstream &out);
  void ReadFile(std::ifstream &in);
 private:
  ImgHeader m_header;
  ImgTreFile m_TreFile;
  ImgLblFile m_LblFile;
  ImgRgnFile m_RgnFile;
};

#endif
