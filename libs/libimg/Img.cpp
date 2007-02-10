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
#include "Parser.h"
#include "Node.h"
#include "Img.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include "time.h"

ImgLblFile::ImgLblFile(){
  Fat.SetFileType("LBL");
  Fat.SetFileName("LBL00001");
}
ImgRgnFile::ImgRgnFile(){
  Fat.SetFileType("RGN");
  Fat.SetFileName("RGN00001");
}

Img::Img(){

}

void Img::WriteFile(std::ofstream &out){
  m_header.SetNumFatBlocks( m_TreFile.Fat.GetNumFatBlocks() + 
			    m_RgnFile.Fat.GetNumFatBlocks() +
			    m_LblFile.Fat.GetNumFatBlocks());
  m_header.WriteFile(out);
  m_TreFile.Fat.WriteFile(out);
  m_RgnFile.Fat.WriteFile(out);
  m_LblFile.Fat.WriteFile(out);
  m_TreFile.WriteFile(out);
  m_RgnFile.WriteFile(out);
  m_LblFile.WriteFile(out);
}

void Img::ReadFile(std::ifstream &in)
{
  ImgFat* fat;
  m_header.ReadFile(in);
  fat = new ImgFat[m_header.GetNumFatBlocks()];
  for(int i=0;i<m_header.GetNumFatBlocks();i++)
    fat[i].ReadFile(in);
  for(int i=0;i<m_header.GetNumFatBlocks();i++)
    {
      if(fat[i].GetFileSize()){
	std::string ft;
	fat[i].GetFileType(ft);
	if(ft=="RGN"){
	  m_RgnFile.Fat.SetFileSize(fat[i].GetFileSize());
	  m_RgnFile.ReadFile(in);
	}
	if(ft=="TRE"){
	  m_TreFile.Fat.SetFileSize(fat[i].GetFileSize());
	  m_TreFile.ReadFile(in);
	}
	if(ft=="LBL"){
	  m_LblFile.Fat.SetFileSize(fat[i].GetFileSize());
	  m_LblFile.ReadFile(in);
	}
      }
    }
}
