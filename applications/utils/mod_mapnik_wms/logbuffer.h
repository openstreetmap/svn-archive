/**
 * header file for logbuffer
 *
 * see comments in logbuffer.cpp
 */

#include <stdio.h>
#include <iostream>
#include <fstream>

class logbuffer : public std::streambuf 
{
public:
   logbuffer(FILE*);
   ~logbuffer();

protected:
   virtual int overflow(int c = EOF);
   virtual int sync();

private:
   FILE* fptr;
   std::string buffer;
   void dump_buffer();
};
