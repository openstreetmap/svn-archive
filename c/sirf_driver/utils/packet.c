#include <stdio.h>
#include "packet.h"

struct field *make_field_multiplier(int count, char *record, char *name, 
		char *description, float mult, char *convert_to) {  
	struct field *f = (struct field *)malloc(sizeof(struct field));
	if (f == NULL) { fprintf(stderr, "f == NULL!\n"); }
	
	f->next = NULL;
	f->count = count;
	f->record = record;
	f->name = name;
	f->mult = mult;
	if (convert_to == NULL) {
		f->convert_to = NULL;
	} else {
		f->convert_to = 
			(char *)malloc(sizeof(char) * strlen(convert_to));
		strcpy(f->convert_to, convert_to);
	}
	f->description = description;
	
	return f;
}

struct field *make_field(int count, char *record, 
		char *name, char *description) {  
	return make_field_multiplier(count, record, name, 
				description, 1.0, NULL);
}

void munge_name(char *dname, char *name) {
  int i, j;

  for (i = 0, j = 0; i < strlen(name); i++, j++) {
    if ((name[i] >= 'a') && (name[i] <= 'z')) {
      dname[j] = name[i] + 'A' - 'a';
    } else if ((name[i] >= 'A') && (name[i] <= 'Z') && (i > 0)) {
      dname[j++] = '_';
      dname[j] = name[i];
    } else {
      dname[j] = name[i];
    }
  }
  // add the _H
  dname[j++] = '_';
  dname[j++] = 'H';
  dname[j++] = '\0';
}

void make_hpp_file(struct packet *p, FILE *fh) {
  struct packet *pptr;
  struct field *fptr;

  for (pptr = p; pptr != NULL; pptr = pptr->next) {
    // open a file for the name
    char define_name[256];

    // munge the packet name
    munge_name(define_name, pptr->name);
    // write out the header
    fprintf(fh, "#ifndef %s\n", define_name);
    fprintf(fh, "#define %s\n\n", define_name);
    fprintf(fh, "#include <Types.hpp>\n");
    if (pptr->input == 1) {
      fprintf(fh, "#include <InputPacket.hpp>\n");
    } else {
      fprintf(fh, "#include <OutputPacket.hpp>\n");
    }
    fprintf(fh, "#include <Stream.hpp>\n");
    for (fptr = pptr->fields; fptr != NULL; fptr = fptr->next) {
      // include any other header files
      if ( (strcmp(fptr->record, "int8") != 0) &&
	   (strcmp(fptr->record, "int16") != 0) &&
	   (strcmp(fptr->record, "int32") != 0) &&
	   (strcmp(fptr->record, "int64") != 0) &&
	   (strcmp(fptr->record, "uint8") != 0) &&
	   (strcmp(fptr->record, "uint16") != 0) &&
	   (strcmp(fptr->record, "uint32") != 0) &&
	   (strcmp(fptr->record, "uint64") != 0) &&
	   (strcmp(fptr->record, "float") != 0) ) {
	// new type
	fprintf(fh, "#include \"%s.hpp\"\n", fptr->record);
      }
    }
    fprintf(fh, "\nnamespace SiRF {\n\n", define_name);
    // now class stuff
    if (pptr->input == 1) {
      fprintf(fh, " class %s : public InputPacket {\n\n", pptr->name);
    } else {
      fprintf(fh, " class %s : public OutputPacket {\n\n", pptr->name);
    }
    fprintf(fh, " public:\n\n");
    if (pptr->type >= 0) {
      fprintf(fh, "  static const unsigned char type = 0x%02x;\n\n",
	      pptr->type);
      fprintf(fh, "  unsigned char getType() { return 0x%02x; }\n\n",
	      pptr->type);
      fprintf(fh, "  bool isInput() { return %s; }\n\n",
	      (pptr->input == 1) ? "true" : "false");
    } else {
      fprintf(fh, "  unsigned char getType() { return 0; }\n\n");
      fprintf(fh, "  bool isInput() { return %s; }\n\n",
	      (pptr->input == 1) ? "true" : "false");
    }
    // inserters
    if (pptr->input != 1) {
      fprintf(fh, "  void input(Stream &in);\n");
    } else {
      // extractors
      fprintf(fh, "  friend Stream &operator<<(Stream &out, const %s &p);\n",
	      pptr->name);
    }
    fprintf(fh, "  void output(std::ostream &out) const;\n\n");
    // now the accessor methods
    for (fptr = pptr->fields; fptr != NULL; fptr = fptr->next) {
      if (strcmp(fptr->name,"Reserved") != 0) {
	if (fptr->convert_to == NULL) {
	  if (fptr->count == 1) {
	    fprintf(fh, "  inline %s get%s() const { return m_%s; }\n", 
		    fptr->record, fptr->name, fptr->name);
	  } else {
	    fprintf(fh, "  inline %s get%s(int i) const { return m_%s[i]; }\n", 
		    fptr->record, fptr->name, fptr->name);
	    fprintf(fh, "  inline const %s *get%sArray() const {\n",
		    fptr->record, fptr->name);
	    fprintf(fh, "   return m_%s;\n  }\n", fptr->name);
	  }
	} else {
	  if (fptr->count == 1) {
	    fprintf(fh, "  inline %s get%s() {\n",
		    fptr->convert_to, fptr->name);
	    fprintf(fh, "    return ((%s)m_%s * %f);\n  }\n",
		    fptr->convert_to, fptr->name, fptr->mult);
	  } else {
	    fprintf(fh, "  inline %s get%s(int i) {\n",
		    fptr->convert_to, fptr->name);
	    fprintf(fh, "    return ((%s)m_%s * %f[i]);\n  }\n",
		    fptr->convert_to, fptr->name, fptr->mult);
	  }
	}
      }
    }
    // private stuff
    fprintf(fh, "\n private:\n\n");
    for (fptr = pptr->fields; fptr != NULL; fptr = fptr->next) {
      if (strcmp(fptr->name,"Reserved") != 0) {
	fprintf(fh, "  /* %s */\n", fptr->description);
	if (fptr->count == 1) {
	  fprintf(fh, "  %s m_%s;\n", fptr->record, fptr->name);
	} else {
	  fprintf(fh, "  %s m_%s[%d];\n", fptr->record,
		  fptr->name, fptr->count);
	}
      }
    }
    fprintf(fh, "\n };\n");
    // finish off
    fprintf(fh, "\n}\n", define_name);
    // end of header
    fprintf(fh, "#endif /* %s */\n", define_name);
    // close the file
    fclose(fh);
  }
}


