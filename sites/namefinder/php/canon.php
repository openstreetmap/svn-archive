<?php

/* This class provides services to canonicalise and generalise search terms */

class canon {

  /* static */ function canonical($term, $alternates=FALSE) {

    /* produces a string which is a canonical form of the name. Names are converted like 
       this when indexed, so that when a similar name is converted on search, it can 
       also be converted to canonical form to compare with the canonical index.

       term: a name being indexed or sought, for example 'Ben Nevis' or 'Newmarket Road [A1304]'
       alternates: whether to consider replacing a diacritical character 
         with multiple alternate replacements, for example Danish aring character with aa and a
         (we normally only do this on indexing, not on lookup).

       The canonical form stores each word surrounded by semcolons
       (that is, not a separator, so there will be two between each one
       as well as one at each end. This makes it easy to search with
       wildcards without worrying about the start and end of
       strings. Alternates in the index are separated by #, and there
       is a # at the start and end. This makes it possible to search
       for exact matches (see below) or partial matches while not
       confusing words from alternate parts. # arises in two ways: a
       square bracket in the name - so 'Newmarket Road [A1134]' becomes
       '#;newmarket;;road;#;A1134;#' - and when there are alternate substitutions 
       for diacritical caharacters (see below) - so 'M<u-umlaut>nchen' becomes 
       '#;munchen;#;muenchen;' */

    static $replacements = '';

    /* The replacements table maps UTF characters (multiple byte keys) to ascii equivalents
       so that characters such as u-umlaut can be matched by u and ue. There are multiple
       tables because some characters have more than one functional replacement 
       (as for u-umlaut). We store multiple canonical forms, but search only on one (so 
       M<u-umlaut>nchen is stored as '#;munchen;#;muenchen;#' which means munchen or 
       muenchen as search strings will match one or the ther, and M<u-umlaut>nchen as 
       a search string will match the first.

       see http://www.utf8-chartable.de/unicode-utf8-table.pl for a utf-8 list
    */

    if (empty($replacements)) {
      $replacements = array(
        array(
          // ligatures
          chr(0xc3).chr(0x86) => 'ae', // cap
          chr(0xc3).chr(0xa6) => 'ae',
          chr(0xc5).chr(0x92) => 'oe', // cap
          chr(0xc3).chr(0x86) => 'oe',
          chr(0xc3).chr(0x9f) => 'ss', // german B
          chr(0xc5).chr(0x8a) => 'ng', // cap
          chr(0xc5).chr(0x8b) => 'ng',
          chr(0xe1).chr(0xb5).chr(0xab) => 'ue',
          chr(0xef).chr(0xac).chr(0x80) => 'ff',
          chr(0xef).chr(0xac).chr(0x81) => 'fi',
          chr(0xef).chr(0xac).chr(0x82) => 'fl',
          chr(0xef).chr(0xac).chr(0x83) => 'ffi',
          chr(0xef).chr(0xac).chr(0x84) => 'ffl',
          chr(0xef).chr(0xac).chr(0x85) => 'ft',
          chr(0xef).chr(0xac).chr(0x86) => 'st',

          chr(0x00) => '',
          chr(0x01) => '',
          chr(0x02) => '',
          chr(0x03) => '',
          chr(0x04) => '',
          chr(0x05) => '',
          chr(0x06) => '',
          chr(0x07) => '',
          chr(0x08) => '',
          chr(0x09) => '',
          chr(0x0A) => '',
          chr(0x0B) => '',
          chr(0x0C) => '',
          chr(0x0D) => '',
          chr(0x0E) => '',
          chr(0x0F) => '',

          chr(0x10) => '',
          chr(0x11) => '',
          chr(0x12) => '',
          chr(0x13) => '',
          chr(0x14) => '',
          chr(0x15) => '',
          chr(0x16) => '',
          chr(0x17) => '',
          chr(0x18) => '',
          chr(0x19) => '',
          chr(0x1A) => '',
          chr(0x1B) => '',
          chr(0x1C) => '',
          chr(0x1D) => '',
          chr(0x1E) => '',
          chr(0x1F) => '',

          chr(0x20) => ';;', // space
          chr(0x21) => '', // !
          chr(0x22) => '',  // "
          chr(0x23) => '', // #
          chr(0x24) => '', // $
          chr(0x25) => '',  // %
          chr(0x26) => ';and;', // &
          chr(0x27) => '',  // '
          chr(0x28) => '', // (
          chr(0x29) => '', // )
          chr(0x2A) => '', // *
          chr(0x2B) => '',  // +
          chr(0x2C) => '', // ,
          chr(0x2D) => ';;',  // -
          chr(0x2E) => '', // .
          chr(0x2F) => ';;', // /

          chr(0x3A) => ';;', // :
          chr(0x3B) => ';;', // ;
          chr(0x3C) => '',  // <
          chr(0x3D) => ';;', // =
          chr(0x3E) => '',  // >
          chr(0x3F) => '', // ?

          chr(0x41) => 'a', // cap A
          chr(0x42) => 'b',
          chr(0x43) => 'c',
          chr(0x44) => 'd',
          chr(0x45) => 'e',
          chr(0x46) => 'f',
          chr(0x47) => 'g',
          chr(0x48) => 'h',
          chr(0x49) => 'i',
          chr(0x4a) => 'j',
          chr(0x4b) => 'k',
          chr(0x4c) => 'l',
          chr(0x4d) => 'm',
          chr(0x4e) => 'n',
          chr(0x4f) => 'o',
          chr(0x50) => 'p',
          chr(0x51) => 'q',
          chr(0x52) => 'r',
          chr(0x53) => 's',
          chr(0x54) => 't',
          chr(0x55) => 'u',
          chr(0x56) => 'v',
          chr(0x57) => 'w',
          chr(0x58) => 'x',
          chr(0x59) => 'y',
          chr(0x5A) => 'z', // cap Z

          chr(0x5B) => ';#;', // [ - forces separation of components for exact match
          chr(0x5C) => '',  // backslash
          chr(0x5D) => '', // ]
          chr(0x5E) => '',  // hat
          chr(0x5F) => '', // _

          chr(0x60) => '', // backtick
          chr(0x7B) => '', // {
          chr(0x7C) => ';;',  // |
          chr(0x7D) => '', // }
          chr(0x7E) => '',  // tilde
          chr(0x7F) => '', // unused ...

          chr(0xc2).chr(0xA0) => ';;', // nbsp
          chr(0xc2).chr(0xA1) => '',  // upside down exclaim
          chr(0xc2).chr(0xA2) => '',  // c stroke
          chr(0xc2).chr(0xA3) => '',  // pound
          chr(0xc2).chr(0xA4) => '',  // 
          chr(0xc2).chr(0xA5) => '',  // yen
          chr(0xc2).chr(0xA6) => '',  // double bar
          chr(0xc2).chr(0xA7) => '',  // para
          chr(0xc2).chr(0xA8) => '',  // umlaut
          chr(0xc2).chr(0xA9) => '',  // copyright
          chr(0xc2).chr(0xAA) => '',
          chr(0xc2).chr(0xAB) => '',  // laquo
          chr(0xc2).chr(0xAC) => '',  // hook
          chr(0xc2).chr(0xAD) => '',  // SHY
          chr(0xc2).chr(0xAE) => '',  // registered
          chr(0xc2).chr(0xAF) => '',  // bar accent

          chr(0xc2).chr(0xB0) => '',  // degrees
          chr(0xc2).chr(0xB1) => '',  // plus or minus
          chr(0xc2).chr(0xB2) => '',  // squared
          chr(0xc2).chr(0xB3) => '',  // cubed
          chr(0xc2).chr(0xB4) => '',  // rsquo ??
          chr(0xc2).chr(0xB5) => '',  // mu
          chr(0xc2).chr(0xB6) => '',  // para
          chr(0xc2).chr(0xB7) => '',  // dot
          chr(0xc2).chr(0xB8) => '',  // cedilla
          chr(0xc2).chr(0xB9) => '',  // power 1
          chr(0xc2).chr(0xBA) => '',  // power 0
          chr(0xc2).chr(0xBB) => '' ,  // raquo
          chr(0xc2).chr(0xBC) => '',  // quarter
          chr(0xc2).chr(0xBD) => '',  // half
          chr(0xc2).chr(0xBE) => '',  // three quarters
          chr(0xc2).chr(0xBF) => '',  // upside down ? mark

          chr(0xc3).chr(0x80) => 'a',  // A grave
          chr(0xc3).chr(0x81) => 'a',  // A acute
          chr(0xc3).chr(0x82) => 'a',  // A circumflex
          chr(0xc3).chr(0x83) => 'a',  // A tilde
          chr(0xc3).chr(0x84) => 'a',  // A umlaut
          chr(0xc3).chr(0x85) => 'aa', // A ring
          chr(0xc3).chr(0x86) => 'ae', // AE dipthong
          chr(0xc3).chr(0x87) => 'c',  // C cedilla
          chr(0xc3).chr(0x88) => 'e',  // E grave
          chr(0xc3).chr(0x89) => 'e',  // E acute
          chr(0xc3).chr(0x8A) => 'e',  // E circumflex
          chr(0xc3).chr(0x8B) => 'e',  // E double dot
          chr(0xc3).chr(0x8C) => 'i',  // I grave
          chr(0xc3).chr(0x8D) => 'i',  // I acute
          chr(0xc3).chr(0x8E) => 'i',  // I circumflex
          chr(0xc3).chr(0x8F) => 'i',  // I umlaut

          chr(0xc3).chr(0x90) => 'd',  // D bar (eth)
          chr(0xc3).chr(0x91) => 'n',  // N tilde
          chr(0xc3).chr(0x92) => 'o',  // O grave
          chr(0xc3).chr(0x93) => 'o',  // O acute
          chr(0xc3).chr(0x94) => 'o',  // O circumflex
          chr(0xc3).chr(0x95) => 'o',  // O tilde
          chr(0xc3).chr(0x96) => 'o',  // O umlaut
          chr(0xc3).chr(0x97) => '',   // multiply
          chr(0xc3).chr(0x98) => 'o',  // O with slash
          chr(0xc3).chr(0x99) => 'u',  // U grave
          chr(0xc3).chr(0x9A) => 'u',  // U acute
          chr(0xc3).chr(0x9B) => 'u',  // U circumflex
          chr(0xc3).chr(0x9C) => 'u',  // U umlaut
          chr(0xc3).chr(0x9D) => 'y',  // y acute
          chr(0xc3).chr(0x9E) => 'th', // D with long straight edge- thorn
          chr(0xc3).chr(0x9F) => 'ss', // german "B" like

          chr(0xc3).chr(0xA0) => 'a',  // a grave
          chr(0xc3).chr(0xA1) => 'a',  // a acute
          chr(0xc3).chr(0xA2) => 'a',  // a circumflex
          chr(0xc3).chr(0xA3) => 'a',  // a tilde
          chr(0xc3).chr(0xA4) => 'a',  // a umlaut
          chr(0xc3).chr(0xA5) => 'aa', // a ring
          chr(0xc3).chr(0xA6) => 'ae', // dipthong
          chr(0xc3).chr(0xA7) => 'c',  // c cedilla
          chr(0xc3).chr(0xA8) => 'e',  // e grave
          chr(0xc3).chr(0xA9) => 'e',  // e acute
          chr(0xc3).chr(0xAA) => 'e',  // e circumflex
          chr(0xc3).chr(0xAB) => 'e',  // e umlaut
          chr(0xc3).chr(0xAC) => 'i',  // i grave
          chr(0xc3).chr(0xAD) => 'i',  // i acute
          chr(0xc3).chr(0xAE) => 'i',  // i circumflex
          chr(0xc3).chr(0xAF) => 'i',  // i umlaut

          chr(0xc3).chr(0xB0) => 'd',  // lower case eth
          chr(0xc3).chr(0xB1) => 'n',  // n tilde
          chr(0xc3).chr(0xB2) => 'o',  // o grave
          chr(0xc3).chr(0xB3) => 'o',  // o acute
          chr(0xc3).chr(0xB4) => 'o',  // o circumflex
          chr(0xc3).chr(0xB5) => 'o',  // o tilde
          chr(0xc3).chr(0xB6) => 'o',  // o umlaut
          chr(0xc3).chr(0xB7) => '',   // divide
          chr(0xc3).chr(0xB8) => 'o',  // o slash (scandinavian)
          chr(0xc3).chr(0xB9) => 'u',  // u grave
          chr(0xc3).chr(0xBA) => 'u',  // u acute
          chr(0xc3).chr(0xBB) => 'u',  // u circumflex
          chr(0xc3).chr(0xBC) => 'u',  // u umlaut
          chr(0xc3).chr(0xBD) => 'y',  // y acute
          chr(0xc3).chr(0xBE) => 'th', // thorn
          chr(0xc3).chr(0xBF) => 'y',   // y umlaut

          chr(0xc4).chr(0x80) => 'a',  
          chr(0xc4).chr(0x81) => 'a',  
          chr(0xc4).chr(0x82) => 'a',  
          chr(0xc4).chr(0x83) => 'a',  
          chr(0xc4).chr(0x84) => 'a',  
          chr(0xc4).chr(0x85) => 'a', 
          chr(0xc4).chr(0x86) => 'c', 
          chr(0xc4).chr(0x87) => 'c',  
          chr(0xc4).chr(0x88) => 'c',  
          chr(0xc4).chr(0x89) => 'c',  
          chr(0xc4).chr(0x8A) => 'c',  
          chr(0xc4).chr(0x8B) => 'c',  
          chr(0xc4).chr(0x8C) => 'c',  
          chr(0xc4).chr(0x8D) => 'c',  
          chr(0xc4).chr(0x8E) => 'd',  
          chr(0xc4).chr(0x8F) => 'd',  

          chr(0xc4).chr(0x90) => 'd',  
          chr(0xc4).chr(0x91) => 'd',  
          chr(0xc4).chr(0x92) => 'e',  
          chr(0xc4).chr(0x93) => 'e',  
          chr(0xc4).chr(0x94) => 'e',  
          chr(0xc4).chr(0x95) => 'e',  
          chr(0xc4).chr(0x96) => 'e',  
          chr(0xc4).chr(0x97) => 'e',   
          chr(0xc4).chr(0x98) => 'e',  
          chr(0xc4).chr(0x99) => 'e',  
          chr(0xc4).chr(0x9A) => 'e',  
          chr(0xc4).chr(0x9B) => 'e',  
          chr(0xc4).chr(0x9C) => 'g',  
          chr(0xc4).chr(0x9D) => 'g',  
          chr(0xc4).chr(0x9E) => 'g', 
          chr(0xc4).chr(0x9F) => 'g',   

          chr(0xc4).chr(0xA0) => 'g',  
          chr(0xc4).chr(0xA1) => 'g',  
          chr(0xc4).chr(0xA2) => 'g',  
          chr(0xc4).chr(0xA3) => 'g',  
          chr(0xc4).chr(0xA4) => 'h',  
          chr(0xc4).chr(0xA5) => 'h',  
          chr(0xc4).chr(0xA6) => 'h',  
          chr(0xc4).chr(0xA7) => 'h',   
          chr(0xc4).chr(0xA8) => 'i',  
          chr(0xc4).chr(0xA9) => 'i',  
          chr(0xc4).chr(0xAA) => 'i',  
          chr(0xc4).chr(0xAB) => 'i',  
          chr(0xc4).chr(0xAC) => 'i',  
          chr(0xc4).chr(0xAD) => 'i',  
          chr(0xc4).chr(0xAE) => 'i', 
          chr(0xc4).chr(0xAF) => 'i',   

          chr(0xc4).chr(0xB0) => 'i',  
          chr(0xc4).chr(0xB1) => 'i',  
          chr(0xc4).chr(0xB2) => 'ij',  
          chr(0xc4).chr(0xB3) => 'ij',  
          chr(0xc4).chr(0xB4) => 'j',  
          chr(0xc4).chr(0xB5) => 'j',  
          chr(0xc4).chr(0xB6) => 'k',  
          chr(0xc4).chr(0xB7) => 'k',   
          // chr(0xc4).chr(0xB8) => '',  kra
          chr(0xc4).chr(0xB9) => 'l',  
          chr(0xc4).chr(0xBA) => 'l',  
          chr(0xc4).chr(0xBB) => 'l',  
          chr(0xc4).chr(0xBC) => 'l',  
          chr(0xc4).chr(0xBD) => 'l',  
          chr(0xc4).chr(0xBE) => 'l', 
          chr(0xc4).chr(0xBF) => 'l',   

          chr(0xc5).chr(0x80) => 'l',
          chr(0xc5).chr(0x81) => 'l',  
          chr(0xc5).chr(0x82) => 'l',  
          chr(0xc5).chr(0x83) => 'n',  
          chr(0xc5).chr(0x84) => 'n',  
          chr(0xc5).chr(0x85) => 'n', 
          chr(0xc5).chr(0x86) => 'n', 
          chr(0xc5).chr(0x87) => 'n',  
          chr(0xc5).chr(0x88) => 'n',  
          chr(0xc5).chr(0x89) => 'n',  
          chr(0xc5).chr(0x8A) => 'n',  // eng
          chr(0xc5).chr(0x8B) => 'n',  // eng
          chr(0xc5).chr(0x8C) => 'o',  
          chr(0xc5).chr(0x8D) => 'o',  
          chr(0xc5).chr(0x8E) => 'o',  
          chr(0xc5).chr(0x8F) => 'o',  

          chr(0xc5).chr(0x90) => 'o',  
          chr(0xc5).chr(0x91) => 'o',  
          chr(0xc5).chr(0x92) => 'oe',  
          chr(0xc5).chr(0x93) => 'oe',  
          chr(0xc5).chr(0x94) => 'r',  
          chr(0xc5).chr(0x95) => 'r',  
          chr(0xc5).chr(0x96) => 'r',  
          chr(0xc5).chr(0x97) => 'r',   
          chr(0xc5).chr(0x98) => 'r',  
          chr(0xc5).chr(0x99) => 'r',  
          chr(0xc5).chr(0x9A) => 's',  
          chr(0xc5).chr(0x9B) => 's',  
          chr(0xc5).chr(0x9C) => 's',  
          chr(0xc5).chr(0x9D) => 's',  
          chr(0xc5).chr(0x9E) => 's', 
          chr(0xc5).chr(0x9F) => 's',   

          chr(0xc5).chr(0xA0) => 's',  
          chr(0xc5).chr(0xA1) => 's',  
          chr(0xc5).chr(0xA2) => 't',  
          chr(0xc5).chr(0xA3) => 't',  
          chr(0xc5).chr(0xA4) => 't',  
          chr(0xc5).chr(0xA5) => 't',  
          chr(0xc5).chr(0xA6) => 't',  
          chr(0xc5).chr(0xA7) => 't',   
          chr(0xc5).chr(0xA8) => 'u',  
          chr(0xc5).chr(0xA9) => 'u',  
          chr(0xc5).chr(0xAA) => 'u',  
          chr(0xc5).chr(0xAB) => 'u',  
          chr(0xc5).chr(0xAC) => 'u',  
          chr(0xc5).chr(0xAD) => 'u',  
          chr(0xc5).chr(0xAE) => 'u', 
          chr(0xc5).chr(0xAF) => 'u',

          chr(0xc5).chr(0xB0) => 'u',  
          chr(0xc5).chr(0xB1) => 'u',  
          chr(0xc5).chr(0xB2) => 'u',  
          chr(0xc5).chr(0xB3) => 'u',  
          chr(0xc5).chr(0xB4) => 'w',  
          chr(0xc5).chr(0xB5) => 'w',  
          chr(0xc5).chr(0xB6) => 'y',  
          chr(0xc5).chr(0xB7) => 'y',   
          chr(0xc5).chr(0xB8) => 'y',
          chr(0xc5).chr(0xB9) => 'z',  
          chr(0xc5).chr(0xBA) => 'z',  
          chr(0xc5).chr(0xBB) => 'z',  
          chr(0xc5).chr(0xBC) => 'z',  
          chr(0xc5).chr(0xBD) => 'z',  
          chr(0xc5).chr(0xBE) => 'z', 
          chr(0xc5).chr(0xBF) => 's',   
   
          chr(0xc6).chr(0x80) => 'b',
          chr(0xc6).chr(0x81) => 'b',  
          chr(0xc6).chr(0x82) => 'b',  
          chr(0xc6).chr(0x83) => 'b',  
          //chr(0xc6).chr(0x84) => '',  
          //chr(0xc6).chr(0x85) => '', 
          //chr(0xc6).chr(0x86) => '', 
          chr(0xc6).chr(0x87) => 'c',  
          chr(0xc6).chr(0x88) => 'c',  
          chr(0xc6).chr(0x89) => 'd',  
          chr(0xc6).chr(0x8A) => 'd',  
          chr(0xc6).chr(0x8B) => 'd',  
          chr(0xc6).chr(0x8C) => 'd',  
          //chr(0xc6).chr(0x8D) => '',  
          //chr(0xc6).chr(0x8E) => '',  
          //chr(0xc6).chr(0x8F) => '',  

          chr(0xc6).chr(0x90) => 'e',  
          chr(0xc6).chr(0x91) => 'f',  
          chr(0xc6).chr(0x92) => 'f',  
          chr(0xc6).chr(0x93) => 'g',  
          //chr(0xc6).chr(0x94) => '',  
          //chr(0xc6).chr(0x95) => '',  
          chr(0xc6).chr(0x96) => 'i',  
          chr(0xc6).chr(0x97) => 'i',   
          chr(0xc6).chr(0x98) => 'k',  
          chr(0xc6).chr(0x99) => 'k',  
          chr(0xc6).chr(0x9A) => 'l',  
          //chr(0xc6).chr(0x9B) => 'e',  
          //chr(0xc6).chr(0x9C) => 'g',  
          chr(0xc6).chr(0x9D) => 'n',  
          chr(0xc6).chr(0x9E) => 'n', 
          chr(0xc6).chr(0x9F) => 'o',   

          chr(0xc6).chr(0xA0) => 'o',  
          chr(0xc6).chr(0xA1) => 'o',  
          chr(0xc6).chr(0xA2) => 'oi',  
          chr(0xc6).chr(0xA3) => 'oi',  
          chr(0xc6).chr(0xA4) => 'p',  
          chr(0xc6).chr(0xA5) => 'p',  
          chr(0xc6).chr(0xA6) => 'yr',  
          //chr(0xc6).chr(0xA7) => '',   
          //chr(0xc6).chr(0xA8) => '',  
          //chr(0xc6).chr(0xA9) => 'i',  
          //chr(0xc6).chr(0xAA) => 'i',  
          chr(0xc6).chr(0xAB) => 't',  
          chr(0xc6).chr(0xAC) => 't',  
          chr(0xc6).chr(0xAD) => 't',  
          chr(0xc6).chr(0xAE) => 't', 
          chr(0xc6).chr(0xAF) => 'u',   

          chr(0xc6).chr(0xB0) => 'u',  
          //chr(0xc6).chr(0xB1) => '',  
          chr(0xc6).chr(0xB2) => 'v',  
          chr(0xc6).chr(0xB3) => 'y',  
          chr(0xc6).chr(0xB4) => 'y',  
          chr(0xc6).chr(0xB5) => 'z',  
          chr(0xc6).chr(0xB6) => 'z',  
          chr(0xc6).chr(0xB7) => 'k',   

          chr(0xc7).chr(0x84) => 'dz',  
          chr(0xc7).chr(0x85) => 'dz', 
          chr(0xc7).chr(0x86) => 'dz', 
          chr(0xc7).chr(0x87) => 'lj',  
          chr(0xc7).chr(0x88) => 'lj',  
          chr(0xc7).chr(0x89) => 'lj',  
          chr(0xc7).chr(0x8A) => 'nj',
          chr(0xc7).chr(0x8B) => 'nj',
          chr(0xc7).chr(0x8C) => 'nj',  
          chr(0xc7).chr(0x8D) => 'a',  
          chr(0xc7).chr(0x8E) => 'a',  
          chr(0xc7).chr(0x8F) => 'i',  

          chr(0xc7).chr(0x90) => 'i',  
          chr(0xc7).chr(0x91) => 'o',  
          chr(0xc7).chr(0x92) => 'o',  
          chr(0xc7).chr(0x93) => 'u',  
          chr(0xc7).chr(0x94) => 'u',  
          chr(0xc7).chr(0x95) => 'u',  
          chr(0xc7).chr(0x96) => 'u',  
          chr(0xc7).chr(0x97) => 'u',   
          chr(0xc7).chr(0x98) => 'u',  
          chr(0xc7).chr(0x99) => 'u',  
          chr(0xc7).chr(0x9A) => 'u',  
          chr(0xc7).chr(0x9B) => 'u',  
          chr(0xc7).chr(0x9C) => 'u',  
          // chr(0xc7).chr(0x9D) => '',  
          chr(0xc7).chr(0x9E) => 'a', 
          chr(0xc7).chr(0x9F) => 'a',   

          chr(0xc7).chr(0xA0) => 'a',  
          chr(0xc7).chr(0xA1) => 'a',  
          chr(0xc7).chr(0xA2) => 'ae',  
          chr(0xc7).chr(0xA3) => 'ae',  
          chr(0xc7).chr(0xA4) => 'g',  
          chr(0xc7).chr(0xA5) => 'g',  
          chr(0xc7).chr(0xA6) => 'g',  
          chr(0xc7).chr(0xA7) => 'g',   
          chr(0xc7).chr(0xA8) => 'k',  
          chr(0xc7).chr(0xA9) => 'k',  
          chr(0xc7).chr(0xAA) => 'q',  
          chr(0xc7).chr(0xAB) => 'q',  
          chr(0xc7).chr(0xAC) => 'q',  
          chr(0xc7).chr(0xAD) => 'q',  

          chr(0xc7).chr(0xB0) => 'j',  
          chr(0xc7).chr(0xB1) => 'dz',  
          chr(0xc7).chr(0xB2) => 'dz',  
          chr(0xc7).chr(0xB3) => 'dz',  
          chr(0xc7).chr(0xB4) => 'g',  
          chr(0xc7).chr(0xB5) => 'g',  
          chr(0xc7).chr(0xB8) => 'n',
          chr(0xc7).chr(0xB9) => 'n',  
          chr(0xc7).chr(0xBA) => 'a',  
          chr(0xc7).chr(0xBB) => 'a',  
          chr(0xc7).chr(0xBC) => 'ae',  
          chr(0xc7).chr(0xBD) => 'ae',  
          chr(0xc7).chr(0xBE) => 'o', 
          chr(0xc7).chr(0xBF) => 'o',   
   
          chr(0xc8).chr(0x80) => 'a',
          chr(0xc8).chr(0x81) => 'a',  
          chr(0xc8).chr(0x82) => 'a',  
          chr(0xc8).chr(0x83) => 'a',  
          chr(0xc8).chr(0x84) => 'e',  
          chr(0xc8).chr(0x85) => 'e', 
          chr(0xc8).chr(0x86) => 'e', 
          chr(0xc8).chr(0x87) => 'e',  
          chr(0xc8).chr(0x88) => 'i',  
          chr(0xc8).chr(0x89) => 'i',  
          chr(0xc8).chr(0x8A) => 'i',  
          chr(0xc8).chr(0x8B) => 'i',  
          chr(0xc8).chr(0x8C) => 'o',  
          chr(0xc8).chr(0x8D) => 'o',  
          chr(0xc8).chr(0x8E) => 'o',  
          chr(0xc8).chr(0x8F) => 'o',  

          chr(0xc8).chr(0x90) => 'r',  
          chr(0xc8).chr(0x91) => 'r',  
          chr(0xc8).chr(0x92) => 'r',  
          chr(0xc8).chr(0x93) => 'r',  
          chr(0xc8).chr(0x94) => 'u',  
          chr(0xc8).chr(0x95) => 'u',  
          chr(0xc8).chr(0x96) => 'u',  
          chr(0xc8).chr(0x97) => 'u',   
          chr(0xc8).chr(0x98) => 's',  
          chr(0xc8).chr(0x99) => 's',  
          chr(0xc8).chr(0x9A) => 't',  
          chr(0xc8).chr(0x9B) => 't',  
          //chr(0xc8).chr(0x9C) => '',  
          //chr(0xc8).chr(0x9D) => '',  
          chr(0xc8).chr(0x9E) => 'h', 
          chr(0xc8).chr(0x9F) => 'h',   

          chr(0xc8).chr(0xA0) => 'n',  
          chr(0xc8).chr(0xA1) => 'd',  
          chr(0xc8).chr(0xA2) => 'ou',  
          chr(0xc8).chr(0xA3) => 'ou',  
          chr(0xc8).chr(0xA4) => 'z',  
          chr(0xc8).chr(0xA5) => 'z',  
          chr(0xc8).chr(0xA6) => 'a',  
          chr(0xc8).chr(0xA7) => 'a',   
          chr(0xc8).chr(0xA8) => 'e',  
          chr(0xc8).chr(0xA9) => 'e',  
          chr(0xc8).chr(0xAA) => 'o',  
          chr(0xc8).chr(0xAB) => 'o',  
          chr(0xc8).chr(0xAC) => 'o',  
          chr(0xc8).chr(0xAD) => 'o',  
          chr(0xc8).chr(0xAE) => 'o', 
          chr(0xc8).chr(0xAF) => 'o',   

          chr(0xc8).chr(0xB0) => 'o',  
          chr(0xc8).chr(0xB1) => 'o',  
          chr(0xc8).chr(0xB2) => 'y',  
          chr(0xc8).chr(0xB3) => 'y',  
          chr(0xc8).chr(0xB4) => 'l',  
          chr(0xc8).chr(0xB5) => 'n',  
          chr(0xc8).chr(0xB6) => 't',  
          chr(0xc8).chr(0xB7) => 'j',   
          chr(0xc8).chr(0xB8) => 'db',
          chr(0xc8).chr(0xB9) => 'qp',  
          chr(0xc8).chr(0xBA) => 'a',  
          chr(0xc8).chr(0xBB) => 'c',  
          chr(0xc8).chr(0xBC) => 'c',  
          chr(0xc8).chr(0xBD) => 'l',  
          chr(0xc8).chr(0xBE) => 't', 
          chr(0xc8).chr(0xBF) => 's',   

          chr(0xc9).chr(0x80) => 'z', 

          chr(0xc9).chr(0x93) => 'b',  
          chr(0xc9).chr(0x95) => 'c',  
          chr(0xc9).chr(0x96) => 'd',  
          chr(0xc9).chr(0x97) => 'd',   
          chr(0xc9).chr(0x9B) => 'e',  

          chr(0xc9).chr(0xA0) => 'g',  
          chr(0xc9).chr(0xA1) => 'g',  
          chr(0xc9).chr(0xA2) => 'g',  
          chr(0xc9).chr(0xA6) => 'h',  
          chr(0xc9).chr(0xA7) => 'h',   
          chr(0xc9).chr(0xA8) => 'i',  
          chr(0xc9).chr(0xA9) => 'i',  
          chr(0xc9).chr(0xAA) => 'i',  
          chr(0xc9).chr(0xAB) => 'l',  
          chr(0xc9).chr(0xAC) => 'l',  
          chr(0xc9).chr(0xAD) => 'l',  

          chr(0xc9).chr(0xB1) => 'm',  
          chr(0xc9).chr(0xB2) => 'n',  
          chr(0xc9).chr(0xB3) => 'n',  
          chr(0xc9).chr(0xB4) => 'n',  
          chr(0xc9).chr(0xB5) => 'o',  
          chr(0xc9).chr(0xB6) => 'oe',
          chr(0xc9).chr(0xB9) => 'r',  
          chr(0xc9).chr(0xBA) => 'r',  
          chr(0xc9).chr(0xBB) => 'r',  
          chr(0xc9).chr(0xBC) => 'r',  
          chr(0xc9).chr(0xBD) => 'r',  
          chr(0xc9).chr(0xBE) => 'r', 
          chr(0xc9).chr(0xBF) => 'r'   
   
          // possibly others from the extended latin sets
        ),
        // alternate replacements:
        array(
          chr(0xc3).chr(0x84) => 'ae',  // A umlaut
          chr(0xc3).chr(0xA4) => 'ae',  // a umlaut

          chr(0xc3).chr(0x96) => 'oe',  // O umlaut
          chr(0xc3).chr(0xB6) => 'oe',  // o umlaut

          chr(0xc3).chr(0x9C) => 'ue',  // U umlaut
          chr(0xc3).chr(0xBC) => 'ue',  // u umlaut

          chr(0xc3).chr(0x85) => 'a', // A ring
          chr(0xc3).chr(0xA5) => 'a', // a ring

          chr(0xc3).chr(0x98) => 'oe',  // O slash (scandinavian)
          chr(0xc3).chr(0xB8) => 'oe',  // o slash 

          chr(0xc3).chr(0xB1) => 'ng',  // n tilde, spanish

          chr(0xc3).chr(0x90) => 'dh',  // D bar (eth)
          chr(0xc3).chr(0xB0) => 'dh',  // lower case eth
        ),
        array(
          chr(0xc3).chr(0xB1) => 'ny',  // n tilde, catalan
        ),
        array(
          chr(0xc3).chr(0xB1) => 'nh',  // n tilde, portugese
        ),
      );
    }

    /* separate the search terms into words */
    $terms = explode(' ', $term);
    $canon = '';
    $prefix = '';
    $usedreplacement = TRUE;

    /* try each replacements table */
    for($alt = 0; $alt < count($replacements); $alt++) {
      $replacement =& $replacements[$alt];
      $thiscanon = '';
      foreach ($terms as $term) {
        /* remove apostrophe-s: these are always stored and searched in the singular
           non-possessive so that (the church of, for example) 'St Andrew's', 
           'St Andrews' and 'St Andrew' all match equivalently */
        $term = preg_replace('~\\\'s$~', '', trim(strtolower($term)));
        if (empty($term)) { continue; }

        $l = mb_strlen($term, 'UTF-8');
        $s = '';
        for ($i = 0; $i < $l; $i++) {
          /* replace listed UTF-8 characters with their ascii
             equivalents.  For search terms we only replace from the
             main replacement table, but so that we get alternatives
             to search for, we replace from all the tables in turn
             (falling back to the main table if not in the alternates */
          $c = mb_substr($term, $i, 1, 'UTF-8');
          if (array_key_exists($c, $replacement)) {
            $s .= $replacement[$c];
            $usedreplacement = TRUE;
          } else if ($alt > 0 && array_key_exists($c, $replacements[0])) {
            $s .= $replacements[0][$c];
          } else {
            $s .= $c;
          }
        }
        $s = trim($s);
        if (empty($s)) { continue; }

        $thiscanon .= ";{$s};";  /* see above re note about semicolon delimiters */
        while (strpos($thiscanon, ';;;') !== FALSE) {
          $thiscanon = str_replace(';;;', ';;', $thiscanon); 
          /* ... arising from replacing multiple consecutive chars with space */
        }
        if (strlen($thiscanon) > 2 && substr($thiscanon, strlen($thiscanon)-2, 2) == ';;') {
          $thiscanon = substr($thiscanon, 0, strlen($thiscanon)-1);
        }
      }
      if ($usedreplacement) {
        $canon .= $prefix . $thiscanon;
        $prefix = '#'; /* see above re note about the hash sign */
      }
      if (! $alternates) { break; }
      $usedreplacement = FALSE;
    }
    return $canon;
  }

