#!/bin/sh

case $1 in
    build)
	echo "aclocal" && aclocal && \
	    echo "libtoolize" && libtoolize --force && \
	    echo "autoheader" && autoheader && \
	    echo "automake" && automake --add-missing && \
	    echo "autoconf" && autoconf && \
	    echo "Everything is OK";
	;;

    clean)
	rm -f COPYING \
	    INSTALL \
	    Makefile.in \
	    aclocal.m4 \
	    config.guess \
	    config.h.in \
	    config.sub \
	    configure \
	    depcomp \
	    install-sh \
	    ltmain.sh \
	    missing;
	rm -rf autom4te.cache;
	;;

    *)
	echo "use autogen.sh <build> or <clean>";
	;;

esac;
