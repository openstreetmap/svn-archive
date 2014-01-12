#!/bin/bash
# replace the revision number in debian/changelog to the last time this 
# Subtree was modified

svncorerevision=`svn info core | grep "Last Changed Rev" | sed 's/Last Changed Rev: //'`
svncorerevision=${svncorerevision/M/}
svnpluginsrevision=`svn info plugins | grep "Last Changed Rev" | sed 's/Last Changed Rev: //'`
svnpluginsrevision=${svnpluginsrevision/M/}
svnrevision="$svncorerevision$svnpluginsrevision"

if [ -n "$svnrevision" ] ; then
    perl -p -i -e "s/\(\S+\)/\(${svnrevision}\)/;" debian/changelog
fi

