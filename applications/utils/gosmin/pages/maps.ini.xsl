<?xml version="1.0" encoding="utf-8"?>
<!-- for details, please have a look at the corresponding .sh file -->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="html"
>
 
    <xsl:output
        method="text"
		indent="no"
        encoding="utf-8"
        doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
        doctype-public="-//W3C//DTD XHTML 1.1//EN"
    />

	<xsl:strip-space elements="*"/>
    
<xsl:template match="map">
[Map <xsl:value-of select="position()"/>]
Name=<xsl:value-of select="@name"/>
Boundary=<xsl:value-of select="@boundary"/>
Comment=<xsl:value-of select="@comment"/>
Updated=<xsl:value-of select="@updated"/>

Name:de=<xsl:value-of select="lang/@name"/>
Boundary:de=<xsl:value-of select="lang/@boundary"/>
Comment:de=<xsl:value-of select="lang/@comment"/>
Updated:de=<xsl:value-of select="lang/@updated"/>

DownloadSizeMB=<xsl:value-of select="download/@sizeMB"/>
DownloadUrl=<xsl:value-of select="download/@url"/>
FileSizeMB=<xsl:value-of select="file/@sizeMB"/>
Routing=<xsl:value-of select="attributes/@routing"/>

Picture=<xsl:value-of select="picture/@url"/>
HelpUrl=<xsl:value-of select="details/@url"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="maps"><xsl:apply-templates/></xsl:template>

</xsl:stylesheet>