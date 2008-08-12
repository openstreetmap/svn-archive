/* This software is placed by in the public domain by its authors. */
/* Written by Nic Roets. */

#include <stdio.h>
#include <unistd.h>
#include <libxml/xmlreader.h>

int main (void)
{
  xmlTextReaderPtr xml = xmlReaderForFd (STDIN_FILENO, "", NULL, 0);
  while (xmlTextReaderRead (xml)) {
    char *name = (char *) BAD_CAST xmlTextReaderName (xml);
    if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_ELEMENT &&
         strcasecmp (name, "text") == 0) {
      while (xmlTextReaderRead (xml) && // memory leak :
              xmlStrcmp (xmlTextReaderName (xml), BAD_CAST "#text") != 0) {}
      printf ("%s\n", xmlTextReaderValue (xml));
    }
  }
}
