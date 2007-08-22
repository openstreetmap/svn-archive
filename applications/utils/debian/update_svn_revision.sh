svncorerevision=`svnversion core`
svncorerevision=${svncorerevision/M/}
svnpluginsrevision=`svnversion plugins`
svnpluginsrevision=${svnpluginsrevision/M/}
svnrevision="$svncorerevision$svnpluginsrevision"

if [ -n "$svnrevision" ] ; then
    perl -p -i -e "s/\(\S+\)/\(${svnrevision}\)/;" debian/changelog
fi

