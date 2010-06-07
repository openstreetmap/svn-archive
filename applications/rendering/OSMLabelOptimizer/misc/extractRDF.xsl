<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns="http://www.w3.org/1999/XSL/Transform"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
  xmlns:cc="http://web.resource.org/cc/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common"
  xmlns:msxsl="urn:schemas-microsoft-com:xslt"
  xmlns:labels="http://openstreetmap.org/osmarender-labels-rtf"
  xmlns:z="http://openstreetmap.org/osmarender-z-rtf"
  xmlns:svgmap="http://www.openstreetmap.org/osmarender/ontology/svgmap/01/svgmap#"
  xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:xsd ="http://www.w3.org/2001/XMLSchema#"
  exclude-result-prefixes="exslt msxsl z labels xsl inkscape xi xlink svg" 
  version="1.0">
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="svg:metadata[@id='metadata']/rdf:RDF">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="node()|@*">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>
