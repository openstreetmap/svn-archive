# Copyright 1999-2007 Gentoo Technologies, Inc.
# Distributed under the terms of the GNU General Public License v2
# $Header: $
# Build upon the official Gentoo-Ebuild by Hanno Boeck

inherit java-pkg-2 java-ant-2 subversion eutils

JOSM_PLUGIN=${PN/josm-plugin-/}

DESCRIPTION="Plugin \"${JOSM_PLUGIN}\" for josm"
HOMEPAGE=""
SRC_URI=""
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~amd64 ~x86"
DEPEND=">=virtual/jdk-1.5"
#=sci-geosciences/josm-svn || =sci-geosciences/josm
IUSE="sci-geosciences/josm-svn sci-geosciences/josm"

EXPORT_FUNCTIONS src_unpack src_compile src_install
ESVN_REPO_URI="http://svn.openstreetmap.org/applications/editors/josm/plugins/${JOSM_PLUGIN}"

josm-plugin-scm_src_unpack() {
	subversion_src_unpack
	#HACK but easy way to have a REVISION
	sed -i -e 's#output="REVISION"#output="_REVISION"#' build.xml
	svn info --xml ${ESVN_STORE_DIR}/${ESVN_PROJECT}/${JOSM_PLUGIN} > REVISION
}

josm-plugin-scm_src_compile() {
	local JAR="${ROOT}/usr/share/josm-scm/lib/josm-custom.jar"
	mkdir dist
	eant -Djosm=${JAR} -Djosm.jar=${JAR} -Dplugin.jar="dist/${JOSM_PLUGIN}.jar" -f build.xml
}

josm-plugin-scm_src_install() {
	insinto /usr/$(get_libdir)/josm/plugins

	doins dist/${JOSM_PLUGIN}.jar
	#for i in dist; do
	#	[[ -f ${i}/${JOSM_PLUGIN}.jar ]] && doins ${i}/${JOSM_PLUGIN}.jar && break
	#done
}
