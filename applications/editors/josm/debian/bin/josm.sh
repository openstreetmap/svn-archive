#!/bin/sh
josm_dir="/usr/local/share/josm"
josm_bin="$josm_dir/josm.jar"

if ! [ -s ~/.josm/preferences ]; then
     echo "Installing Preferences File"
     cp "$josm_dir/preferences"  ~/.josm/preferences
fi

if ! [ -s ~/.josm/bookmarks ]; then
     echo "Installing Bookmarks File"
     cp "$josm_dir/bookmarks"  ~/.josm/bookmarks
fi

# ls -l "$josm_bin"
# unzip -p    $josm_bin REVISION | grep "Last Changed"

if [ -n "$http_proxy" ]; then
    proxy_host=${http_proxy%:*}
    proxy_host=${proxy_host#*//}
    proxy_port=${http_proxy##*:}
    proxy_port=${proxy_port%/}
    proxy=" -Dhttp.proxyHost=$proxy_host -Dhttp.proxyPort=$proxy_port "
    echo "Proxy: $proxy"
fi

java -Djosm.resource=/usr/share/icons/map-icons/square.small \
     -Xmx500m \
     $proxy \
     -jar "$josm_bin"\
     "$@"
