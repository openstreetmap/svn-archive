# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

inherit qt4

RESTRICT="nomirror"
FILE_DATE="2007.09.18"
DESCRIPTION="Views and uploads maps to Garmin GPS receivers."
HOMEPAGE="http://qlandkarte.sourceforge.net"
SRC_URI="mirror://sourceforge/qlandkarte/QLandkarte.${FILE_DATE}.tar.gz"

LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~x86 ~amd64"
IUSE=""

DEPEND="$(qt4_min_version 4.2)
		>=sci-libs/proj-4.4
		>=dev-libs/libusb-0.1"
RDEPEND=""

S=${WORKDIR}/QLandkarte

src_compile() {
	eqmake4 QLandkarte.pro
	make || die "emake failed"
}

src_install() {
	dobin bin/QLandkarte || die "dobin failed"
	
	insinto /usr/$(get_libdir)/qlandkarte/plugins
	doins bin/plugins/* || die "doins failed"  #bug: dereferences links!
}
