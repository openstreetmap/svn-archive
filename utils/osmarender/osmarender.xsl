<?xml version='1.0' standalone='no'?>
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp '&#160;'> ]>

<!-- Osmarender.xsl 1.6 -->

<!-- Revision history:
     1.0 2006-03-21 Initial version
     1.1 2006-03-23 Remove <html> and <body> tags
     1.2 2006-03-24 Support for ways
     1.3 2006-04-10 width key will override line width
                    Implements nested rules
                    General restructuring
                    Implements <symbol> instruction
     1.4 2006-04-11 Implements <textPath> instruction for text on Segments and Ways
     1.5 2006-04-11 Fix bug that generated invalid xsl-stylesheet PI
                    Fix bug resulting in superflous white space output
                    Fix bug causing dy attribute on <textPath> element rather than <text> element
     1.6 2006-04-12 Fix bug with <text> instructions choking on <segment> and <way> elements in Batik 
-->

<!-- Osmarender rules files contain two kinds of element; rules and instructions.  Rule elements provide a
     simple selection mechanism.  Instructions define what to do with the elements that match the rules. 
     
     Rules are simple filters based on keys and values (the k and v attributes).  For example:
      <rule k="highway" v="motorway">...</rule> 
     will select all elements (segments, ways and nodes) that have a key of highway with a value of motorway.
     Rules can be nested to provide a logical "and".  For example:
       <rule k="highway" v="primary">
         <rule k="abutters" v="retail">
          ...
         </rule>
       </rule>
     would select all segments that are primary roads *and* abutted by retail premises. 
     
     Instructions define what to do with the elements that match the rules.  Typically, they render the element
     in some way by generating an svg command to draw a line or circle etc.  In most cases the attributes of
     the instruction are copied to the corresponding svg command.  For example:
       <line stroke-width="10"/> 
     will generate a corresponding svg command to draw a line with a width of 10px.
-->

