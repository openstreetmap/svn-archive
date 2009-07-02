# - Try to find zziplib
# Once done, this will define
#
#  ZZIPLIB_FOUND - system has zziplib
#  ZZIPLIB_INCLUDE_DIRS - the zziplib include directories
#  ZZIPLIB_LIBRARIES - link these to use zziplib

include(FindPkgConfig)

# Use pkg-config to get hints about paths
pkg_check_modules(ZZIPLIB_PKGCONF zziplib)

# Include dir
find_path(ZZIPLIB_INCLUDE_DIR
  NAMES zzip/lib.h
  PATHS ${ZZIPLIB_PKGCONF_INCLUDE_DIRS}
)

# Finally the library itself
find_library(ZZIPLIB_LIBRARY
  NAMES zzip
  PATHS ${ZZIPLIB_PKGCONF_LIBRARY_DIRS}
)

INCLUDE(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(ZZIPLIB DEFAULT_MSG ZZIPLIB_LIBRARY ZZIPLIB_INCLUDE_DIR)
IF(ZZIPLIB_FOUND)
    set(ZZIPLIB_INCLUDES ${ZZIPLIB_INCLUDE_DIR})
    set(ZZIPLIB_LIBRARIES ${ZZIPLIB_LIBRARY})
ELSE()
    set(ZZIPLIB_INCLUDES)
    set(ZZIPLIB_LIBRARIES)
ENDIF()