  // --------------------------------------------------
  /* static */ function canonical_with_synonym($term) {
    /* cononicalise the term as above, but also create mutliple
       canonical strings where each has a variation in common
       abbreviations (road for rd etc, and vice-versa, and singnular
       for plural - that's particularly important for church names and
       similar, where we want to match "St John's" with "St John" or
       "St Johns" (simple canonicalisation will have removed the
       apostrophe, so the plural to singular also acts as possessive
       to non-possessive */

    static $synonyms = array(
      'road'=>'rd',          'rd'=>'road',
      'street'=>'st',        'st'=>array('street','saint'),
      'avenue'=>'ave',       'ave'=>'avenue',
      'crescent'=>'cres',    'cres'=>'crescent',
      'close'=>'cl',         'cl'=>'close',
      'way'=>'wy',           'wy'=>'way',
      'highway'=>'hwy',      'hwy'=>'highway',
      'house'=>'hse',        'hse'=>'house',
      'court'=>'ct',         'ct'=>'court',
      'park'=>'pk',          'pk'=>'park',
      'lane'=>'ln',          'ln'=>'lane',
      'rue'=>'r',            'r'=>'rue',
      'boulevard'=>'blvd',   'blvd'=>'boulevard',
      'boulevard'=>'bvd',    'bvd'=>'boulevard',
      'drive'=>'drv',        'drv'=>'drive',
      'saint'=>'st',         // see above
      'international'=>'intl', 'intl'=>'international', // as in airports
      'stn'=>'station',      'station'=>'stn',
      'north'=>'n',          'n'=>'north',
      'south'=>'s',          's'=>'south',
      'east'=>'e',           'e'=>'east',
      'west'=>'w',           'w'=>'west'
    );

    $term = canon::canonical($term);
    if ($term == '') { return array(); }

    $words = explode(';', $term); // expect blanks at start and end
    $terms = array('');
    for ($w = 1; $w < count($words); $w++) {
      $word = $words[$w];
      $c = count($terms);
      if (! empty($synonyms[$word])) {
        $syns = is_array($synonyms[$word]) ? $synonyms[$word] : array($synonyms[$word]);
        for ($j = 0; $j < count($syns); $j++) {
          for ($i = 0; $i < $c; $i++) { $terms[$i+($j+1)*$c] = $terms[$i].';'.$syns[$j]; }
        }
      } else {
        $lastchar = strlen($word) - 1;
        if ($lastchar >= 0 && $word{$lastchar} == 's') {
          /* apply singular form too, only for 's' not 'es' or other peculiarities */
          for ($i = 0; $i < $c; $i++) { $terms[$i+$c] = $terms[$i].';'.substr($word,0,$lastchar); }
        }
      }
      for ($i = 0; $i < $c; $i++) { $terms[$i] .= ';'.$word; }
    }

    return $terms;
  }

