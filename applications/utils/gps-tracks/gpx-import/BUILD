To build gpx-import:

You will need a C compiler, and the following Debian/Ubuntu packages:
  zlib1g-dev libbz2-dev libarchive-dev libexpat1-dev libgd2-noxpm-dev

    DB := mysql

Then run:

    make DB=mysql -C src

or:

    make DB=postgres -C src

depending on whether you're using a mysql or postgres database.

Then edit settings.sh to suit your environment, and then run
gpx-import:

    ./settings.sh start src/gpx-import

Enjoy,

Daniel.
