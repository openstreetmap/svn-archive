project(perl_lib)

if (WITH_SCRIPTS)

  MACRO_OPTIONAL_FIND_PACKAGE(Perl)

  if (PERL_FOUND)
    MACRO_OPTIONAL_FIND_PACKAGE(PerlLibs)
    #MESSAGE("DEBUG: > PERL_SITELIB: ${PERL_SITELIB}")

    file(GLOB_RECURSE perl_modules ${CMAKE_CURRENT_SOURCE_DIR}/*.pm)

      if (perl_modules)
        foreach(perl_module ${perl_modules})
          file(RELATIVE_PATH relative_module_path ${CMAKE_CURRENT_SOURCE_DIR} ${perl_module})
          get_filename_component(relative_module_path ${relative_module_path} PATH)
          string(REGEX REPLACE "${PERL_PREFIX}" "${CMAKE_INSTALL_PREFIX}" GPSDRIVE_PERL_SITELIB ${PERL_SITELIB})

          install(FILES ${perl_module} DESTINATION ${GPSDRIVE_PERL_SITELIB}/${relative_module_path})
          #message("DEBUG: install ${perl_module} to ${GPSDRIVE_PERL_SITELIB}/${relative_module_path}")
        endforeach(perl_module ${perl_modules})
      endif (perl_modules)
  endif (PERL_FOUND)
endif (WITH_SCRIPTS)

