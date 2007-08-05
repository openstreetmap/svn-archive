#!/bin/sh

dst_path=$1

if [ ! -n "$dst_path" ] ; then
    echo "Please specify a Directory to use as Basedirectory"
    echo "Usage:"
    echo "     $0 <working-dir>"
    exit -1 
fi

package_name=openstreetmap
dst_path=${dst_path%/}

perl_path=$dst_path/usr/local/share/perl5
bin_path=$dst_path/usr/local/bin
share_path=$dst_path/usr/share/$package_name
mkdir -p $perl_path
mkdir -p $bin_path
mkdir -p $share_path

# Copy Perl libraries
find perl_lib/ -name "*.pm" | while read src_fn ; do 
    dst_fn=$perl_path/${src_fn#perl_lib/}
    dst_dir="`dirname "$dst_fn"`"
    test -d $dst_dir || mkdir -p $dst_dir
    cp "$src_fn" "$dst_fn"
done

# Copy Perl Binaries
find ./ -name "*.pl" | while read src_fn ; do 
    dst_fn=$bin_path/${src_fn##*/}
    dst_fn=${dst_fn/.pl}
    if head -1 "$src_fn" | grep -q -e '^#! */usr/bin/perl' ; then
	cp "$src_fn" "$dst_fn"
    else
	echo "WARNING!!! Perl Hash Bang is missing at File '$src_fn'"
	echo "           I'm not adding this File to the debian Package"
	echo "First Line: `head -1 "$src_fn"`"
    fi
done

# Copy Python Binaries
find ./ -name "*.py" | while read src_fn ; do 
    dst_fn=$bin_path/${src_fn##*/}
    dst_fn=${dst_fn/.py}
    if head -1 "$src_fn" | grep -q -e '^#! */usr/bin/python' -e '^#!/usr/bin/env python'; then
	cp "$src_fn" "$dst_fn"
    else
	echo "WARNING!!! Python Hash Bang is missing at File '$src_fn'"
	echo "           I'm not adding this File to the debian Package"
	echo "           First Line: `head -1 "$src_fn"`"
    fi
done


# XXX
# For later:
# Add java tools


