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
#ifndef IMGFAT_H
#define IMGFAT_H
#include "Img.h"

class ImgFat : public ImgData {
 public:
  ImgFat();
  void WriteFile(std::ofstream &out);
  int ReadFile(std::ifstream &in);
  void CreateHeader();
  void SetFirstBlock(int n);
  void SetNumBlocks(int n);
  void SetFileSize(int n);
  void SetFileName(char* fn);
  void SetFileType(char* fn);
  int GetNumBlocks();
  int GetFirstBlock();
  int GetNumFatBlocks();
  int GetFileSize();
  void GetFileName(std::string &fn);
  void GetFileType(std::string &fn);

 protected:
  int firstblock;
  int filesize;
  std::string filename;
  std::string filetype;
  
 private:
  int headerlength;
};

#endif
