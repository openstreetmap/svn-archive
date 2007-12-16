# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: /var/cvsroot/gentoo-x86/sci-geosciences/josm/josm-1.5_p457.ebuild,v 1.1 2007/11/07 15:37:37 hanno Exp $

inherit eutils


RESTRICT="nomirror"

DESCRIPTION="Osmarender-Plugin for josm"
HOMEPAGE="http://josm.openstreetmap.de/"
SRC_URI="http://svn.openstreetmap.org/applications/editors/josm/plugins/dist/osmarender.jar"
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~amd64 ~x86"
DEPEND=">=sci-geosciences/josm-1.5_p480"
S="${WORKDIR}"

src_unpack() {
	einfo "nothing to unpack"
}

src_compile() {
	einfo "nothing to compile"
}

src_install() {
	insinto /usr/lib/josm/plugins
	newins ${DISTDIR}/osmarender.jar osmarender.jar
#	doins *.jar
}
