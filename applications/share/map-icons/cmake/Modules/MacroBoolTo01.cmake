# MACRO_BOOL_TO_01( VAR RESULT0 ... RESULTN )
# This macro evaluates its first argument
# and sets all the given vaiables either to 0 or 1
# depending on the value of the first one

MACRO(MACRO_BOOL_TO_01 FOUND_VAR )
   FOREACH (_current_VAR ${ARGN})
      IF(${FOUND_VAR})
         SET(${_current_VAR} 1)
      ELSE(${FOUND_VAR})
         SET(${_current_VAR} 0)
      ENDIF(${FOUND_VAR})
   ENDFOREACH(_current_VAR)
ENDMACRO(MACRO_BOOL_TO_01)
