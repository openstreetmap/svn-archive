# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $
# Build upon the official Gentoo-Ebuild by Hanno Boeck

inherit java-pkg-2 java-ant-2 subversion eutils

DESCRIPTION="Java-based editor for the OpenStreetMap project"
HOMEPAGE="http://josm.openstreetmap.de/"
SRC_URI=""
LICENSE="GPL-2"
SLOT="scm"
KEYWORDS="~amd64 ~x86"
DEPEND=">=virtual/jdk-1.5"
IUSE=""

ESVN_REPO_URI="http://josm.openstreetmap.de/svn/trunk"

src_compile() {
	eant -f build.xml compile

	subversion_wc_info
	echo "Revision: ${ESVN_WC_REVISION}" > build/REVISION

	eant -f build.xml dist
}

src_install() {
	dobin "${FILESDIR}/josm-scm" || die

	java-pkg_dojar "dist/josm-custom.jar"
}
