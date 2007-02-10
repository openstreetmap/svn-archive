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
#ifndef IMGSUBFILE_H
#define IMGSUBFILE_H
#include "Img.h"
#include "ImgFat.h"

class ImgSubfile : public ImgData {
 public:
  ImgSubfile();
  void CreateHeader();
  int ReadFile(std::ifstream &in);
  ImgFat Fat;

 protected:
  std::string subtype;
  
 private:
  int headerlength;
  std::string type;
  
};

#endif
