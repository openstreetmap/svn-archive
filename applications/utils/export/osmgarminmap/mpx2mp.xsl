<?xml version='1.0' encoding='iso-8859-1' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
<!--

Copyright (C) 2006  Jochen Topf <jochen@remote.org>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA

-->

	<xsl:output method="text" encoding="iso-8859-1"/>

	<xsl:key name='point' match='/defs/point/feature' use="concat(@type,'=',@subtype)"/>
	<xsl:key name='city' match='/defs/city/feature' use="concat(@type,'=',@subtype)"/>
	<xsl:key name='polyline' match="/defs/polyline/feature" use='@type'/>
	<xsl:key name='polygon' match='/defs/polygon/feature' use='@type'/>
	<xsl:key name='feature' match='/defs/*' use='name()'/>

	<xsl:variable name='data' select='document(/defs/@data)/map'/>

    <xsl:template match="/defs">
        <xsl:text>[IMG ID]
ID=</xsl:text>
        <xsl:value-of select="$data/@mapid"/>
        <xsl:text>
Name=</xsl:text>
        <xsl:value-of select="$data/@name"/>
        <xsl:text>
TreSize=5000
RgnLimit=1024
Transparent=Y
LBLcoding=9
Codepage=1252
Levels=8
Level0=24
Level1=23
Level2=22
Level3=21
Level4=20
Level5=19
Level6=18
Level7=17
Zoom0=0
Zoom1=1
Zoom2=2
Zoom3=3
Zoom4=4
Zoom5=5
Zoom6=6
Zoom7=7
Transparent=N
Elevation=m
[END-IMG ID]

</xsl:text>

        <xsl:apply-templates select="$data/point|$data/city|$data/polyline|$data/polygon" mode="data">
            <xsl:with-param name="mapping" select="/defs"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="point|city" mode="data">
        <xsl:param name="mapping"/>
        <xsl:variable name="current" select="name(.)"/>
        <xsl:variable name="feature-id">
            <xsl:for-each select="$mapping">
                <xsl:value-of select="key('feature', $current)/@id"/>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="type" select="concat(@type,'=',@subtype)"/>
        
        <xsl:text>[</xsl:text>
        <xsl:value-of select="$feature-id"/>
        <xsl:text>]
; id=</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>
Type=</xsl:text>
        <xsl:for-each select="$mapping">
            <xsl:value-of select="key($current, $type)/@garmin_id"/>
        </xsl:for-each>

        <xsl:text>
SubType=</xsl:text>
        <xsl:for-each select="$mapping">
            <xsl:value-of select="key($current, $type)/@garmin_subid"/>
        </xsl:for-each>

        <xsl:if test="@endlevel!=''">
            <xsl:text>
EndLevel=</xsl:text>
            <xsl:value-of select="@endlevel"/>
        </xsl:if>

        <xsl:if test="label">
            <xsl:text>
Label=</xsl:text>
            <xsl:value-of select="translate(label, 'abcdefghijklmnopqrstuvwxyzöüä', 'ABCDEFGHIJKLMNOPQRSTUVWXYZÖÜÄ')"/>
        </xsl:if>

        <xsl:text>
Data0=(</xsl:text>
        <xsl:value-of select="data/@latlon"/>
        <xsl:text>)
[END-</xsl:text>
        <xsl:value-of select="$feature-id"/>
        <xsl:text>]

</xsl:text>

    </xsl:template>

    <xsl:template match="polyline|polygon" mode="data">
        <xsl:param name="mapping"/>
        <xsl:variable name="current" select="name(.)"/>
        <xsl:variable name="feature-id">
            <xsl:for-each select="$mapping">
                <xsl:value-of select="key('feature', $current)/@id"/>
            </xsl:for-each>
        </xsl:variable>

        <xsl:text>[</xsl:text>
        <xsl:value-of select="$feature-id"/>
        <xsl:text>]
; id=</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>
Type=</xsl:text>
        <xsl:variable name="type" select="@type"/>
        <xsl:for-each select="$mapping">
            <xsl:value-of select="key($current, $type)/@garmin_id"/>
        </xsl:for-each>

        <xsl:if test="@endlevel!=''">
            <xsl:text>
EndLevel=</xsl:text>
            <xsl:value-of select="@endlevel"/>
        </xsl:if>

        <xsl:if test="label">
            <xsl:text>
Label=</xsl:text>
            <xsl:value-of select="translate(label, 'abcdefghijklmnopqrstuvwxyzöüä', 'ABCDEFGHIJKLMNOPQRSTUVWXYZÖÜÄ')"/>
        </xsl:if>

        <xsl:text>
Data0=</xsl:text>
        <xsl:apply-templates select="data[@level=0]" mode="data"/>
        <xsl:text>
[END-</xsl:text>
        <xsl:value-of select="$feature-id"/>
        <xsl:text>]

</xsl:text>

    </xsl:template>

    <xsl:template match="data" mode="data">
        <xsl:for-each select="segment">
            <xsl:if test="position()!=1">
                <xsl:text>,</xsl:text> 
            </xsl:if>
            <xsl:text>(</xsl:text> 
            <xsl:value-of select="@from"/>
            <xsl:text>),(</xsl:text> 
            <xsl:value-of select="@to"/>
            <xsl:text>)</xsl:text> 
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
