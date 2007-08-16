/******************************************************************************
 * Copyright (c) 2007  Marc Kessels
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ******************************************************************************
 */
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "rb.h"
#include "osm.h"
#include "tags.h"
struct rb_table * text_table=NULL;
extern int postgres;


/* File descriptor for .osm file. */
extern FILE *fp;

/* File descriptors for postgres sql files. */
extern FILE *fp_n;
extern FILE *fp_nt;
extern FILE *fp_w;
extern FILE *fp_wn;
extern FILE *fp_wt;

void saveTag(struct tags *p,struct nodes * n){
	if (postgres) 
		fprintf(fp_nt, "%li\t%s\t%s\n", n->ID, p->key, p->value);//fp_nt
	else
		fprintf(fp,"	<tag k=\"%s\" v=\"%s\" />\n",p->key,p->value);
	return;
}

void saveTags(struct tags *p,struct nodes *n){
//	printf("in saveTags %p\n",p);
	if (p!=NULL)
	{
		saveTag(p,n);
		saveTags(p->nextTag,n);
	}
	return;
}

char * addText(char * text)
{
	char * storetext;
	char ** p;
	storetext = (char *) calloc(1,(strlen(text)+1)*sizeof(char));
	if (storetext==NULL)
	{
		fprintf(stderr,"out of memory\n");
		exit(1);
	}
	strcpy(storetext,text);
	p=(char **) rb_probe (text_table, storetext);
	if (*p!=storetext)
	{
		//item was already in list
		free(storetext);
	}
	return *p;
}

struct tags * addtag(struct tags *p,char * tag_key, char * tag_value,struct tags **rv){
	if (p==NULL)
	{
		/*new tag arrived*/
		p = (struct tags *) calloc(1,sizeof(struct tags));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		p->nextTag=NULL;
		p->key=addText(tag_key);
		p->value=addText(tag_value);
		if (rv!=NULL) *rv=p;
	}
	else
		p->nextTag=addtag(p->nextTag, tag_key, tag_value,rv);
	return p;
}


int compare_strings (const void *pa, const void *pb, void *param)
{
	return strcmp (pa, pb);
}


void init_tags()
{
	if (text_table==NULL)
	{
		text_table=rb_create (compare_strings, NULL,NULL);
	}
	else
	{
		printf("error: text_table is inited twice\n");
		exit(1);
	}
	return;
};
