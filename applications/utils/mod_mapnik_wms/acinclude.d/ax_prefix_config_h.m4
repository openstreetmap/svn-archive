AC_DEFUN([AX_PREFIX_CONFIG_H],[dnl
AC_BEFORE([AC_CONFIG_HEADERS],[$0])dnl
AC_CONFIG_COMMANDS([ifelse($1,,$PACKAGE-config.h,$1)],[dnl
AS_VAR_PUSHDEF([_OUT],[ac_prefix_conf_OUT])dnl
AS_VAR_PUSHDEF([_DEF],[ac_prefix_conf_DEF])dnl
AS_VAR_PUSHDEF([_PKG],[ac_prefix_conf_PKG])dnl
AS_VAR_PUSHDEF([_LOW],[ac_prefix_conf_LOW])dnl
AS_VAR_PUSHDEF([_UPP],[ac_prefix_conf_UPP])dnl
AS_VAR_PUSHDEF([_INP],[ac_prefix_conf_INP])dnl
m4_pushdef([_script],[conftest.prefix])dnl
m4_pushdef([_symbol],[m4_cr_Letters[]m4_cr_digits[]_])dnl
_OUT=`echo ifelse($1, , $PACKAGE-config.h, $1)`
_DEF=`echo _$_OUT | sed -e "y:m4_cr_letters:m4_cr_LETTERS[]:" -e "s/@&lt;:@^m4_cr_Letters@:&gt;@/_/g"`
_PKG=`echo ifelse($2, , $PACKAGE, $2)`
_LOW=`echo _$_PKG | sed -e "y:m4_cr_LETTERS-:m4_cr_letters[]_:"`
_UPP=`echo $_PKG | sed -e "y:m4_cr_letters-:m4_cr_LETTERS[]_:"  -e "/^@&lt;:@m4_cr_digits@:&gt;@/s/^/_/"`
_INP=`echo "ifelse($3,,,$3)" | sed -e 's/ *//'`
if test ".$_INP" = "."; then
   for ac_file in : $CONFIG_HEADERS; do test "_$ac_file" = _: &amp;&amp; continue
     case "$ac_file" in
        *.h) _INP=$ac_file ;;
        *)
     esac
     test ".$_INP" != "." &amp;&amp; break
   done
fi
if test ".$_INP" = "."; then
   case "$_OUT" in
      */*) _INP=`basename "$_OUT"`
      ;;
      *-*) _INP=`echo "$_OUT" | sed -e "s/@&lt;:@_symbol@:&gt;@*-//"`
      ;;
      *) _INP=config.h
      ;;
   esac
fi
if test -z "$_PKG" ; then
   AC_MSG_ERROR([no prefix for _PREFIX_PKG_CONFIG_H])
else
  if test ! -f "$_INP" ; then if test -f "$srcdir/$_INP" ; then
     _INP="$srcdir/$_INP"
  fi fi
  AC_MSG_NOTICE(creating: $_OUT: prefix $_UPP for $_INP defines)
  if test -f $_INP ; then
    echo "s/^@%:@undef  *\\(@&lt;:@m4_cr_LETTERS[]_@:&gt;@\\)/@%:@undef $_UPP""_\\1/" &gt; _script
    echo "s/^@%:@undef  *\\(@&lt;:@m4_cr_letters@:&gt;@\\)/@%:@undef $_LOW""_\\1/" &gt;&gt; _script
    echo "s/^@%:@def[]ine  *\\(@&lt;:@m4_cr_LETTERS[]_@:&gt;@@&lt;:@_symbol@:&gt;@*\\)\\(.*\\)/@%:@ifndef $_UPP""_\\1 \\" &gt;&gt; _script
    echo "@%:@def[]ine $_UPP""_\\1 \\2 \\" &gt;&gt; _script
    echo "@%:@endif/" &gt;&gt;_script
    echo "s/^@%:@def[]ine  *\\(@&lt;:@m4_cr_letters@:&gt;@@&lt;:@_symbol@:&gt;@*\\)\\(.*\\)/@%:@ifndef $_LOW""_\\1 \\" &gt;&gt; _script
    echo "@%:@define $_LOW""_\\1 \\2 \\" &gt;&gt; _script
    echo "@%:@endif/" &gt;&gt; _script
    # now executing _script on _DEF input to create _OUT output file
    echo "@%:@ifndef $_DEF"      &gt;$tmp/pconfig.h
    echo "@%:@def[]ine $_DEF 1" &gt;&gt;$tmp/pconfig.h
    echo ' ' &gt;&gt;$tmp/pconfig.h
    echo /'*' $_OUT. Generated automatically at end of configure. '*'/ &gt;&gt;$tmp/pconfig.h

    sed -f _script $_INP &gt;&gt;$tmp/pconfig.h
    echo ' ' &gt;&gt;$tmp/pconfig.h
    echo '/* once:' $_DEF '*/' &gt;&gt;$tmp/pconfig.h
    echo "@%:@endif" &gt;&gt;$tmp/pconfig.h
    if cmp -s $_OUT $tmp/pconfig.h 2&gt;/dev/null; then
      rm -f $tmp/pconfig.h
      AC_MSG_NOTICE([unchanged $_OUT])
    else
      ac_dir=`AS_DIRNAME(["$_OUT"])`
      AS_MKDIR_P(["$ac_dir"])
      rm -f "$_OUT"
      mv $tmp/pconfig.h "$_OUT"
    fi
    cp _script _configs.sed
  else
    AC_MSG_ERROR([input file $_INP does not exist - skip generating $_OUT])
  fi
  rm -f conftest.*
fi
m4_popdef([_symbol])dnl
m4_popdef([_script])dnl
AS_VAR_POPDEF([_INP])dnl
AS_VAR_POPDEF([_UPP])dnl
AS_VAR_POPDEF([_LOW])dnl
AS_VAR_POPDEF([_PKG])dnl
AS_VAR_POPDEF([_DEF])dnl
AS_VAR_POPDEF([_OUT])dnl
],[PACKAGE="$PACKAGE"])])

dnl implementation note: a bug report (31.5.2005) from Marten Svantesson points 
dnl out a problem where `echo "\1"` results in a Control-A. The unix standard
dnl    http://www.opengroup.org/onlinepubs/000095399/utilities/echo.html
dnl defines all backslash-sequences to be inherently non-portable asking
dnl for replacement mit printf. Some old systems had problems with that
dnl one either. However, the latest libtool (!) release does export an $ECHO 
dnl (and $echo) that does the right thing - just one question is left: what 
dnl was the first version to have it? Is it greater 2.58 ? 