  // --------------------------------------------------
  /* static */ function likecanon1($name, $exact=FALSE) {
    /* generates a SQL fragment which compares canonical indexes with
    given (canonical) name.  exact is a boolean which will mean the
    match has to be exactly word for word (though each word may still
    have accented variants); when false the index need only contain
    all the words in 'name' in the same order to match, though there may be other 
    words before, after or in between. For example, 'Hinton Road' canonicalises 
    to ';hinton;road;'. We may have '#;hinton;road;#' 
    and say '#;cherry;hinton;road;#' in the index. Exact match catches only the first, 
    non-exact both */

    $wild = $exact ? '' : '%';
    return y_op::like('canon', '%#'.$wild.str_replace(';;', ";{$wild};", $name).$wild.'#%');
  }

  // --------------------------------------------------
  /* static */ function likecanon($names, $exact=FALSE) {
    /* generates SQL fragment which ors each of the matches for names from likecanon1  */
    if (count($names) == 1) { return canon::likecanon1($names[0], $exact); }
    $ors = array();
    foreach ($names as $name) { $ors[] = canon::likecanon1($name, $exact); }
    return y_op::oor($ors);
  }

  // --------------------------------------------------
  /* static */ function distancerestriction($lat, $lon) {
    /* This generates a SQL fragment for ORDER BY so that names come back sorted by distance 
       from given latitude and longitude */
    return y_op::oprintf("(pow(%f - {$lat},2) + pow(%f - {$lon},2))", 'lat', 'lon');
  }


  // --------------------------------------------------
  /* static */ function getuniqueid($osmid, $type) {
    /* osm ids are only unique within type (node, segement, way), so we make them unique
       overall by inserting in the osm id an extra loworder decimal digit for the type */
    static $types;
    if (! isset($types)) { $types = array_flip(canon::getosmtypes()); }
    return 10 *$osmid + $types[$type];
  }

  // --------------------------------------------------
  /* static */ function getosmid($id, &$type) {
    /* converts from name finder id to osm id; the converse of getuniqueid above */
    static $types;
    if (! isset($types)) { $types = canon::getosmtypes(); }
    $typeindex = $id % 10;
    $type = $types[$typeindex];
    return (int)floor(($id/10));
  }

  // --------------------------------------------------
  /* static */ function getosmtypes() {
    static $types = array(1=>'node',2=>'segment',3=>'way');    
    return $types;
  }
  
}

?>
