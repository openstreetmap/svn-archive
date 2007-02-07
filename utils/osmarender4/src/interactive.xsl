<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Draw zoom controls -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name='zoomControl'>
        <defs>

            <style type='text/css'>
              .fancyButton {
                stroke: #8080ff;
                stroke-width: 2px;
                fill: #fefefe;
                }
              .fancyButton:hover {
                stroke: red;
                }
            </style>

            <filter id="fancyButton" filterUnits="userSpaceOnUse" x="0" y="0" width="200" height="350">
                <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
                <feOffset in="blur" dx="2" dy="2" result="offsetBlur"/>
                <feSpecularLighting in="blur" surfaceScale="5" specularConstant=".75"
                          specularExponent="20" lighting-color="white"
                          result="specOut">
                    <fePointLight x="-5000" y="-10000" z="7000"/>
                </feSpecularLighting>
                <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut"/>
                <feComposite in="SourceGraphic" in2="specOut" operator="arithmetic"
                   k1="0" k2="1" k3="1" k4="0" result="litPaint"/>
                <feMerge>
                    <feMergeNode in="offsetBlur"/>
                    <feMergeNode in="litPaint"/>
                </feMerge>
            </filter>
            <symbol id="panDown" viewBox="0 0 19 19" class='fancyButton'>
                <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
                <path d="M 9.5,5 L 9.5,14"/>
            </symbol>
            <symbol id="panUp" viewBox="0 0 19 19" class='fancyButton'>
                <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
                <path d="M 9.5,5 L 9.5,14"/>
            </symbol>
            <symbol id="panLeft" viewBox="0 0 19 19" class='fancyButton'>
                <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
                <path d="M 5,9.5 L 14,9.5"/>
            </symbol>
            <symbol id="panRight" viewBox="0 0 19 19" class='fancyButton'>
                <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
                <path d="M 5,9.5 L 14,9.5"/>
            </symbol>
            <symbol id="zoomIn" viewBox="0 0 19 19" class='fancyButton'>
                <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
                <path d="M 5,9.5 L 14,9.5 M 9.5,5 L 9.5,14"/>
            </symbol>
            <symbol id="zoomOut" viewBox="0 0 19 19" class='fancyButton'>
                <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
                <path d="M 5,9.5 L 14,9.5"/>
            </symbol>

        </defs>

        <g id='gPanDown' filter='url(#fancyButton)' onclick='fnPan("down")'>
            <use x="18px" y="60px" xlink:href="#panDown" width='14px' height='14px' />
        </g>
        <g id='gPanRight' filter='url(#fancyButton)' onclick='fnPan("right")'>
            <use x="8px" y="70px" xlink:href="#panRight" width='14px' height='14px' />
        </g>
        <g id='gPanLeft' filter='url(#fancyButton)' onclick='fnPan("left")'>
            <use x="28px" y="70px" xlink:href="#panLeft" width='14px' height='14px' />
        </g>
        <g id='gPanUp' filter='url(#fancyButton)' onclick='fnPan("up")'>
            <use x="18px" y="80px" xlink:href="#panUp" width='14px' height='14px' />
        </g>

        <xsl:variable name='x1' select='25' />
        <xsl:variable name='y1' select='105'/>
        <xsl:variable name='x2' select='25'/>
        <xsl:variable name='y2' select='300'/>

        <line style="stroke-width: 10; stroke-linecap: butt; stroke: #8080ff;">
            <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
            <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
            <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
            <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
        </line>

        <line style="stroke-width: 8; stroke-linecap: butt; stroke: white; stroke-dasharray: 10,1;">
            <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
            <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
            <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
            <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
        </line>

        <!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
        <g id='gZoomIn' filter='url(#fancyButton)' onmousedown='fnZoom("in")'>
            <use x="15.5px" y="100px" xlink:href="#zoomIn" width='19px' height='19px'/>
        </g>

        <!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
        <g id='gZoomOut' filter='url(#fancyButton)' onmousedown='fnZoom("out")'>
            <use x="15.5px" y="288px" xlink:href="#zoomOut" width='19px' height='19px' />
        </g>
    </xsl:template>

    <xsl:template name='javaScript'>
        <script>
            <xi:include href="interactive.js" parse="text"/>
        </script>
    </xsl:template>

</xsl:stylesheet>
