AC_INIT([SiRF], [0.02], [matt@matt-amos.uklinux.net], [sirfdump])
AC_CONFIG_SRCDIR([src/main.cpp])
AM_CONFIG_HEADER(config.h)
AM_INIT_AUTOMAKE
AM_MAINTAINER_MODE

AC_PROG_CC
AC_HEADER_STDC
AC_PROG_CXX
AM_PROG_LEX
AC_PROG_YACC

AC_CHECK_HEADER([curses.h],
		[AC_DEFINE([HAVE_CURSES_H], [], [If we have a curses header])],
		[AC_MSG_ERROR([You must have a Curses library installed.])])
AC_SEARCH_LIBS([initscr],
		[curses ncurses],
		[AC_DEFINE([HAVE_LIBCURSES], [], [If we have a curses library])],
		[AC_MSG_ERROR([You must have a Curses library installed.])])

AC_CHECK_HEADER([pthread.h],
		[AC_DEFINE([HAVE_PTHREAD_H], [], [Threading headers])],
		[AC_MSG_ERROR([You must have a pthreads library.])])
AC_CHECK_HEADER([semaphore.h],
		[AC_DEFINE([HAVE_SEM_H], [], [Semaphore headers])],
		[AC_MSG_ERROR([You must have a pthreads library.])])

AC_CHECK_SIZEOF(unsigned char)
AC_CHECK_SIZEOF(unsigned short)
AC_CHECK_SIZEOF(unsigned int)
AC_CHECK_SIZEOF(unsigned long int)
AC_CHECK_SIZEOF(unsigned long long int)
AC_CHECK_SIZEOF(char)
AC_CHECK_SIZEOF(short)
AC_CHECK_SIZEOF(int)
AC_CHECK_SIZEOF(long int)
AC_CHECK_SIZEOF(long long int)
AC_CHECK_SIZEOF(float)
AC_CHECK_SIZEOF(double)
AC_CHECK_SIZEOF(long double)

AC_C_BIGENDIAN

AM_GNU_GETTEXT([external])
AM_PROG_LIBTOOL

AC_SUBST(CFLAGS)
AC_SUBST(CPPFLAGS)
AC_SUBST(LDFLAGS)

AC_PROG_INSTALL
AC_CONFIG_FILES([ po/Makefile.in 
m4/Makefile 
Makefile
include/Makefile
src/Makefile
libSiRF/Makefile
libSiRF/Packets/Makefile
libSiRF/Packets/Input/Makefile
libSiRF/Packets/Output/Makefile
utils/Makefile
])
AC_OUTPUT
