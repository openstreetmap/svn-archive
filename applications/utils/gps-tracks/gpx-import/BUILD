To build gpx-import:

You will need a C compiler, and the following Debian/Ubuntu packages:
  zlib1g-dev libbz2-dev libarchive-dev libexpat1-dev libgd2-noxpm-dev

If you're using PostgreSQL you need to change this line in src/Makefile:

    DB := mysql

to this:

    DB := postgres

Then run:

    make -C src

Then edit settings.sh to suit your environment, and then run
gpx-import:

    ./settings.sh start src/gpx-import

Enjoy,

Daniel.
