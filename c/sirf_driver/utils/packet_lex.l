%{
#include <stdio.h>
#include "packet_yacc.h"
%}
%%
packet			return PACKET;
\{			return OBRACE;
\}			return CBRACE;
\[			return OBRACKET;
\]			return CBRACKET;
;			return TERMINATOR;
\*			return MULTIPLIER;
\/			return DIVISOR;
=			return EQUALS;
\(			return OPAREN;
\)			return CPAREN;
\|                      return VBAR;
input			return INPUT;
output			return OUTPUT;
bitmask                 return BITMASK;
unsigned		{
			strcpy(yylval.strim,yytext); 
			return MODIFIER;
			}
[0-9][0-9]*		{
			yylval.inum = atoi(yytext);
			return NUMBER;
			}
[0-9]*\.?[0-9]*([eE][\+-][0-9]+)? {
			yylval.fnum = atof(yytext);
			return FLOAT;
			}
[a-zA-Z][a-zA-Z0-9_]*	{
			strcpy(yylval.strim,yytext); 
			return NAME;
			}
\/\*.*\*\/		{
			int n = strlen(yytext) - 4;
			strncpy(yylval.strim,&yytext[2],n);
			yylval.strim[n] = '\0';
			return QUOTED;
			}
\/\/.*\n		/* comments */;
\n			/* ignore eol */;
[ \t]+			/* ignore whitespace */;
%%