void make_cpp_file(struct packet *p, FILE *fh) {
  struct packet *pptr;
  struct field *fptr;

  for (pptr = p; pptr != NULL; pptr = pptr->next) {
    // open a file for the name
    char define_name[256];

    // print out implementation file
    // headers
    fprintf(fh, "#include <%s.hpp>\n", pptr->name);
    fprintf(fh, "\nnamespace SiRF {\n\n");
    if (pptr->input == 1) { 
      // implementation of inserter
      fprintf(fh, " Stream &operator<<(Stream &out, const %s &p) {\n",
	      pptr->name);
      for (fptr = pptr->fields; fptr != NULL; fptr = fptr->next) {
	if (strcmp(fptr->name,"Reserved") != 0) {
	  if (fptr->count == 1) {
	    fprintf(fh, "  out << (int)m_%s;\n", fptr->name);
	  } else {
	    fprintf(fh, "  for (int i = 0; i < %d; i++) {\n",
		    fptr->count);
	    fprintf(fh, "   out << (int)m_%s[i];\n", fptr->name);
	    fprintf(fh, "  }\n");
	  }
	} else {
	  fprintf(fh, "  {\n   %s Reserved = 0;\n", fptr->record);
	  if (fptr->count == 1) {
	    fprintf(fh, "   out << (int) Reserved;\n");
	  } else {
	    fprintf(fh, "   for (int i = 0; i < %d; i++) {\n", fptr->count);
	    fprintf(fh, "    out << (int) Reserved;\n   }\n");
	  }
	  fprintf(fh, "  }\n");
	}
      }
      fprintf(fh, "  return out;\n }\n\n");
    } else {
      // implementation of extractor
      fprintf(fh, " void %s::input(Stream &in) {\n",
	      pptr->name);
      for (fptr = pptr->fields; fptr != NULL; fptr = fptr->next) {
	if (strcmp(fptr->name,"Reserved") != 0) {
	  if (fptr->count == 1) {
	    fprintf(fh, "  in >> m_%s;\n", fptr->name);
	  } else {
	    fprintf(fh, "  for (int i = 0; i < %d; i++) {\n",
		    fptr->count);
	    fprintf(fh, "   in >> m_%s[i];\n", fptr->name);
	    fprintf(fh, "  }\n");
	  }
	} else {
	  fprintf(fh, "  {\n   %s Reserved;\n", fptr->record);
	  if (fptr->count == 1) {
	    fprintf(fh, "   in >> Reserved;\n");
	  } else {
	    fprintf(fh, "   for (int i = 0; i < %d; i++) {\n", fptr->count);
	    fprintf(fh, "    in >> Reserved;\n   }\n");
	  }
	  fprintf(fh, "  }\n");
	}
      }
      fprintf(fh, " }\n\n");
    }
    // implementation of insertor for debugging
    fprintf(fh, 
	    " void %s::output(std::ostream &out) const {\n",
	    pptr->name);
    for (fptr = pptr->fields; fptr != NULL; fptr = fptr->next) {
      if (strcmp(fptr->name,"Reserved") != 0) {
	if (fptr->count == 1) {
	  fprintf(fh, "  out << \"%s:\\t\" << (int)m_%s << \" %s\" << std::endl;\n", 
		  fptr->name, fptr->name, fptr->description);
	} else {
	  fprintf(fh, "  out << \"%s: %s\" << std::endl;\n",
		  fptr->name, fptr->description); 
	  fprintf(fh, "  for (int i = 0; i < %d; i++) {\n",
		  fptr->count);
	  fprintf(fh, "   out << i << \":\\t\" << m_%s[i] << std::endl;\n",
		  fptr->name);
	  fprintf(fh, "  }\n");
	}
      }
    }
    fprintf(fh, " }\n\n");

    if (pptr->type >= 0) {
      fprintf(fh, "  const unsigned char %s::type;\n\n",
	      pptr->name, pptr->type);
    }

    // finish off
    fprintf(fh, "}\n");
    fclose(fh);
  }
}

