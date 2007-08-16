#include <stdio.h>



void saveTags(struct tags *p,struct nodes *n);
struct tags * addtag(struct tags *p,char * tag_key, char * tag_value,struct tags **rv);
void init_tags();
