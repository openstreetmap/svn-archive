#/bin/sh
# Parameter:
# verbose - macht das script ein wenig gesprächiger
# test    - macht keinen "svn up"

if echo "$@" | grep verbose ; then
	verbose="1"	
fi

svn_applications_dir="/var/www/svn.openstreetmap.org/applications"
svn_build_compare_directory="/var/www/build_files"

#----------------------
# Update Copy of svn
# ---------------------
test -n $verbose &&echo "svn UP"
cd $svn_applications_dir/
if ! echo "$@" | grep test ; then
	if ! svn up | grep -v -e 'Hole externen Verweis nach' -e '^$' -e 'Externer Verweis, Revision'; then
		echo "SVN UP Failed"
		exit -1
	fi
	svn revert editors/josm/dist/*.jar 2>/dev/null >/dev/null
	svn revert editors/josm/plugins/lang/*/*.po 2>/dev/null >/dev/null
	svn revert editors/josm/plugins/lang/keys.pot 2>/dev/null >/dev/null
fi

svn status editors/josm/ | grep -e "^M" -e "^C"

# ---------------------
# Compare Build Scripts to doublecheck changed FIles
# ---------------------
test -n $verbose && echo "Compage build Files"
cd
cd $svn_applications_dir/editors/josm/
for file in nsis/josm-setup-unix.sh core/build.xml plugins/*/*build.xml ; do
	if ! diff -b -B -w -u $svn_build_compare_directory/$file $file ; then
		echo "File changed: $file"
		echo "if the diff seems ok do"
		echo "sudo -u www-data cp `pwd`/$file $svn_build_compare_directory/$file"
		diff -b -B -w -u $svn_build_compare_directory/$file $file |
			mail -s "Error in nsis build" "nsis-updater4osm.de@ostertag.name"
		exit 0
	fi
done

cd
cd $svn_applications_dir/editors/josm/nsis
if ! ./josm-setup-unix.sh ; then
	echo "josm+plugin Build failed"
	exit -1
fi

test -n $verbose && echo "move to webserver:"
mv -v josm-setup*.exe /var/www/www.openstreetmap.de/download/

# link latest to highest number
last_file=`ls /var/www/www.openstreetmap.de/download/| grep -v latest | sort | tail -1 `

cd /var/www/www.openstreetmap.de/download
rm josm-setup-latest.exe
ln -s $last_file josm-setup-latest.exe
