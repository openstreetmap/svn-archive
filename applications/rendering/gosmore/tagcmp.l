%option noyywrap case-insensitive
%option align
/* 'align' will hopefully improve ARM performance, but does it affect x86 performance ? */
        int cmpStart, cmpCnt, num;
%{
#define YY_USER_ACTION cmpCnt += yyleng;
/* This action is described in the FAQ contained in the flex info page */
%}

%s middle

WE	[\0 \n\t,]
/* Word end */

 /* The rules. 
    If we find a meaningless prefix, we set cmpStart.
    If we find a meaningless suffix, we reduce cmpCnt, so that it's excluded and we return.
 */
 /* Rules for English. */
%%
<INITIAL>the" "								cmpStart = cmpCnt; cmpCnt = 0;
<middle>" "(ave|avenue|blvd|boulevard|byp|bypass){WE}			cmpCnt -= yyleng; return num;
<middle>" "(cir|circle|close|cres|crescent|ct|court|ctr|center){WE}	cmpCnt -= yyleng; return num;
<middle>" "(dr|drive|hwy|highway|ln|lane|loop){WE}			cmpCnt -= yyleng; return num;
<middle>" "(pass|pky|parkway|pl|place|plz|plaza){WE}			cmpCnt -= yyleng; return num;
<middle>" "(rd|road|sq|square|st|street){WE}				cmpCnt -= yyleng; return num;
<middle>" "(ter|terrace|tpke|turnpike|walk|way){WE}			cmpCnt -= yyleng; return num;
twenty[ -]	num = 20;
thirty[ -]	num = 30;
fourty[ -]	num = 40;
fifty[ -]	num = 50;
sixty[ -]	num = 60;
seventy[ -]	num = 70;
eighty[ -]	num = 80;
ninety[ -]	num = 90;
first		return num + 1;
second		return num + 2;
third		return num + 3;
fourth		return num + 4;
fifth		return num + 5;
sixth		return num + 6;
seventh		return num + 7;
eighth		return num + 8;
nineth		return num + 9;
tenth		return 10;
eleventh	return 11;
twelth		return 12;
thirteenth	return 13;
fourteenth	return 14;
fifthteenth	return 15;
sixteenth	return 16;
seventeenth	return 17;
eighteenth	return 18;
nineteenth	return 19;
twentieth	return 20;

 /* Rules for French. */
<INITIAL>(avenue|boulevard|rue)" "(de" "(la" ")?)?			cmpStart = cmpCnt; cmpCnt = 0;

 /* Rules for Italian. */
<INITIAL>via" "								cmpStart = cmpCnt; cmpCnt = 0;

 /* Rules for German. Normally "strasse" will not be a separate word, but my guess is that it's
    better to accomodate that variation. */
<middle>" "?(strasse|platz|straße){WE}					cmpCnt -= yyleng; return num;

 /* Rules for Dutch. Same guess regarding 2 words variations. */
<middle>" "?(straat|weg|baan|kade|steeg|gracht|veer){WE}		cmpCnt -= yyleng; return num;

 /* American/TIGER street names often start with an N, an E, a W or an S. Ignore it. */
<INITIAL>[news]" "							cmpStart = cmpCnt; cmpCnt = 0;

[0-9]+		return atoi (yytext);
[\0\n,]		return num;
<INITIAL>.	BEGIN (middle);
<middle>.	;

%%

main()
{
        //yyscan_t scanner;
        YY_BUFFER_STATE buf;
        //yylex_init( &scanner );
        char a[100], b[100];
        int acmpStart, acmpCnt, anum, i;
        while (gets (a)) {
        // for (i = 0; i < 2; i++) { // for 'a' and 'b'
          acmpStart = cmpStart;
          acmpCnt = cmpCnt;
          anum = num;
          cmpStart = 0;
          cmpCnt = 0;
          num = 0;
          buf = yy_scan_bytes (i ? b : a, 100);
          /* yy_scan_string() does not process the trainling '\0', which makes it
             impossible?? to match full words. */
          
          num = yylex();
          printf ("%d '%.*s'\n", num, cmpCnt - cmpStart, a + cmpStart);
          BEGIN (INITIAL);
          
          yy_delete_buffer(buf); //,scanner);
        }
        //yylex_destroy( scanner );
        // return num != anum ? num - anum : strncasecmp (a + acmpStart, b + cmpStart,
        //  acmpCnt > cmpCnt ? cmpCnt : acmpCnt) ? : cmpCnt - acmpCnt);
}
