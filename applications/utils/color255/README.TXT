color255
========

This is a trivial C program which intends to be a lightweight replacement
for the ImageMagick convert tool. Even though it uses the same ImageMagick
library this program is much faster to convert 32bit RGBA PNGs to an 8bit
colour palette PNG. This is useful for tile renderers which prefer the
smaller tile size and quick conversion.

Requirements
------------
This needs the ImageMagick C library and development headers to be installed

Fedora: yum install ImageMagick-devel
Ubuntu/Debian: apg-get install libmagick9-dev

Tests
-----

A sample set of 700 tiles of 256x256 32bit RGBA PNG from tilecache was
selected. These were then converted individually and timed,

$time find tilecache -type f -name '*.png' -exec convert -colors 255 {} {} \;

real    2m37.116s
user    1m1.218s
sys     1m33.867s

Repeat again with the original tiles:

real    2m40.155s
user    1m3.852s
sys     1m34.423s

Note the enormous amount of system time. This seems to be the real killer
for convert. It isn't clear what it is trying to do which causes this hit.

Now running this tool against the same files:

$ find tilecache -type f -name '*.png' -exec ./color255 {} \;

real    1m3.758s
user    0m56.641s
sys     0m6.435s

Repeat again with the original tiles:

real    1m4.654s
user    0m57.302s
sys     0m6.485s

This version is more than twice as fast (with the drop in system usage
being the biggest change).

It can be made even faster if it can operate on multiple files at once
by amortizing the startup costs:

$ time find tilecache -type f -name '*.png' | xargs ~/bin/color255

real    0m54.372s
user    0m52.441s
sys     0m1.741s

