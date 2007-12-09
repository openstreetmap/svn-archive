# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $
# Taken from http://bugs.gentoo.org/show_bug.cgi?id=194470
# made by Bill Skellenger and Boris

inherit eutils

DESCRIPTION="Viking is a free/open source program to manage GPS data."
HOMEPAGE="http://viking.sourceforge.net/"
SRC_URI="mirror://sourceforge/${PN}/${P}.tar.gz"
LICENSE="GPL-2"
SLOT="0"
IUSE="expedia google old-google openstreetmap realtime-gps terraserver"
KEYWORDS="~x86 ~amd64"

DEPEND=">=x11-libs/gtk+-2.4.0
	>=dev-libs/glib-2.12.0
	net-misc/curl
	realtime-gps? ( sci-geosciences/gpsd )"

# Parallel build is broken
MAKEOPTS="${MAKEOPTS} -j1"

src_compile() {
	econf \
		$(use_enable openstreetmap) \
		$(use_enable expedia) \
		$(use_enable terraserver) \
		$(use_enable old-google) \
		$(use_enable google) \
		$(use_enable realtime-gps realtime-gps-tracking) \
		|| die "configure failed"

	emake || die "emake failed"
}

src_install() {
	einstall || die "Install failed"
	dodoc README doc/GEOCODED-PHOTOS doc/GETTING-STARTED doc/GPSMAPPER \
		|| die "Unable to install docs"
}
