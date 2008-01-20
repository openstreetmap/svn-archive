# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: /var/cvsroot/gentoo-x86/sci-geosciences/josm/josm-1.5_p457.ebuild,v 1.1 2007/11/07 15:37:37 hanno Exp $

inherit eutils

RESTRICT="nomirror"
MY_P=${PN}-snapshot-${PV/1.5_p/}
DESCRIPTION="Java-based editor for the OpenStreetMap project"
HOMEPAGE="http://josm.openstreetmap.de/"
SRC_URI="http://josm.openstreetmap.de/download/${MY_P}.jar
	linguas_de? ( http://svn.openstreetmap.org/applications/editors/josm/dist/lang-de.jar )
	linguas_en_GB? ( http://svn.openstreetmap.org/applications/editors/josm/dist/lang-en_GB.jar )
	linguas_fr? ( http://svn.openstreetmap.org/applications/editors/josm/dist/lang-fr.jar )
	linguas_ro? ( http://svn.openstreetmap.org/applications/editors/josm/dist/lang-ro.jar )"
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="amd64 ppc x86"
DEPEND="virtual/jre"
S="${WORKDIR}"

LINGUAS="en de"
IUSE="linguas_de linguas_en_GB linguas_fr linguas_ro"

src_unpack() {
	einfo Nothing to unpack
}

src_compile() {
	einfo Nothing to compile
}

src_install() {
	dobin "${FILESDIR}/josm" || die

	insinto /usr/$(get_libdir)/josm/
	newins "${DISTDIR}/${MY_P}.jar" josm.jar || die

	insinto /usr/$(get_libdir)/josm/plugins
	use linguas_de && newins ${DISTDIR}/lang-de.jar lang-de.jar
	use linguas_en_GB && newins ${DISTDIR}/lang-en_GB-20061020.jar lang-en_GB.jar
	use linguas_fr && newins ${DISTDIR}/lang-fr-20061020.jar lang-fr.jar
	use linguas_ro && newins ${DISTDIR}/lang-ro-20061020.jar lang-ro.jar

	domenu "${FILESDIR}/josm.desktop" || die
	doicon "${FILESDIR}/josm.png" || die
}
