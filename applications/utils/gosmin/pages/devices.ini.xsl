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
    
<xsl:template match="device">
[Device <xsl:value-of select="position()"/>]
Series=<xsl:value-of select="@series"/>
Name=<xsl:value-of select="@name"/>
MapDisplay=<xsl:value-of select="display/@map"/>
MapTypFile=<xsl:value-of select="display/@typfile"/>
Firmware=<xsl:value-of select="firmware/@version"/>
Connection=<xsl:value-of select="connection/@type"/><xsl:text> </xsl:text><xsl:value-of select="connection/@version"/>
MassStorageMode=<xsl:value-of select="connection/@massstoragemode"/>
MapImgFiles=<xsl:value-of select="connection/@imgfiles"/>
MemoryMB=<xsl:value-of select="memory/internal/@size"/>
MemoryCard=<xsl:value-of select="memory/slot/@type"/>
MemorySDMaxGB=<xsl:value-of select="memory/slot/card[@type='SD']/@maxsize"/>
MemorySDHCMaxGB=<xsl:value-of select="memory/slot/card[@type='SDHC']/@maxsize"/>
Picture=<xsl:value-of select="picture/@url"/>
PictureSimilar=<xsl:if test="picture/@identical='similiar'">Yes</xsl:if>
Help=<xsl:value-of select="help/@url"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="devices"><xsl:apply-templates/></xsl:template>

</xsl:stylesheet>