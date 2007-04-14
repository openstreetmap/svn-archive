<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Calculate bounding box, size of map etc. -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Calculate the size of the bounding box based on the file content -->

    <xsl:variable name="bllat">
        <xsl:for-each select="$data/osm/node/@lat">
            <xsl:sort data-type="number" order="ascending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="bllon">
        <xsl:for-each select="$data/osm/node/@lon">
            <xsl:sort data-type="number" order="ascending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="trlat">
        <xsl:for-each select="$data/osm/node/@lat">
            <xsl:sort data-type="number" order="descending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="trlon">
        <xsl:for-each select="$data/osm/node/@lon">
            <xsl:sort data-type="number" order="descending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="bottomLeftLatitude">
        <xsl:choose>
            <xsl:when test='/rules/bounds'>
                <xsl:value-of select='/rules/bounds/@minlat'/>
            </xsl:when>
            <xsl:when test='$data/osm/bounds'>
                <xsl:value-of select='$data/osm/bounds/@request_minlat'/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='$bllat'/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="bottomLeftLongitude">
        <xsl:choose>
            <xsl:when test='/rules/bounds'>
                <xsl:value-of select='/rules/bounds/@minlon'/>
            </xsl:when>
            <xsl:when test='$data/osm/bounds'>
                <xsl:value-of select='$data/osm/bounds/@request_minlon'/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='$bllon'/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="topRightLatitude">
        <xsl:choose>
            <xsl:when test='/rules/bounds'>
                <xsl:value-of select='/rules/bounds/@maxlat'/>
            </xsl:when>
            <xsl:when test='$data/osm/bounds'>
                <xsl:value-of select='$data/osm/bounds/@request_maxlat'/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='$trlat'/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="topRightLongitude">
        <xsl:choose>
            <xsl:when test='/rules/bounds'>
                <xsl:value-of select='/rules/bounds/@maxlon'/>
            </xsl:when>
            <xsl:when test='$data/osm/bounds'>
                <xsl:value-of select='$data/osm/bounds/@request_maxlon'/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='$trlon'/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- Derive the latitude of the middle of the map -->
    <xsl:variable name='middleLatitude' select='($topRightLatitude + $bottomLeftLatitude) div 2.0'/>
    <!--woohoo lets do trigonometry in xslt -->
    <!--convert latitude to radians -->
    <xsl:variable name='latr' select='$middleLatitude * 3.1415926 div 180.0' />
    <!--taylor series: two terms is 1% error at lat<68 and 10% error lat<83. we probably need polar projection by then -->
    <xsl:variable name='coslat' select='1 - ($latr * $latr) div 2 + ($latr * $latr * $latr * $latr) div 24' />
    <xsl:variable name='projection' select='1 div $coslat' />

    <xsl:variable name='dataWidth' select='(number($topRightLongitude)-number($bottomLeftLongitude))*10000*$scale' />
    <xsl:variable name='dataHeight' select='(number($topRightLatitude)-number($bottomLeftLatitude))*10000*$scale*$projection' />
    <xsl:variable name='km' select='(0.0089928*$scale*10000*$projection)' />
    <xsl:variable name='documentWidth'>
        <xsl:choose>
            <xsl:when test='$dataWidth &gt; (number(/rules/@minimumMapWidth) * $km)'>
                <xsl:value-of select='$dataWidth'/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select='number(/rules/@minimumMapWidth) * $km'/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name='documentHeight'>
        <xsl:choose>
            <xsl:when test='$dataHeight &gt; (number(/rules/@minimumMapHeight) * $km)'>
                <xsl:value-of select='$dataHeight'/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select='number(/rules/@minimumMapHeight) * $km'/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name='width' select='($documentWidth div 2) + ($dataWidth div 2)'/>
    <xsl:variable name='height' select='($documentHeight div 2) + ($dataHeight div 2)'/>

</xsl:stylesheet>
