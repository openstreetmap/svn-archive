# This script will download rana on a machine without SVN available

# Rana itself
wget --recursive --no-parent -nH --cut-dirs=2 http://svn.openstreetmap.org/applications/mobile/rana

# Dependancy on pyroute
wget --recursive --no-parent -nH --cut-dirs=2 --directory-prefix=rana/modules http://svn.openstreetmap.org/applications/routing/pyroutelib2

# Dependancy on pyrender
wget --recursive --no-parent -nH --cut-dirs=2 --directory-prefix=rana/modules http://svn.openstreetmap.org/applications/rendering/pyrender

# remove index files
rm rana/index.html
rm rana/modules/index.html
rm rana/modules/pyrender/index.html
rm rana/modules/pyroutelib2/index.html
