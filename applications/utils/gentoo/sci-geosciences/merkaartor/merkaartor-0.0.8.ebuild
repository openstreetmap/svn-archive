# Copyright 1999-2007 Gentoo Technologies, Inc.
# Distributed under the terms of the GNU General Public License v2
# $Header: $
# Taken from http://bugs.gentoo.org/show_bug.cgi?id=155488
# made by Dirk-Lüder Kreie and Boris

inherit qt4

RESTRICT="nomirror"
DESCRIPTION="A Qt4 based map editor for the openstreetmap.org project."
SRC_URI="http://www.irule.be/bvh/c++/merkaartor/versions/Merkaartor-${PV}.tgz"
HOMEPAGE="http://www.irule.be/bvh/c++/merkaartor/"
DEPEND="$(qt4_min_version 4.2)"
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="amd64 x86"

S=${WORKDIR}/${PN}

src_compile() {
	eqmake4 Merkaartor.pro 
	emake || die "emake failed"
}

src_install() {
	dobin release/merkaartor || "dobin failed"
}
