
struct bit_collection {
  char *name;
  struct bit_collection *next;
};

struct field {
  struct field *next;
  int count;
  float mult;
  char *record, *name, *description, *convert_to;
  struct bit_collection *bitset;
};

struct packet {
  int type, input;
  char *name;
  struct field *fields;
  struct packet *next;
};

struct field*
make_field_multiplier(int count, char *record, char *name, 
		      char *description, float mult, char *convert_to);

struct field*
make_field(int count, char *record, char *name, char *description);

void make_hpp_file(struct packet *p, FILE *fh);
void make_cpp_file(struct packet *p, FILE *fh);
