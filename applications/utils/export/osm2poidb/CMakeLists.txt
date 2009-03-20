project(osm2poidb)

# global needed variables
set(APPLICATION_NAME "osm2poidb")
set(APPLICATION_VERSION "0.2")

# required cmake version
cmake_minimum_required(VERSION 2.4.3)
set(CMAKE_COLOR_MAKEFILE ON)
set(CMAKE_VERBOSE_MAKEFILE OFF)

# where to look first for cmake modules, before ${CMAKE_ROOT}/Modules/ is checked
set(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/cmake/Modules)

# make some more macros available
include(MacroEnsureOutOfSourceBuild)
include(MacroGetSubversionRevision)
macro_get_subversion_revision(SVN_REVISION)

# disallow in-source build
macro_ensure_out_of_source_build("${APPLICATION_NAME} requires an out of source build. Please create a separate build directory and run 'cmake /path/to/osm2poidb [options]' there.")

# macro_optional_find_package() is the same as FIND_PACKAGE(<name>) but additionally creates an OPTION(WITH_<name>)
# so the checking for the software can be disabled via ccmake or -DWITH_<name>=OFF
#MACRO_OPTIONAL_FIND_PACKAGE(DBUS)

include(DefineInstallationPaths)

find_package(GTK2 REQUIRED)
find_package(XML2 REQUIRED)
find_package(SQLite3 REQUIRED)

set(GPSDRIVE_PUBLIC_INCLUDE_DIRS
  ${CMAKE_CURRENT_SOURCE_DIR}
  ${CMAKE_BINARY_DIR}
  CACHE INTERNAL "gpsdrive public include directories"
)

set(GPSDRIVE_PRIVATE_INCLUDE_DIRS
  ${GTK2_INCLUDE_DIRS}
  ${XML2_INCLUDE_DIRS}
  ${SQLITE3_INCLUDE_DIRS}
)

include_directories(
  ${GPSDRIVE_PUBLIC_INCLUDE_DIRS}
  ${GPSDRIVE_PRIVATE_INCLUDE_DIRS}
)

add_definitions (
  -DPACKAGE=${APPLICATION_NAME}
  -DPACKAGE_VERSION=${APPLICATION_VERSION}
)

add_executable(osm2poidb osm2poidb.c)

target_link_libraries(osm2poidb
  ${GTK2_LIBRARIES}
  ${SQLITE3_LIBRARIES}
  ${XML2_LIBRARIES}
)


install(
  TARGETS
    osm2poidb
  DESTINATION
    ${BIN_INSTALL_DIR}
)

