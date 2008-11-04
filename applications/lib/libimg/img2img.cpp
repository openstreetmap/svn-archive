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
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <cstdlib>
#include "time.h"

using std::cout;
using std::cout;
using std::endl;

int main(int argc,char* argv[])
{
	if(argc<3)
	{
		cout<<"Usage: img2img InImgFile OutImgFile" << endl;
		exit(1);
	}

	Img ImgFile;
	std::ifstream in(argv[1]);
	std::ofstream out(argv[2]);
	cout << "Reading file " << argv[1] << endl;
	ImgFile.ReadFile(in);
	cout << "Done\n";
	cout << "Writing file " << argv[2] << endl;
	ImgFile.WriteFile(out);
	in.close();
	out.close();
	return 0;
}
