# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $
# Taken from http://bugs.gentoo.org/show_bug.cgi?id=155488
# made by Dirk-Lüder Kreie and Boris

inherit qt4 subversion eutils

DESCRIPTION="Qt4-based editor for the OpenStreetMap project"
HOMEPAGE="http://www.irule.be/bvh/c++/merkaartor/"
SRC_URI=""
LICENSE="GPL-2"
SLOT="scm"
KEYWORDS="~amd64 ~x86"
DEPEND="$(qt4_min_version 4.2)"
IUSE=""

ESVN_REPO_URI="http://svn.openstreetmap.org/applications/editors/merkaartor/"

src_compile() {
	eqmake4 Merkaartor.pro
	emake || die "emake failed"
	mv release/merkaartor release/merkaartor-scm
}

src_install() {
	dobin release/merkaartor-scm || die
}
