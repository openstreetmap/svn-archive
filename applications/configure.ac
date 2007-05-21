#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.


AC_PREREQ(2.61)
AC_INIT(openstreetmap, 1.0, bugs@openstreetmap.org)
AM_INIT_AUTOMAKE(AC_PACKAGE_NAME, AC_PACKAGE_VERSION)
AC_CONFIG_SRCDIR([utils/export/osm2pgsql/osm2pgsql.c])
#AC_CONFIG_HEADER([config.h])
AC_USE_SYSTEM_EXTENSIONS()

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_INSTALL
AC_PROG_LN_S


# Checks for libraries.
# FIXME: Replace `main' with a function in `-lbz2':
AC_CHECK_LIB([bz2], [main])
# FIXME: Replace `main' with a function in `-lccoord':
AC_CHECK_LIB([ccoord], [main])
# FIXME: Replace `main' with a function in `-lcurl':
AC_CHECK_LIB([curl], [main])
# FIXME: Replace `main' with a function in `-lexpat':
AC_CHECK_LIB([expat], [main])
# FIXME: Replace `main' with a function in `-lfreetype':
AC_CHECK_LIB([freetype], [main])
# FIXME: Replace `main' with a function in `-lgd':
AC_CHECK_LIB([gd], [main])
# FIXME: Replace `main' with a function in `-ljeeps':
AC_CHECK_LIB([jeeps], [main])
# FIXME: Replace `main' with a function in `-ljpeg':
AC_CHECK_LIB([jpeg], [main])
# FIXME: Replace `main' with a function in `-lm':
AC_CHECK_LIB([m], [main])
# FIXME: Replace `main' with a function in `-lmapnik':
AC_CHECK_LIB([mapnik], [main])
# FIXME: Replace `main' with a function in `-losm':
AC_CHECK_LIB([osm], [main])
# FIXME: Replace `main' with a function in `-lpng':
AC_CHECK_LIB([png], [main])
# FIXME: Replace `main' with a function in `-lpq':
AC_CHECK_LIB([pq], [main])
# FIXME: Replace `main' with a function in `-lshp':
AC_CHECK_LIB([shp], [main])
# FIXME: Replace `main' with a function in `-lz':
AC_CHECK_LIB([z], [main])

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([fcntl.h float.h inttypes.h limits.h malloc.h stddef.h stdint.h stdlib.h string.h sys/ioctl.h sys/time.h termios.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_C_INLINE
AC_TYPE_INT16_T
AC_TYPE_INT32_T
AC_TYPE_SIZE_T
AC_HEADER_TIME
AC_STRUCT_TM
AC_TYPE_UINT16_T
AC_TYPE_UINT32_T
AC_TYPE_UINT64_T
AC_TYPE_UINT8_T
AC_C_VOLATILE
AC_CHECK_TYPES([ptrdiff_t])

# Checks for library functions.
AC_FUNC_ERROR_AT_LINE
AC_FUNC_MALLOC
AC_FUNC_MEMCMP
AC_FUNC_MKTIME
AC_FUNC_REALLOC
AC_FUNC_SELECT_ARGTYPES
AC_FUNC_STRFTIME
AC_FUNC_STRTOD
AC_FUNC_VPRINTF
AC_CHECK_FUNCS([atexit floor memmove memset pow select sqrt strchr strdup strncasecmp strrchr strtol strtoul])

AC_CONFIG_FILES([
	Makefile
	share/map-icons/Makefile
	utils/export/Makefile
	utils/filter/Makefile
	utils/import/Makefile
	utils/import/osmtrackfilter/Makefile
	utils/little-osm/Makefile
	utils/Makefile
	utils/osm-data/Makefile
	utils/perl_lib/Makefile
	utils/planet.osm/Makefile
	utils/xsltrans/Makefile
	])
AC_OUTPUT
