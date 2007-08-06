#include <stdio.h>


struct tags{
	char * key;  /*stored in text b-tree to save memory*/
	char * value;  /*stored in text b-tree to save memory*/
	struct tags* nextTag;
};

void saveTags(FILE * fp, struct tags *p);
struct tags * addtag(struct tags *p,char * tag_key, char * tag_value,struct tags **rv);
void init_tags();
