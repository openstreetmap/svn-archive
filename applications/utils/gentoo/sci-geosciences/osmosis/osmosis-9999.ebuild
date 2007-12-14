# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

inherit java-pkg-2 java-ant-2 subversion eutils

DESCRIPTION="Osmosis is a command line java app for processing OSM data."
HOMEPAGE="http://wiki.openstreetmap.org/index.php/Osmosis"
SRC_URI=""
LICENSE="Unknown Apache-2.0"
SLOT="0"
KEYWORDS="~amd64"
DEPEND="dev-java/ant-nodeps
		mysql? ( >=dev-java/jdbc-mysql-5 )
		postgres? ( >=dev-java/jdbc-postgresql-8.2 )
		>=virtual/jdk-1.5"
IUSE="mysql postgres"

ESVN_REPO_URI="http://svn.openstreetmap.org/applications/utils/osmosis/"

pkg_setup() {
	if ! use mysql && ! use postgres; then
		ewarn "If you use neither the mysql nor the postgres USE-flags"
		ewarn "you will have no support for databases"
	fi
}

src_compile() {
	eant -f build.xml build_binary || die "ant failed"
}

src_install() {
	java-pkg_dojar build/binary/osmosis.jar

	dobin ${FILESDIR}/osmosis
	dodoc doc/contributors.txt
}

pkg_postinst() {
	if use mysql || use postgres; then
		einfo "You can get the initial schema as a SQL-file from"
		einfo "http://gweb.bretth.com/osm_schema_latest.sql"
	fi
}
