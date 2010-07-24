#! /bin/sh

## jump out if one of the programs returns 'false'
set -e

## on macosx glibtoolize, others have libtool
if test x$LIBTOOLIZE = x; then
  if test \! x`which glibtoolize` = x; then
    LIBTOOLIZE=glibtoolize
  elif test \! x`which libtoolize-1.5` = x; then
    LIBTOOLIZE=libtoolize-1.5
  elif test \! x`which libtoolize` = x; then
    LIBTOOLIZE=libtoolize
  fi
fi


$LIBTOOLIZE -f -c && aclocal -I ./acinclude.d -I /usr/share/aclocal && autoheader && automake -ac && autoconf && ./configure "$@"
