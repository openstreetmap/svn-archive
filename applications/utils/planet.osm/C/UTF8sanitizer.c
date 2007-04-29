/*
 * UTF8sanitizer.c - filter out invalid UTF8 bytesequences
 *     
 * Copyright (C) 2006 Jonas Svensson (jonass@lysator.liu.se)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

/* To compile:
   gcc -O2 -o UTF8sanitizer UTF8sanitizer.c

   Usage (assuming bash):
   UTF8sanitizer <sourcefile >destfile 2>errors.txt
*/

#include <stdio.h>

int main(int argc, char** argv) {
  long long line;
  long long chars1, chars2, chars3, chars4, chars5, chars6;
  int state, current_size;
  int current_char, long_char[6];
  int i;

  chars1=chars2=chars3=chars4=chars5=chars6=0;
  line = 0;
  state = 1;
  current_size=0;
  current_char=getchar();
  while (!feof(stdin)) {
    if ((current_char & 128) == 0) {
      //Handle_ASCII_char();
      if (current_char == '\n') 
	line++;
      else
	chars1++;
      if (state != 1) {
	fprintf(stderr, "Error at line %lld\n", line);
	putchar('_');
	state = 1;
      }
      putchar(current_char);
    } else if ((current_char & (128+64)) == 128) {
      // Handle_continue_char();
      if(state > 1) {
	state--;
	if(state==1) {
	  // long char finished
	  for(i=1; i<current_size; i++) {
	    putchar(long_char[i-1]);
	  }
	  putchar(current_char);
	}
      } else {
	fprintf(stderr, "Error at line %lld\n", line);
	putchar('_');
	state=1;
      }
    } else if ((current_char & (128+64+32)) == (128+64)) {
      //Handle_two_bytes();
      state=2;
      chars2++;
      current_size=2;
    } else if ((current_char & (128+64+32+16)) == (128+64+32)) {
      //Handle_three_bytes();
      state=3;
      chars3++;
      current_size=3;
    } else if ((current_char & (128+64+32+16+8)) == (128+64+32+16)) {
      //Handle_four_bytes();
      state=4;
      chars4++;
      current_size=4;
    } else if ((current_char & (128+64+32+16+8+4)) == (128+64+32+16+8)) {
      //Handle_five_bytes();
      state=5;
      chars5++;
      current_size=5;
    } else if ((current_char & (128+64+32+16+8+4+2)) == (128+64+32+16+8+4)) {
      //Handle_six_bytes();
      state=6;
      chars6++;
      current_size=6;
    }
    if(state>1) {
      long_char[current_size-state]=current_char;
    }
    current_char=getchar();
  }

  fprintf(stderr, "Summary:\n");
  fprintf(stderr, "chars1: %lld\n", chars1);
  fprintf(stderr, "chars2: %lld\n", chars2);
  fprintf(stderr, "chars3: %lld\n", chars3);
  fprintf(stderr, "chars4: %lld\n", chars4);
  fprintf(stderr, "chars5: %lld\n", chars5);
  fprintf(stderr, "chars6: %lld\n", chars6);
  fprintf(stderr, "lines : %lld\n", line);

  return 0;
}