<xsl:stylesheet 
 version="1.0"
 xmlns="http://www.w3.org/2000/svg"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:variable name='scale' select='/rules/@scale'/>
  <xsl:variable name='data' select='document(/rules/@data)'/>
  <xsl:key name='nodeById' match='/osm/node' use='@id'/>
  <xsl:key name='segmentById' match='/osm/segment' use='@id'/>

  <!-- Automatically calculate the size of the bounding box -->
  <xsl:variable name="trlat">
    <xsl:for-each select="$data/osm/node/@lat">
      <xsl:sort data-type="number" order="descending"/>
      <xsl:if test="position()=1">
        <xsl:value-of select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  <xsl:variable name="bllat">
    <xsl:for-each select="$data/osm/node/@lat">
      <xsl:sort data-type="number" order="ascending"/>
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
  <xsl:variable name="bllon">
    <xsl:for-each select="$data/osm/node/@lon">
      <xsl:sort data-type="number" order="ascending"/>
      <xsl:if test="position()=1">
        <xsl:value-of select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  
  <xsl:variable name='width' select='(number($trlon)-number($bllon))*10000*$scale' />
  <xsl:variable name='height' select='(number($trlat)-number($bllat))*10000*$scale*1.6' />

  <!-- Main template -->
  <xsl:template match="/rules">     

    <!-- Include an external css stylesheet if one was specified in the rules file -->
    <xsl:if test='@xml-stylesheet'>
      <xsl:processing-instruction name='xml-stylesheet'>
        href="<xsl:value-of select='@xml-stylesheet'/>" type="text/css"
      </xsl:processing-instruction>
    </xsl:if>

    <svg
     version="1.1"
     baseProfile="full"
     height="{$height}px"
     width="{$width}px">    

      <defs>
        
        <!-- Get any <defs> from the rules file -->
        <xsl:copy-of select='defs/*'/>
        
        <!-- Get CSS definitions from the rules file (deprecated) -->
        <style type='text/css'>
          <xsl:value-of select='style'/>
        </style>
      </defs>

      <!-- Process all the rules -->
      <xsl:variable name='allElements' select='$data/osm/*' />
       <xsl:apply-templates select='/rules/rule'>
         <xsl:with-param name='elements' select='$allElements' />
       </xsl:apply-templates>

      <!-- Draw the scale in the bottom left corner -->
      <xsl:call-template name="scaleDraw"/>

      <!-- Attribute to OSM -->
      <xsl:call-template name="attribution"/>

      <!-- Creative commons license -->
      <xsl:call-template name="license"/>
    </svg>

  </xsl:template>


  <!-- ============================================================================= -->
  <!-- Rule processing template                                                      -->
  <!-- ============================================================================= -->

  <!-- For each rule apply line, circle, text, etc templates.  Then apply the rule template recursively for each nested rule --> 
  <xsl:template match='rule'>
    <xsl:param name='elements' />

    <!-- This is the rule currently being processed -->
    <xsl:variable name='rule' select='.'/>

    <xsl:choose>
      <xsl:when test='$rule/@v="~"'>
        <xsl:variable name='elementsWithoutKey' select='$elements[count(tag[@k=$rule/@k])=0]'/>
        <xsl:apply-templates select='*'>
          <xsl:with-param name='elements' select='$elementsWithoutKey' />
          <xsl:with-param name='rule' select='$rule'/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name='elementsWithKey' select='$elements[tag[@k=$rule/@k and @v=$rule/@v]]'/>
        <xsl:apply-templates select='*'>
          <xsl:with-param name='elements' select='$elementsWithKey' />
          <xsl:with-param name='rule' select='$rule'/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- ============================================================================= -->
  <!-- Templates to process line, circle, text, etc instructions                     -->
  <!-- ============================================================================= -->
  <!-- Each template is passed a variable containing the set of elements that need to
       be processed.  The set of elements is already determined by the rules, so 
       these templates don't need to know anything about the rules context they are in. -->

  <!-- Process a <line> instruction -->
  <xsl:template match='line'>
    <xsl:param name='elements' />

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name='instruction' select='.'/>

    <g>
      <xsl:copy-of select='@*'/> <!-- Add all the attributes of the <line> instruction to the <g> element -->

      <!-- For each segment and way -->
      <xsl:apply-templates select='$elements' mode='line'>
        <xsl:with-param name='instruction' select='$instruction' />
      </xsl:apply-templates>

    </g>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match='*' mode='line'/>
  
  
  <!-- Draw lines for a segment -->
  <xsl:template match='segment' mode='line'>
    <xsl:param name='instruction' />

    <xsl:call-template name='drawLine'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='segment' select='.'/>
    </xsl:call-template>

  </xsl:template>


  <!-- Draw lines for a way (draw all the segments that belong to the way) -->
  <xsl:template match='way' mode='line'>
    <xsl:param name='instruction' />

    <!-- The current <way> element -->
    <xsl:variable name='way' select='.' />
    
    <xsl:for-each select='seg'>
      <xsl:variable name='segmentId' select='@id'/>
      <xsl:for-each select='key("segmentById",$segmentId)'>
        <xsl:call-template name='drawLine'>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name='segment' select='.'/>
          <xsl:with-param name='way' select='$way'/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>

  </xsl:template>
  

  <!-- Process circle instruction -->
  <xsl:template match='circle'>
    <xsl:param name='elements'/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name='instruction' select='.' />

    <xsl:for-each select='$elements[name()="node"]'>
      <xsl:call-template name='drawCircle'>
        <xsl:with-param name='instruction' select='$instruction'/>
      </xsl:call-template>          
    </xsl:for-each>
  </xsl:template>


  <!-- Process a symbol instruction -->
  <xsl:template match='symbol'>
    <xsl:param name='elements'/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name='instruction' select='.' />

    <xsl:for-each select='$elements[name()="node"]'>
      <xsl:call-template name='drawSymbol'>
        <xsl:with-param name='instruction' select='$instruction'/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>


  <!-- Process a <text> instruction -->
  <xsl:template match='text'>
    <xsl:param name='elements'/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name='instruction' select='.' />
    
    <!-- Select all <node> elements that have a key that matches the k attribute of the text instruction -->
    <xsl:for-each select='$elements[name()="node"][tag[@k=$instruction/@k]]'>
        <xsl:call-template name='renderText'>
          <xsl:with-param name='instruction' select='$instruction'/>
        </xsl:call-template>          
    </xsl:for-each>
  </xsl:template>


  <!-- Process a <textPath> instruction -->
  <xsl:template match='textPath'>
    <xsl:param name='elements'/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name='instruction' select='.' />
    
    <!-- Select all elements that have a key that matches the k attribute of the textPath instruction -->
    <xsl:apply-templates select='$elements[tag[@k=$instruction/@k]]' mode='textPath'>
      <xsl:with-param name='instruction' select='$instruction' />
    </xsl:apply-templates>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match='*' mode='textPath'/>


  <!-- Render textPaths for a segment -->
  <xsl:template match='segment' mode='textPath'>
    <xsl:param name='instruction' />

    <!-- Generate the path for the segment -->
    <xsl:variable name='pathData'>
      <xsl:call-template name='segmentPath'/>
    </xsl:variable>

    <defs>
      <path id="segment_{@id}" d="{$pathData}"/>
    </defs>

    <xsl:call-template name='renderTextPath'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='pathId' select='concat("segment_",@id)'/>
    </xsl:call-template>

  </xsl:template>


  <!-- Render textPaths for a way -->
  <xsl:template match='way' mode='textPath'>
    <xsl:param name='instruction' />

    <!-- The current <way> element -->
    <xsl:variable name='way' select='.' />
    
    <!-- Generate the path for the way -->
    <xsl:variable name='pathData'>
      <xsl:for-each select='seg'>
        <xsl:variable name='segmentId' select='@id'/>
        <xsl:for-each select='key("segmentById",$segmentId)'>
            <xsl:call-template name='segmentPath'/>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>

    <defs>
      <path id="way_{@id}" d="{$pathData}"/>
    </defs>

    <xsl:call-template name='renderTextPath'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='pathId' select='concat("way_",@id)'/>
    </xsl:call-template>

  </xsl:template>


  <!-- Return the path for a segment -->
  <xsl:template name='segmentPath'>
    <xsl:variable name='from' select='@from'/>
    <xsl:variable name='to' select='@to'/>
    <xsl:variable name='fromNode' select='key("nodeById",$from)'/>
    <xsl:variable name='toNode' select='key("nodeById",$to)'/>

    <xsl:variable name='x1' select='round(($width)-((($trlon)-($fromNode/@lon))*10000*$scale))' />
    <xsl:variable name='y1' select='round(($height)+((($bllat)-($fromNode/@lat))*10000*$scale*1.6))'/>
    <xsl:variable name='x2' select='round(($width)-((($trlon)-($toNode/@lon))*10000*$scale))'/>
    <xsl:variable name='y2' select='round(($height)+((($bllat)-($toNode/@lat))*10000*$scale*1.6))'/>
    <!-- Only really need the M for the first segment if the segments are contiguous -->
    <xsl:text>M</xsl:text>
    <xsl:value-of select='$x1'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='$y1'/>
    <xsl:text>L</xsl:text>
    <xsl:value-of select='$x2'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='$y2'/>
  </xsl:template>
    
  
  
  <!-- ============================================================================= -->
  <!-- Drawing templates                                                             -->
  <!-- ============================================================================= -->

  <!-- Draw a line for the current <segment> element using the formatting of the current <line> instruction -->
  <xsl:template name='drawLine'>
    <xsl:param name='instruction'/>
    <xsl:param name='segment'/> <!-- The current segment element -->
    <xsl:param name='way'/>  <!-- The current way element if applicable -->

    <xsl:variable name='from' select='@from'/>
    <xsl:variable name='to' select='@to'/>
    <xsl:variable name='fromNode' select='key("nodeById",$from)'/>
    <xsl:variable name='toNode' select='key("nodeById",$to)'/>

    <xsl:variable name='x1' select='round(($width)-((($trlon)-($fromNode/@lon))*10000*$scale))' />
    <xsl:variable name='y1' select='round(($height)+((($bllat)-($fromNode/@lat))*10000*$scale*1.6))'/>
    <xsl:variable name='x2' select='round(($width)-((($trlon)-($toNode/@lon))*10000*$scale))'/>
    <xsl:variable name='y2' select='round(($height)+((($bllat)-($toNode/@lat))*10000*$scale*1.6))'/>
    
    <line>
      <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
      <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
      <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
      <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
      <!-- If the current <segment> or <way> element has a width key then use it's value to override the stroke-width of the line -->
      <xsl:if test='$segment/tag[@k="width"]'>
        <xsl:attribute name='stroke-width'><xsl:value-of select='$segment/tag[@k="width"]/@v'/></xsl:attribute> 
      </xsl:if>
      <xsl:if test='$way'>
        <xsl:if test='$way/tag[@k="width"]'>
          <xsl:attribute name='stroke-width'><xsl:value-of select='$way/tag[@k="width"]/@v'/></xsl:attribute> 
        </xsl:if>
      </xsl:if>
    </line>

  </xsl:template>


  <!-- Draw a circle for the current <node> element using the formatting of the current <circle> instruction -->
  <xsl:template name='drawCircle'>
    <xsl:param name='instruction'/>

    <xsl:variable name='x' select='($width)-((($trlon)-(@lon))*10000*$scale)' />
    <xsl:variable name='y' select='($height)+((($bllat)-(@lat))*10000*$scale*1.6)'/>
    <xsl:variable name='x1'>
      <xsl:choose>
        <xsl:when test='$instruction/@x-offset'>
          <xsl:value-of select='number($x)+number($instruction/@x-offset)'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='number($x)'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name='y1'>
      <xsl:choose>
        <xsl:when test='$instruction/@y-offset'>
          <xsl:value-of select='number($y)+number($instruction/@y-offset)'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='number($y)'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <circle r='1' cx='{$x1}' cy='{$y1}'>
        <xsl:copy-of select='$instruction/@*'/> <!-- Copy all the attributes from the <circle> instruction -->    
    </circle>
    
  </xsl:template>


  <!-- Draw a symbol for the current <node> element using the formatting of the current <symbol> instruction -->
  <xsl:template name='drawSymbol'>
    <xsl:param name='instruction'/>

    <xsl:variable name='x' select='($width)-((($trlon)-(@lon))*10000*$scale)' />
    <xsl:variable name='y' select='($height)+((($bllat)-(@lat))*10000*$scale*1.6)'/>
    <xsl:variable name='x1'>
      <xsl:choose>
        <xsl:when test='$instruction/@x-offset'>
          <xsl:value-of select='number($x)+number($instruction/@x-offset)'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='number($x)'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name='y1'>
      <xsl:choose>
        <xsl:when test='$instruction/@y-offset'>
          <xsl:value-of select='number($y)+number($instruction/@y-offset)'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='number($y)'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <use x='{$x1}' y='{$y1}'>
        <xsl:copy-of select='$instruction/@*'/> <!-- Copy all the attributes from the <circle> instruction -->    
    </use>
    
  </xsl:template>


  <!-- Render the appropriate attribute of the current <node> element using the formatting of the current <text> instruction -->
  <xsl:template name='renderText'>
    <xsl:param name='instruction'/>
    
    <xsl:variable name='x' select='($width)-((($trlon)-(@lon))*10000*$scale)' />
    <xsl:variable name='y' select='($height)+((($bllat)-(@lat))*10000*$scale*1.6)'/>
    <xsl:variable name='x1'>
      <xsl:choose>
        <xsl:when test='$instruction/@x-offset'>
          <xsl:value-of select='number($x)+number($instruction/@x-offset)'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='number($x)'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name='y1'>
      <xsl:choose>
        <xsl:when test='$instruction/@y-offset'>
          <xsl:value-of select='number($y)+number($instruction/@y-offset)'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='number($y)'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <text>
      <xsl:copy-of select='$instruction/@*'/>    
      <xsl:attribute name='x'><xsl:value-of select='$x1'/></xsl:attribute>
      <xsl:attribute name='y'><xsl:value-of select='$y1'/></xsl:attribute>
      <xsl:value-of select='tag[@k=$instruction/@k]/@v'/>
    </text>
  </xsl:template>


  <!-- Render the appropriate attribute of the current <segment> element using the formatting of the current <textPath> instruction -->
  <xsl:template name='renderTextPath'>
    <xsl:param name='instruction'/>
    <xsl:param name='pathId'/>
    
    <text>
      <xsl:apply-templates select='$instruction/@*' mode='renderTextPath-text'/>
      <textPath xlink:href="#{$pathId}">
        <xsl:apply-templates select='$instruction/@*' mode='renderTextPath-textPath'/>
        <xsl:value-of select='tag[@k=$instruction/@k]/@v'/>
      </textPath>
    </text>
  </xsl:template>


  <!-- Suppress the following attributes, allow everything else -->
  <xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength" mode='renderTextPath-text'>
  </xsl:template>

  <xsl:template match="@*" mode='renderTextPath-text'>
    <xsl:copy/>
  </xsl:template>

  <!-- Allow the following attributes, suppress everything else -->
  <xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength" mode='renderTextPath-textPath'>
    <xsl:copy/>
  </xsl:template>

  <xsl:template match="@*" mode='renderTextPath-textPath'>
  </xsl:template>


  <!-- ============================================================================= -->
  <!-- Fairly static stuff                                                           -->
  <!-- ============================================================================= -->

  <!-- Draw an approximate scale in the bottom left corner of the map -->
  <xsl:template name='scaleDraw'>
    <xsl:variable name='x1' select='round(($width)-((($trlon)-(number($bllon)))*10000*$scale))+20' />
    <xsl:variable name='y1' select='round(($height)+((($bllat)-(number($bllat)))*10000*$scale*1.6))-20'/>
    <xsl:variable name='x2' select='round(($width)-((($trlon)-(number($bllon)+0.0089928))*10000*$scale))+20'/>
    <xsl:variable name='y2' select='round(($height)+((($bllat)-(number($bllat)))*10000*$scale*1.6))-20'/>
    
    <text font-family='Verdana' font-size='10px' fill='black'>
      <xsl:attribute name='x'><xsl:value-of select='$x1'/></xsl:attribute>
      <xsl:attribute name='y'><xsl:value-of select='number($y1)-10'/></xsl:attribute>
      0
    </text>

    <text font-family='Verdana' font-size='10px' fill='black'>
      <xsl:attribute name='x'><xsl:value-of select='$x2'/></xsl:attribute>
      <xsl:attribute name='y'><xsl:value-of select='number($y2)-10'/></xsl:attribute>
      1km
    </text>

    <line style="stroke-width: 4; stroke-linecap: butt; stroke: #000000;">
      <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
      <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
      <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
      <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
    </line>

    <line style="stroke-width: 1; stroke-linecap: butt; stroke: #000000;">
      <xsl:attribute name='x1'><xsl:value-of select='number($x1)'/></xsl:attribute>
      <xsl:attribute name='y1'><xsl:value-of select='number($y1)'/></xsl:attribute>
      <xsl:attribute name='x2'><xsl:value-of select='number($x1)'/></xsl:attribute>
      <xsl:attribute name='y2'><xsl:value-of select='number($y1)-10'/></xsl:attribute>
    </line>

    <line style="stroke-width: 1; stroke-linecap: butt; stroke: #000000;">
      <xsl:attribute name='x1'><xsl:value-of select='number($x2)'/></xsl:attribute>
      <xsl:attribute name='y1'><xsl:value-of select='number($y2)'/></xsl:attribute>
      <xsl:attribute name='x2'><xsl:value-of select='number($x2)'/></xsl:attribute>
      <xsl:attribute name='y2'><xsl:value-of select='number($y2)-10'/></xsl:attribute>
    </line>

  </xsl:template>


  <!-- Draw the copyright and attribution details at the bottom of the map -->
  <xsl:template name='attribution'>
    <a xlink:href='http:www.openstreetmap.org'>
      <image x="{number($width)-360}" y="{number($height)-70}" width="150px" height="50px"
              xlink:href="http://wiki.openstreetmap.org/images/8/86/Osm_linkage.png">
        <title>Copyright OpenStreetMap 2006</title>
      </image>
      <text font-family='Verdana' font-size='8px' fill='black' x='{number($width)-360}' y='{number($height)-10}'>
      Copyright 2006, OpenStreetMap.org
      </text>
    </a>       
  </xsl:template>
  

  <!-- Draw the license details at the bottom right of the map -->
  <xsl:template name='license'>
    <!--Creative Commons License-->
    <a xlink:href='http://creativecommons.org/licenses/by-sa/2.0/'>
      <image x="{number($width)-190}" y="{number($height)-60}" width="88px" height="31px"
              xlink:href="http://www.openstreetmap.org/images/somerights20.png">
        <title>Creative Commons - Some Rights Reserved - Attribution-ShareAlike 2.0</title>
      </image>
      <text font-family='Verdana' font-size='8px' fill='black' x='{number($width)-190}' y='{number($height)-20}'>
      This work is licensed under a Creative
      </text>
      <text font-family='Verdana' font-size='8px' fill='black' x='{number($width)-190}' y='{number($height)-10}'>
      Commons Attribution-ShareAlike 2.0 License.
      </text>
    </a>       
  </xsl:template>
  
</xsl:stylesheet>
