/*
Copyright (C) Stephen Coast (steve@fractalus.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

import java.util.*;
import java.lang.*;
import javax.swing.*;
import java.awt.*;



public class osmApplet extends JApplet {
    
    public void init() {


      new osmDisplay( getContentPane() );       

        
        
        
    } // init

    
    public static void main(String[] args)
    {

      new osmApplet().go();

    } // main

    public osmApplet()
    {

    } // osmApplet2

    public void go()
    {
      JFrame jf = new JFrame("blah");
      
      jf.setSize(600,600);
      jf.show();
      
      new osmDisplay(jf.getContentPane());
 
      jf.pack();
      
    } 
}
