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
#ifndef IMGTRE_H
#define IMGTRE_H

#define MAX_LEVEL 16
#define TRUE 1
#define FALSE 0

class MapLevel {
 public:
  int present;
  int last;
  int bits_per_coord;
  int subs;
};

class ImgTreFile : public ImgSubfile {
 public:
  ImgTreFile();
  int ReadFile(std::ifstream &in);
 private:
  double bound_n, bound_e, bound_s, bound_w;
  int ReadLevels(int add, int size);
  int ReadSubs(int add, int size);
  int ReadCopy(int add, int size, int rec_size);
  int ReadPolyL(int add, int size, int rec_size);
  int ReadPolyG(int add, int size, int rec_size);
  int ReadPOI(int add, int size, int rec_size);
  MapLevel Levels[16];
};

#endif
