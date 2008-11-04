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
#ifndef IMGHEADER_H
#define IMGHEADER_H
#include "Img.h"

class ImgHeader : public ImgData 
{
 public:
  ImgHeader();
  void CreateHeader();
  void SetNumFatBlocks(int n);
  int GetNumFatBlocks();
  int ReadFile(std::ifstream &in);

 private:
  int chksum;
  int nFatBlocks;
#define IDENT "GARMIN\0"
  std::string description;
  void SetDescription(const char *desc);
};

#endif
