<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp ' '> ]>

<!-- Osmarender.xsl 2.0 -->

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
     2.0 2006-07-07 Implements <area> instruction for areas and ways
                    Fix bug to enable stroke-linecap="butt"
          Implements e attribute for rules, allowing selection by element type
                    Implements v="*" for rules
                    Implements k="*" for rules
                    Implements e="node|segment|way|area" for rules
                    Implements v="rag|tag|bobtail" for rules
                    Implements k="rag|tag|bobtail" for rules
          Generates progress message as each rule is processed
          Elements with tags that have a key starting with svg: will be applied to the corresponding rendered element (TODO: test)
          Use of width key (eg <tag k="width" v="5px"/>) desupported in favour of svg:stroke-width (eg <tag k="svg:stroke-width" v="5px"/>
          Use of x-offset and y-offset attributes desupported in favour of dx and dy for <text> instructions and transform='translate(x,y)'
            for <symbol> instructions.
          Implements name_direction='-1' tag on segments and ways to draw street names in the reverse direction.
          Use of <textPath> instruction desupported in favour of <text> instruction which now does the right thing for both segments and ways.        
          Copyright and attribution captions dynamically re-positioned top-left.   
                    TODO: use paths for drawing segments
                    TODO: remove redundant Move to instructions
                    TODO: remove spurious output
                    TODO: test with inkscape etc
-->

<!-- Osmarender rules files contain two kinds of element; rules and instructions.  Rule elements provide a
     simple selection mechanism.  Instructions define what to do with the elements that match the rules. 
     
     Rules are simple filters based on elements, keys and values (the e, k and v attributes).  For example:
      <rule e="way" k="highway" v="motorway">...</rule> 
     will select all ways that have a key of highway with a value of motorway.
     Rules can be nested to provide a logical "and".  For example:
       <rule k="highway" v="primary">
         <rule k="abutters" v="retail">
          ...
         </rule>
       </rule>
     would select all segments that are primary roads *and* abutted by retail premises. 

   Each filter attribute can contain a | separated list of values.  For example e="segment|way" will match all segments and all ways.  
   k="highway|waterway" will match all elements with a key of highway or waterway. v="primary|secondary" will match all elements that
   have key values equal to primary or secondary.
       
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
 
  <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

  <xsl:key name='nodeById' match='/osm/node' use='@id'/>
  <xsl:key name='segmentById' match='/osm/segment' use='@id'/>
  <xsl:key name='segmentByFromNode' match='/osm/segment' use='@from'/>
  <xsl:key name='segmentByToNode' match='/osm/segment' use='@to'/>

  <xsl:variable name='data' select='document(/rules/@data)'/>

  <!-- Automatically calculate the size of the bounding box -->
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
  

  <xsl:variable name='scale' select='/rules/@scale'/>
  <xsl:variable name='projection' select='/rules/@projection'/>
  <xsl:variable name='width' select='(number($trlon)-number($bllon))*10000*$scale' />
  <xsl:variable name='height' select='(number($trlat)-number($bllat))*10000*$scale*$projection' />


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
     onscroll='fnResize()'
     onzoom='fnResize()'
     onresize='fnResize()'>    

      <xsl:call-template name='javaScript'/>

      <defs>        
        <!-- Get any <defs> and styles from the rules file -->
        <xsl:copy-of select='defs/*'/>
      </defs>

      <!-- Process all the rules, one layer at a time -->
      <!--<xsl:variable name='allElements' select='$data/osm/*[tag[@k="elevation" and @v="-1"]]' />
       <xsl:apply-templates select='/rules/rule'>
         <xsl:with-param name='elements' select='$allElements' />
       </xsl:apply-templates>
      <xsl:variable name='allElements' select='$data/osm/*[count(tag[@k="elevation"])=0]' />
       <xsl:apply-templates select='/rules/rule'>
         <xsl:with-param name='elements' select='$allElements' />
       </xsl:apply-templates>
      <xsl:variable name='allElements' select='$data/osm/*[tag[@k="elevation" and @v="1"]]' />
       <xsl:apply-templates select='/rules/rule'>
         <xsl:with-param name='elements' select='$allElements' />
       </xsl:apply-templates>-->

      <!-- Pre-generate named path definitions for all ways -->
      <xsl:variable name='allWays' select='$data/osm/way' />
      <defs>
        <xsl:for-each select='$allWays'>
          <xsl:call-template name='generateWayPath'/>
        </xsl:for-each>
      </defs>

      <!-- Apply the rules to all elements -->
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

    <!-- Make list of elements that this rule should be applied to -->
    <xsl:variable name='eBare'>
      <xsl:choose>
        <xsl:when test='$rule/@e="*"'>node|segment|way|area</xsl:when>
        <xsl:when test='$rule/@e'><xsl:value-of select='$rule/@e'/></xsl:when>
        <xsl:otherwise>node|segment|way|area</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- List of keys that this rule should be applied to -->
    <xsl:variable name='kBare' select='$rule/@k' />

    <!-- List of values that this rule should be applied to -->
    <xsl:variable name='vBare' select='$rule/@v' />

    <!-- Top'n'tail selectors with | for contains usage -->
    <xsl:variable name='e'>|<xsl:value-of select='$eBare'/>|</xsl:variable>
    <xsl:variable name='k'>|<xsl:value-of select='$kBare'/>|</xsl:variable>
    <xsl:variable name='v'>|<xsl:value-of select='$vBare'/>|</xsl:variable>

    <xsl:message>
Processing &lt;rule e="<xsl:value-of select='$eBare'/>" k="<xsl:value-of select='$kBare'/>" v="<xsl:value-of select='$vBare'/>" &gt;    
    </xsl:message>

    <xsl:choose>
      <xsl:when test='contains($k,"|*|")'>
        <xsl:choose>
          <xsl:when test='contains($v,"|~|")'>
            <xsl:variable name='elementsWithNoTags' select='$elements[contains($e,name()) and count(tag)=0]'/>
            <xsl:apply-templates select='*'>
              <xsl:with-param name='elements' select='$elementsWithNoTags' />
              <xsl:with-param name='rule' select='$rule'/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test='contains($v,"|*|")'>
            <xsl:variable name='allElements' select='$elements[contains($e,name())]'/>
            <xsl:apply-templates select='*'>
              <xsl:with-param name='elements' select='$allElements' />
              <xsl:with-param name='rule' select='$rule'/>
            </xsl:apply-templates>          
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name='allElementsWithValue' select='$elements[contains($e,name()) and tag[contains($v,concat("|",@v,"|"))]]'/>
            <xsl:apply-templates select='*'>
              <xsl:with-param name='elements' select='$allElementsWithValue' />
              <xsl:with-param name='rule' select='$rule'/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test='contains($v,"|~|")'>
        <xsl:variable name='elementsWithoutKey' select='$elements[contains($e,name()) and count(tag[contains($k,concat("|",@k,"|"))])=0]'/>
        <xsl:apply-templates select='*'>
          <xsl:with-param name='elements' select='$elementsWithoutKey' />
          <xsl:with-param name='rule' select='$rule'/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test='contains($v,"|*|")'>
        <xsl:variable name='allElementsWithKey' select='$elements[contains($e,name()) and tag[contains($k,concat("|",@k,"|"))]]'/>
        <xsl:apply-templates select='*'>
          <xsl:with-param name='elements' select='$allElementsWithKey' />
          <xsl:with-param name='rule' select='$rule'/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name='elementsWithKey' select='$elements[contains($e,name()) and tag[contains($k,concat("|",@k,"|")) and contains($v,concat("|",@v,"|"))]]'/>
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
      <xsl:apply-templates select='@*' mode='copySvg'/> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

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
    
    <xsl:call-template name='drawWay'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='way' select='$way'/>
    </xsl:call-template>

    <!--<xsl:for-each select='seg'>
      <xsl:variable name='segmentId' select='@id'/>
      <xsl:for-each select='key("segmentById",$segmentId)'>
        <xsl:call-template name='drawLine'>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name='segment' select='.'/>
          <xsl:with-param name='way' select='$way'/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>-->

  </xsl:template>
  

  <!-- Process an <area> instruction -->
  <xsl:template match='area'>
    <xsl:param name='elements' />

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name='instruction' select='.'/>

    <g>
      <xsl:apply-templates select='@*' mode='copySvg'/> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

      <!-- For each segment and way -->
      <xsl:apply-templates select='$elements' mode='area'>
        <xsl:with-param name='instruction' select='$instruction' />
      </xsl:apply-templates>

    </g>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match='*' mode='area'/>
  
  
  <!-- Draw area for a <way> or an <area> -->
  <xsl:template match='way|area' mode='area'>
    <xsl:param name='instruction' />

    <xsl:call-template name='generateAreaPath' />

    <xsl:call-template name='renderArea'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='pathId' select='concat("area_",@id)'/>
    </xsl:call-template>

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

    <!-- Select all <segment> and <way> elements that have a key that matches the k attribute of the text instruction -->
    <xsl:apply-templates select='$elements[name()="segment" or name()="way"][tag[@k=$instruction/@k]]' mode='textPath'>
      <xsl:with-param name='instruction' select='$instruction' />
    </xsl:apply-templates>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match='*' mode='textPath'/>


  <!-- Render textPaths for a segment -->
  <xsl:template match='segment' mode='textPath'>
    <xsl:param name='instruction' />
    
    <!-- The current <segment> element -->
    <xsl:variable name='segment' select='.' />

    <!-- Generate the path for the segment -->
    <!-- Text on segments should be relatively uncommon so only generate a <path> when one is needed -->
    <xsl:call-template name='generateSegmentPath' />

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

    <xsl:call-template name='renderTextPath'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='pathId' select='concat("way_",@id)'/>
    </xsl:call-template>

  </xsl:template>


  <!-- Generate a way path for the current segment -->
  <xsl:template name='generateSegmentPath'>
    <xsl:variable name='pathData'>
      <xsl:choose>
        <xsl:when test='tag[@k="name_direction"]/@v="-1"'>
          <xsl:call-template name='segmentMoveToEnd'/>
          <xsl:call-template name='segmentLineToStart'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name='segmentMoveToStart'/>
          <xsl:call-template name='segmentLineToEnd'/>      
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <path id="segment_{@id}" d="{$pathData}"/>

  </xsl:template>


  <!-- Generate a way path for the current way element -->
  <xsl:template name='generateWayPath'>
    
    <!-- Generate the path for the way -->
    <xsl:variable name='pathData'>
      <xsl:choose>
        <xsl:when test='tag[@k="name_direction"]/@v="-1"'>
          <xsl:for-each select='seg'>
            <xsl:sort select='position()' data-type='number' order='descending'/>
            <xsl:variable name='segmentId' select='@id'/>
            <xsl:for-each select='key("segmentById",$segmentId)'>
                <xsl:call-template name='segmentMoveToEnd'/>        
                <xsl:call-template name='segmentLineToStart'/>
            </xsl:for-each>
          </xsl:for-each>        
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select='seg[key("segmentById",@id)]'>
            <xsl:variable name='segmentId' select='@id'/>
            <xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=key("segmentById",preceding-sibling::seg[1]/@id)/@to'/>
            <xsl:for-each select='key("segmentById",$segmentId)'>
              <xsl:if test='not($linkedSegment)'>
                <xsl:call-template name='segmentMoveToStart'/>        
              </xsl:if>
                <xsl:call-template name='segmentLineToEnd'/>              
            </xsl:for-each>
          </xsl:for-each>        
        </xsl:otherwise>      
      </xsl:choose>
    </xsl:variable>

    <path id="way_{@id}" d="{$pathData}"/>

  </xsl:template>


  <!-- Generate an area path for the current way or area element -->
  <xsl:template name='generateAreaPath'>

    <!-- Generate the path for the area -->
    <xsl:variable name='pathData'>
      <xsl:for-each select='seg[key("segmentById",@id)]'>
        <xsl:variable name='segmentId' select='@id'/>
        <xsl:variable name='segmentSequence' select='position()'/>
        <xsl:for-each select='key("segmentById",$segmentId)'>
            <xsl:if test='$segmentSequence=1'>
              <xsl:call-template name='segmentMoveToStart'/>        
            </xsl:if>
            <xsl:call-template name='segmentLineToEnd'/>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:text>Z</xsl:text>
    </xsl:variable>

    <path id="area_{@id}" d="{$pathData}"/>

  </xsl:template>


  <!-- Generate a MoveTo command for a segment start -->
  <xsl:template name='segmentMoveToStart'>
    <xsl:variable name='from' select='@from'/>
    <xsl:variable name='fromNode' select='key("nodeById",$from)'/>

    <xsl:variable name='x1' select='($width)-((($trlon)-($fromNode/@lon))*10000*$scale)' />
    <xsl:variable name='y1' select='($height)+((($bllat)-($fromNode/@lat))*10000*$scale*$projection)'/>
    <xsl:text>M</xsl:text>
    <xsl:value-of select='$x1'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='$y1'/>
  </xsl:template>

    
  <!-- Generate a LineTo command for a segment start -->
  <xsl:template name='segmentLineToStart'>
    <xsl:variable name='from' select='@from'/>
    <xsl:variable name='fromNode' select='key("nodeById",$from)'/>

    <xsl:variable name='x1' select='($width)-((($trlon)-($fromNode/@lon))*10000*$scale)' />
    <xsl:variable name='y1' select='($height)+((($bllat)-($fromNode/@lat))*10000*$scale*$projection)'/>
    <xsl:text>L</xsl:text>
    <xsl:value-of select='$x1'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='$y1'/>
  </xsl:template>


  <!-- Generate a MoveTo command for a segment end -->
  <xsl:template name='segmentMoveToEnd'>
    <xsl:variable name='to' select='@to'/>
    <xsl:variable name='toNode' select='key("nodeById",$to)'/>

    <xsl:variable name='x2' select='($width)-((($trlon)-($toNode/@lon))*10000*$scale)'/>
    <xsl:variable name='y2' select='($height)+((($bllat)-($toNode/@lat))*10000*$scale*$projection)'/>
    <xsl:text>M</xsl:text>
    <xsl:value-of select='$x2'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='$y2'/>
  </xsl:template>

  <!-- Generate a LineTo command for a segment end -->
  <xsl:template name='segmentLineToEnd'>
    <xsl:variable name='to' select='@to'/>
    <xsl:variable name='toNode' select='key("nodeById",$to)'/>

    <xsl:variable name='x2' select='($width)-((($trlon)-($toNode/@lon))*10000*$scale)'/>
    <xsl:variable name='y2' select='($height)+((($bllat)-($toNode/@lat))*10000*$scale*$projection)'/>
    <xsl:text>L</xsl:text>
    <xsl:value-of select='$x2'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='$y2'/>
  </xsl:template>
  
  
  <!-- ============================================================================= -->
  <!-- Drawing templates                                                             -->
  <!-- ============================================================================= -->

  <!-- Draw a line for the current <segment> element using the formatting of the current <line> instruction -->
  <!-- Should do something like the following for drawing ways:
      <g stroke='red' stroke-width='0.8' stroke-linejoin='round' stroke-linecap='butt' fill='none'>
     <path id='test1' d="M6,9 L10,10 L10,20 L11,23 L12,23 L14,25 L20,3 "/>
    </g>
  -->
  
  <xsl:template name='drawLine'>
    <xsl:param name='instruction'/>
    <xsl:param name='segment'/> <!-- The current segment element -->
    <xsl:param name='way'/>  <!-- The current way element if applicable -->

    <xsl:variable name='from' select='@from'/>
    <xsl:variable name='to' select='@to'/>
    <xsl:variable name='fromNode' select='key("nodeById",$from)'/>
    <xsl:variable name='toNode' select='key("nodeById",$to)'/>
    <xsl:variable name='fromNodeContinuation' select='(count(key("segmentByFromNode",$fromNode/@id))+count(key("segmentByToNode",$fromNode/@id)))>1' />
    <xsl:variable name='toNodeContinuation' select='(count(key("segmentByFromNode",$toNode/@id))+count(key("segmentByToNode",$toNode/@id)))>1' />

    <xsl:variable name='x1' select='($width)-((($trlon)-($fromNode/@lon))*10000*$scale)' />
    <xsl:variable name='y1' select='($height)+((($bllat)-($fromNode/@lat))*10000*$scale*$projection)' />
    <xsl:variable name='x2' select='($width)-((($trlon)-($toNode/@lon))*10000*$scale)' />
    <xsl:variable name='y2' select='($height)+((($bllat)-($toNode/@lat))*10000*$scale*$projection)' />
    
    <line>
      <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
      <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
      <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
      <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
      <xsl:call-template name='getSvgAttributesFromOsmTags'/>
    </line>

    <!-- If this is not the end of a path then draw a half length line with a rounded linecap at the from-node end -->
    <xsl:if test='$fromNodeContinuation'>
      <line>
        <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
        <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
        <xsl:attribute name='x2'><xsl:value-of select='number($x1)+((number($x2)-number($x1)) div 2)'/></xsl:attribute>
        <xsl:attribute name='y2'><xsl:value-of select='number($y1)+((number($y2)-number($y1)) div 2)'/></xsl:attribute>
        <!-- add the rounded linecap attribute --> 
        <xsl:attribute name='stroke-linecap'>round</xsl:attribute>
        <!-- suppress any markers else these could be drawn in the wrong place -->
        <xsl:attribute name='marker-start'>none</xsl:attribute>
        <xsl:attribute name='marker-end'>none</xsl:attribute>
        <xsl:call-template name='getSvgAttributesFromOsmTags'/>
      </line>
    </xsl:if>

    <!-- If this is not the end of a path then draw a half length line with a rounded linecap at the to-node end -->
    <xsl:if test='$toNodeContinuation'>
      <line>
        <xsl:attribute name='x1'><xsl:value-of select='number($x1)+((number($x2)-number($x1)) div 2)'/></xsl:attribute>
        <xsl:attribute name='y1'><xsl:value-of select='number($y1)+((number($y2)-number($y1)) div 2)'/></xsl:attribute>
        <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
        <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
        <!-- add the rounded linecap attribute --> 
        <xsl:attribute name='stroke-linecap'>round</xsl:attribute>
        <!-- suppress any markers else these could be drawn in the wrong place -->
        <xsl:attribute name='marker-start'>none</xsl:attribute>
        <xsl:attribute name='marker-end'>none</xsl:attribute>
        <xsl:call-template name='getSvgAttributesFromOsmTags'/>
      </line>
    </xsl:if>
  </xsl:template>


  <!-- Draw a line for the current <way> element using the formatting of the current <line> instruction -->
  <!-- Should do something like the following for drawing ways:
      <g stroke='red' stroke-width='0.8' stroke-linejoin='round' stroke-linecap='butt' fill='none'>
     <path id='test1' d="M6,9 L10,10 L10,20 L11,23 L12,23 L14,25 L20,3 "/>
    </g>
    The problem here is that discontinuous ways show up as cracks in the road.  Drawing circles at nodes will have the one-way problem, and
    may be wasteful.
  -->
  
  <xsl:template name='drawWay'>
    <xsl:param name='instruction'/>
    <xsl:param name='way'/>  <!-- The current way element if applicable -->

    <use xlink:href='#way_{$way/@id}'/>
    
    <!--<xsl:variable name='from' select='@from'/>
    <xsl:variable name='to' select='@to'/>
    <xsl:variable name='fromNode' select='key("nodeById",$from)'/>
    <xsl:variable name='toNode' select='key("nodeById",$to)'/>
    <xsl:variable name='fromNodeContinuation' select='(count(key("segmentByFromNode",$fromNode/@id))+count(key("segmentByToNode",$fromNode/@id)))>1' />
    <xsl:variable name='toNodeContinuation' select='(count(key("segmentByFromNode",$toNode/@id))+count(key("segmentByToNode",$toNode/@id)))>1' />

    <xsl:variable name='x1' select='($width)-((($trlon)-($fromNode/@lon))*10000*$scale)' />
    <xsl:variable name='y1' select='($height)+((($bllat)-($fromNode/@lat))*10000*$scale*$projection)' />
    <xsl:variable name='x2' select='($width)-((($trlon)-($toNode/@lon))*10000*$scale)' />
    <xsl:variable name='y2' select='($height)+((($bllat)-($toNode/@lat))*10000*$scale*$projection)' />
    
    <line>
      <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
      <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
      <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
      <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
      <xsl:call-template name='getSvgAttributesFromOsmTags'/>
    </line>
    -->

    <!-- If this is not the end of a path then draw a zero length line with a rounded linecap at the from-node end -->
    <!-- Firefox does not render 0 length lines, whereas Adobe and Batik do.  Maybe could generate a circle for
         the benefit of Firefox here
         NB this is a problem if the class has a marker, it gets drawn twice once properly and once randomly -->
    <!--<xsl:if test='$fromNodeContinuation'>
      <line>
        <xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
        <xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
        <xsl:attribute name='x2'><xsl:value-of select='$x1'/></xsl:attribute>
        <xsl:attribute name='y2'><xsl:value-of select='$y1'/></xsl:attribute>
        -->
        <!-- add the rounded linecap attribute --> 
        <!--<xsl:attribute name='stroke-linecap'>round</xsl:attribute>
        <xsl:call-template name='getSvgAttributesFromOsmTags'/>
      </line>
    </xsl:if>
    -->
    
    <!-- If this is not the end of a path then draw a zero length line with a rounded linecap at the to-node end -->
    <!--<xsl:if test='$toNodeContinuation'>
      <line>
        <xsl:attribute name='x1'><xsl:value-of select='$x2'/></xsl:attribute>
        <xsl:attribute name='y1'><xsl:value-of select='$y2'/></xsl:attribute>
        <xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
        <xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
        -->
        <!-- add the rounded linecap attribute --> 
        <!--<xsl:attribute name='stroke-linecap'>round</xsl:attribute>
        <xsl:call-template name='getSvgAttributesFromOsmTags'/>
      </line>
    </xsl:if>
    -->
  </xsl:template>



  <!-- Draw a circle for the current <node> element using the formatting of the current <circle> instruction -->
  <xsl:template name='drawCircle'>
    <xsl:param name='instruction'/>

    <xsl:variable name='x' select='($width)-((($trlon)-(@lon))*10000*$scale)' />
    <xsl:variable name='y' select='($height)+((($bllat)-(@lat))*10000*$scale*$projection)'/>

    <circle r='1' cx='{$x}' cy='{$y}'>
      <xsl:apply-templates select='$instruction/@*' mode='copySvg' /> <!-- Copy all the svg attributes from the <circle> instruction -->    
    </circle>
    
  </xsl:template>

  
  <!-- Draw a symbol for the current <node> element using the formatting of the current <symbol> instruction -->
  <xsl:template name='drawSymbol'>
    <xsl:param name='instruction'/>

    <xsl:variable name='x' select='($width)-((($trlon)-(@lon))*10000*$scale)' />
    <xsl:variable name='y' select='($height)+((($bllat)-(@lat))*10000*$scale*$projection)'/>

    <use x='{$x}' y='{$y}'>
      <xsl:apply-templates select='$instruction/@*' mode='copySvg'/> <!-- Copy all the attributes from the <circle> instruction -->    
    </use>
    
  </xsl:template>


  <!-- Render the appropriate attribute of the current <node> element using the formatting of the current <text> instruction -->
  <xsl:template name='renderText'>
    <xsl:param name='instruction'/>
    
    <xsl:variable name='x' select='($width)-((($trlon)-(@lon))*10000*$scale)' />
    <xsl:variable name='y' select='($height)+((($bllat)-(@lat))*10000*$scale*$projection)'/>

    <text>
      <xsl:apply-templates select='$instruction/@*' mode='copySvg'/>    
      <xsl:attribute name='x'><xsl:value-of select='$x'/></xsl:attribute>
      <xsl:attribute name='y'><xsl:value-of select='$y'/></xsl:attribute>
      <xsl:call-template name='getSvgAttributesFromOsmTags'/>
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
        <xsl:call-template name='getSvgAttributesFromOsmTags'/>
        <xsl:value-of select='tag[@k=$instruction/@k]/@v'/>
      </textPath>
    </text>
  </xsl:template>


  <!-- Suppress the following attributes, allow everything else -->
  <xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength|@k" mode='renderTextPath-text'>
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


  <!-- Render the appropriate attribute of the current <way> element using the formatting of the current <area> instruction -->
  <xsl:template name='renderArea'>
    <xsl:param name='instruction'/>
    <xsl:param name='pathId'/>
    
    <use xlink:href="#{$pathId}">
      <xsl:apply-templates select='$instruction/@*' mode='copySvg' />
    </use>
  </xsl:template>


  <!-- Copy all attributes except osma:* -->
  <xsl:template match='@*' mode='copySvg'>
    <xsl:copy/>
  </xsl:template>
  
  <!-- Suppress osma:* attributes -->
  <!--<xsl:template match='@osma:*' mode='copySvg'/>-->
  

  <!-- If there are any OSM tags like <tag k="svg:font-size" v="5"/> then add these as attributes of the svg output --> 
  <xsl:template name='getSvgAttributesFromOsmTags'>
    <xsl:for-each select='tag[contains(@k,"svg:")]'>
      <xsl:attribute name='{substring-after(@k,"svg:")}'><xsl:value-of select='@v'/></xsl:attribute>
    </xsl:for-each>  
  </xsl:template>
  
  
  <!-- ============================================================================= -->
  <!-- Fairly static stuff                                                           -->
  <!-- ============================================================================= -->

  <!-- Draw an approximate scale in the bottom left corner of the map -->
  <xsl:template name='scaleDraw'>
    <xsl:variable name='x1' select='round(($width)-((($trlon)-(number($bllon)))*10000*$scale))+20' />
    <xsl:variable name='y1' select='round(($height)+((($bllat)-(number($bllat)))*10000*$scale*$projection))-20'/>
    <xsl:variable name='x2' select='round(($width)-((($trlon)-(number($bllon)+0.0089928))*10000*$scale))+20'/>
    <xsl:variable name='y2' select='round(($height)+((($bllat)-(number($bllat)))*10000*$scale*$projection))-20'/>
    
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


  <!-- Draw the copyright and attribution details at the top of the map -->
  <xsl:template name='attribution'>
    <g id='gAttribution'>
      <a xlink:href='http:www.openstreetmap.org'>
        <image x="10" y="10" width="150px" height="50px"
                xlink:href="Osm_linkage.png">
          <title>Copyright OpenStreetMap 2006</title>
        </image>
        <text font-family='Verdana' font-size='8px' fill='black' x='10' y='70'>
        Copyright 2006, OpenStreetMap.org
        </text>
      </a>       
    </g>
  </xsl:template>
  

  <!-- Draw the license details at the bottom right of the map -->
  <xsl:template name='license'>
    <g id='gLicense'>
      <!--Creative Commons License-->
      <a xlink:href='http://creativecommons.org/licenses/by-sa/2.0/'>
        <image x="170" y="20" width="88px" height="31px"
                xlink:href="somerights20.png">
          <title>Creative Commons - Some Rights Reserved - Attribution-ShareAlike 2.0</title>
        </image>
        <text font-family='Verdana' font-size='8px' fill='black' x='170' y='60'>
        This work is licensed under a Creative
        </text>
        <text font-family='Verdana' font-size='8px' fill='black' x='170' y='70'>
        Commons Attribution-ShareAlike 2.0 License.
        </text>
      </a>       
    </g>
  </xsl:template>
  
  <xsl:template name='javaScript'>
    <script>
      function fnResize() {
        fnResizeElement("gAttribution")
        fnResizeElement("gLicense")
      }
    

      function fnResizeElement(e) {
        //
        var oSVG,scale,currentTranslateX,currentTranslateY,oe
        //
        oSVG=document.rootElement
        scale=1/oSVG.currentScale
        currentTranslateX=oSVG.currentTranslate.x
        currentTranslateY=oSVG.currentTranslate.y
        oe=document.getElementById(e)
        oe.setAttributeNS(null,"transform","scale("+scale+","+scale+") translate("+(-currentTranslateX)+","+(-currentTranslateY)+")")
      }


      function fnToggleImage(osmImage) {
        var xlink = 'http://www.w3.org/1999/xlink';
         ogThumbnail=document.getElementById('gThumbnail')
        if (ogThumbnail.getAttributeNS(null,"visibility")=="visible") fnHideImage()
        else {
          ogThumbnail.setAttributeNS(null,"visibility","visible")    
           oThumbnail=document.getElementById('thumbnail')
          oThumbnail.setAttributeNS(xlink,"href",osmImage)
        }
      }
    
      function fnHideImage() {
         ogThumbnail=document.getElementById('gThumbnail')
        ogThumbnail.setAttributeNS(null,"visibility","hidden")    
      }
    
    </script>  
  </xsl:template>

</xsl:stylesheet>
