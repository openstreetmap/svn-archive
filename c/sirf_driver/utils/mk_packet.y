%{
#include <stdio.h>
#include <string.h>
#include "packet.h"
 
#define YYERROR_VERBOSE

extern FILE *yyin;
FILE *ofh;
typedef enum output_style { Header, Implementation } output_style_t;
output_style_t ostyle;

void yyerror(const char *str) {
  fprintf(stderr,"error: %s\n",str);
  exit(1);
}
 
int yywrap() {
  return 1;
} 

int main(int argc, char *argv[]) {
  if (argc < 3) {
    fprintf(stderr, "USAGE: mk_packet [-c|-h] <output file> <input file>\n");
    exit(-1);
  }
  if (argv[1][0] != '-') {
    fprintf(stderr, "USAGE: mk_packet [-c|-h] <output file> <input file>\n");
    exit(-1);
  }
  switch (argv[1][1]) {
  case 'h':
    ostyle = Header;
    break;
  case 'c':
    ostyle = Implementation;
    break;
  default:
    fprintf(stderr, "USAGE: mk_packet [-c|-h] <output file> <input file>\n");
    exit(-1);
  }

  yyin = fopen(argv[3], "r");
  ofh = fopen(argv[2], "w");

  if (yyin == NULL) {
    fprintf(stderr, "ERROR: failed to open input file: \"%s\"\n", argv[3]);
    exit(-1);
  }
  if (ofh == NULL) {
    fprintf(stderr, "ERROR: failed to open output file: \"%s\"\n", argv[2]);
    exit(-1);
  }

  yyparse();

  return 0;
} 

%}

%union{
	int		inum;
	float		fnum;
	char*		string;
	char		strim[1000];
	struct packet*	pkt;
	struct field*	fld;
	char		chr;
}

%type <pkt> packet packets
%type <fld> field fields pfields
%type <string> name
%type <chr> arithmetic
%type <string> type
%type <string> quoted
%type <inum> pinput ptype

%token PACKET OBRACE CBRACE TERMINATOR OBRACKET CBRACKET
%token DIVISOR MULTIPLIER EQUALS OPAREN CPAREN INPUT OUTPUT
%token <strim> BYTES
%token <strim> NAME
%token <strim> QUOTED
%token <strim> MODIFIER
%token <inum> NUMBER
%token <strim> ARITHMETIC
%token <fnum> FLOAT
%token <chr> AQUOTE

%%
file:	packets
	{
	  if (ostyle == Header) {
	    make_hpp_file($1,ofh);
	  } else {
	    make_cpp_file($1,ofh);
	  }
	}
;

packets: packet { $$ = $1; }
	| packet packets { $1->next = $2; $$ = $1; }
	;

packet: pinput name ptype pfields
{
  struct packet *p = (struct packet *)malloc(sizeof(struct packet));
  if (p == NULL) { fprintf(stderr, "p == NULL!\n"); }
  p->type = $3;
  p->name = $2;
  p->input = $1;
  p->fields = $4;
  $$ = p;
}
;

pinput: INPUT PACKET { $$=1; }
| OUTPUT PACKET { $$=0; }
;

ptype: OPAREN CPAREN { $$=-1; }
| OPAREN NUMBER CPAREN { $$=$2; }
;

pfields: OBRACE fields CBRACE { $$ = $2; }
;

name:	NAME
	{
		char *name = (char *)malloc(sizeof(char) * strlen($1));
		if (name == NULL) { fprintf(stderr, "name == NULL!\n"); }
		strcpy(name, $1);
		$$ = name;
	}
;

type:	name { $$ = $1; }
	| MODIFIER name {
		int nlen = strlen($1) + strlen($2) + 2;
		char *type = (char *)malloc(sizeof(char) * nlen);
		snprintf(type, nlen, "%s %s", $1, $2);
		free($2);
		$$ = type;
	}
;

fields: field { $$ = $1; }
	| field fields { $1->next = $2; $$ = $1; }
;

field:  type name quoted TERMINATOR
		{ $$ = make_field(1, $1, $2, $3); }
	| type name OBRACKET NUMBER CBRACKET quoted TERMINATOR
		{ $$ = make_field($4, $1, $2, $6); }
	| type name EQUALS name arithmetic FLOAT quoted TERMINATOR
		{
		float f;
		struct field *fi;

		switch ($5) {
		case '*': 
			fi = make_field_multiplier(1, $1, $2, $7, $6, $4); 
			break;
		case '/':
			fi = make_field_multiplier(1, $1, $2, $7, (1.0 / $6), $4); 
			break;
		}

		$$ = fi;
		}
;

arithmetic:	MULTIPLIER { $$ = '*'; }
		| DIVISOR { $$ = '/'; }
;

quoted:		QUOTED
	{
		char *quoted = (char *)malloc(sizeof(char) * strlen($1));
		if (quoted == NULL) { fprintf(stderr, "quoted == NULL!\n"); }
		strcpy(quoted, $1);
		$$ = quoted;
	}
;

%%
