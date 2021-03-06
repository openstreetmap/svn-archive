# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $
# Build upon the official Gentoo-Ebuild by Hanno Boeck

inherit java-pkg-2 java-ant-2 subversion eutils

DESCRIPTION="Java-based editor for the OpenStreetMap project"
HOMEPAGE="http://josm.openstreetmap.de/"
SRC_URI=""
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~amd64 ~ppc ~x86"
DEPEND=">=virtual/jdk-1.5"
IUSE=""

ESVN_REPO_URI="http://josm.openstreetmap.de/svn/trunk"

src_unpack() {
	subversion_src_unpack
	cd "${S}"
	epatch "${FILESDIR}/${P}-buildxml.patch"
}

src_compile() {
	eant -f build.xml compile
}

src_install() {
	eant -f build.xml dist

	dobin "${FILESDIR}/josm" || die

	insinto "/usr/$(get_libdir)/josm/"
	newins "dist/josm-custom.jar" "josm.jar"  || die

	domenu "${FILESDIR}/josm.desktop" || die
	doicon "${FILESDIR}/josm.png" || die
}
