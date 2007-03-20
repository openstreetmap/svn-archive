#! /usr/bin/ruby
#
# OSMPS
#
# OpenStreetMap .osm to PostScript renderer
#
# Copyright (c) Matthew Newton, 2007
#
#-------------------------------------------------------------------------------
#   This program is free software; you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation; either version 2 of the License, or
#   (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program; if not, write to the Free Software
#   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#-------------------------------------------------------------------------------
#
#   As an additional exception, PostScript code contained within,
#   and output by, this program may be freely copied and modified
#   without restriction.
#
#   The PostScript pathtext code is based on Sample Code from the book
#     "PostScript Language Tutorial and Cookbook"
#   Copyright (c) Adobe Systems Inc. 1985 and is included in this
#   software within the terms of the Adobe licence found at
#   http://partners.adobe.com/public/developer/ps/eula_submit.jsp?eula_name=ps_eula
#
#-------------------------------------------------------------------------------
# This source is best viewed with a folding editor, such as Vim ;-)

require 'xml/libxml'

PSResource = <<EOR# {{{
%%BeginResource: osmps
/bd {bind def} bind def
/pathtomarkdict 1 dict def
/pathtomark {
% move all but first pair into array
  counttomark 2 sub array astore
% pull the first coords to the top (mark array x y)
  3 1 roll
% perform transformation, and moveto
  newpath
  exch x exch y
  moveto
% lineto for each other pair
  pathtomarkdict begin
  /xpos false def
  {
    xpos false eq {
      /xpos exch x def
    }
    { 
      y xpos exch
      lineto
      /xpos false def
    } ifelse
  } forall
  end
  pop
} def
/pathcopy { counttomark 1 add copy } def
/area { pathtomark fill } def
/line { pathtomark stroke } def
/cline { pathtomark closepath stroke } def
/c {
255 div 3 1 roll
255 div 3 1 roll
255 div 3 1 roll
setrgbcolor
} def
/lw {setlinewidth} bd
/gs {gsave} bd
/gr {grestore} bd
% node
/n { 0.2 0 360 arc fill } bd
% segment
/s { 4 2 roll moveto lineto } bd
% 0.3 setlinewidth
% 0.7 setgray

%------------------------------------------------------------
% Tweaked PathText code from Adobe PostScript Cookbook
/pathtextdict 26 dict def
/pathtext
  { pathtextdict begin
  /charbaseoffset 0.15 def
  /offset exch def
  /str exch def
  /pathdist 0 def
  /setdist offset def
  /charcount 0 def
  gsave
    flattenpath
    {movetoproc}  {linetoproc}
    {curvetoproc} {closepathproc}
    pathforall
  grestore
  newpath
  end
  } def
  
pathtextdict begin
/movetoproc
  { /newy exch def /newx exch def
  /firstx newx def /firsty newy def
  %/ovr 0 def   <- bug in Adobe's original code
  /ovr offset def
  newx newy transform
  /cpy exch def /cpx exch def
  } def
  
/linetoproc
  { /oldx newx def /oldy newy def
  /newy exch def /newx exch def
  /dx newx oldx sub def
  /dy newy oldy sub def
  /dist dx dup mul dy dup mul add sqrt def
  dist 0 ne
    { /dsx dx dist div ovr mul def
      /dsy dy dist div ovr mul def
      oldx dsx add oldy dsy add transform
      /cpy exch def /cpx exch def
      /pathdist pathdist dist add def
    { setdist pathdist le
      { charcount str length lt
      {setchar} {exit} ifelse }
      { /ovr setdist pathdist sub def
      exit }
      ifelse
    } loop
      } if
  } def
  
/curvetoproc
  { (ERROR: No curveto's after flattenpath!) print
  } def
  
/closepathproc
  { firstx firsty linetoproc
  firstx firsty movetoproc
  } def
  
/setchar
  { /char str charcount 1 getinterval def
  /charcount charcount 1 add def
  /charwidth char stringwidth pop def
  gsave
    cpx cpy itransform translate
    dy dx atan rotate
	  0 charbaseoffset neg moveto char show 0 charbaseoffset rmoveto
    currentpoint transform
    /cpy exch def /cpx exch def
  grestore
  /setdist setdist charwidth add def
  } def
end
% End of code derived from Adobe source
%------------------------------------------------------------

%------------------------------------------------------------
% Calculate the length of the current graphics path
% - pathlen <length>
%
/pathlendict 7 dict def
/pathlen
  { pathlendict begin
  /length 0 def
  gsave
    flattenpath
    {pathlenmt} {pathlenlt}
    {pathlenct} {pathlencp}
    pathforall
  grestore
  newpath
  length
  end
  } def

pathlendict begin
/pathlenmt
  { /newy exch def
    /newx exch def
    /firstx newx def
    /firsty newy def
  } def

/pathlenlt
  { /oldx newx def
    /oldy newy def
    /newy exch def
    /newx exch def
    /length newx oldx sub dup mul
            newy oldy sub dup mul
            add sqrt
            length add def
  } def

/pathlenct
  { (This should never happen!) print
  } def

/pathlencp
  { firstx firsty pathlenlt
  } def
end

%------------------------------------------------------------
% Draw in a road name, central to the given path, but only
% if it will actually fit.
% mark <x1> <y1> <x2> <y2> <xn> <yn> (text) roadname -
%
/roadnamedict 3 dict def
/roadname
  { roadnamedict begin
    /text exch def
    pathtomark
    gsave
    /plen pathlen def
    grestore
    /tlen text stringwidth pop def
    tlen 0.0 gt plen 0.0 gt and
    plen tlen gt and
    { gsave
      0 0 0 setrgbcolor
      text
      plen 2 div tlen 2 div sub
      pathtext
      grestore
    } if
    end
  } def

/Helvetica-Bold findfont 0.5 scalefont setfont

%%EndResource
EOR
# }}}
class Style# {{{
  def initialize# {{{
    @match = {}
    @style = ""
    @type = :path
    @drawps = {}
  end

# }}}
  def addtag(k, va)# {{{
    if va == nil
      @match[k] = nil
    end
    if not @match.has_key?(k)
      @match[k] = []
    end
    if @match[k] != nil
      arr = va
      if va.class == String
        arr = [va]
      end
      arr.each do |v|
        @match[k].push(v)
      end
    end
  end

# }}}
  def matchtags(tags)# {{{
    @match.keys.each do |m|
      if not tags.has_key?(m)
        return false
      end
      if @match[m] != nil
        if not @match[m].include?(tags[m])
          return false
        end
      end
    end
    true
  end

# }}}
  def setarea# {{{
    @type = :area
  end

# }}}
  def setpath# {{{
    @type = :path
  end

# }}}
  def setnode# {{{
    @type = :node
  end
  
# }}}
  def area?# {{{
    @type == :area
  end

# }}}
  def adddrawps(layer, ps)# {{{
    if not @drawps.has_key?(layer)
      @drawps[layer] = []
    end
    @drawps[layer].push(ps)
  end
  
# }}}
  def adddrawtagstring(layer, tagname)# {{{
    if not @drawps.has_key?(layer)
      @drawps[layer] = []
    end
    @drawps[layer].push("%tag #{tagname}")
  end
  
# }}}
  def layers# {{{
    @drawps.keys
  end
  
# }}}
  def length# {{{
    @drawps.length
  end

# }}}
  def ps(layer, tags)# {{{
  # return "" if layer is out of range
    if @drawps[layer] == nil
      return ""
    end
    ps = ""
    @drawps[layer].each do |d|
      s = d
      if d[0].chr == "%"
        cmd = d.split(" ")
        if cmd[0] == "%tag"
          s = tags.has_key?(cmd[1]) ? "(#{tags[cmd[1]]})" : "()"
        end
      end
      ps += s + "\n"
    end
#    @drawps[num].join(" ") + "\n"
    ps
  end

# }}}
  def to_s# {{{
    @drawps.join(" ") + "\n"
  end

# }}}
end #}}}
class Tags #{{{
  def initialize(tags)# {{{
    @tags = {}
    self.append(tags)
    @usecount = 0
    @styles = nil
    @area = false
  end

# }}}
  def append(tags)# {{{
    tags.keys.each do |t|
      @tags[t] = tags[t]
    end
  end

# }}}
  def tags# {{{
    @tags
  end

# }}}
  def length# {{{
    @tags.length
  end

# }}}
  def area?# {{{
    @area
  end

# }}}
  def renderprepare(styles)# {{{
    @styles = []
    styles.each do |s|
      if s.matchtags(@tags)
        @styles.push(s)
        if s.area?
          @area = true
        end
      end
    end
  end

# }}}
  def renderclear# {{{
    @styles = nil
  end

# }}}
  def renderpre# {{{
    "gs\n"
  end

# }}}
  def render# {{{
    ps = ""
    @styles.each do |s|
      ps += s.to_s
    end
    ps
  end

# }}}
  def renderlayers# {{{
    layers = {}
    @styles.each do |s|
      s.layers.each do |l|
        layers[l] = 1
      end
    end
    layers.keys
  end

# }}}
  def renderlayer(l)# {{{
    ps = ""
    @styles.each do |s|
      ps += s.ps(l, @tags)
    end
    ps
  end

# }}}
  def renderpost# {{{
    "gr\n"
  end

# }}}
  def renderlength# {{{
    length = 0
    @styles.each { |s| length = length > s.length ? length : s.length }
    length
  end

# }}}
  def inc# {{{
    @usecount += 1
  end

# }}}
  def dec# {{{
    @usecount -= 1
  end

# }}}
  def used# {{{
    @usecount
  end

# }}}
  def to_s# {{{
    "tags(" + @tags.map{|t| t[0]+"="+t[1]}.join(",") + ")"
  end

# }}}
end #}}}
class MapObject #{{{
  def initialize
    @tags = nil
    @graph = nil
    @osmid = nil
  end

  def setgraph(g)
    if @graph != nil
      raise "error"
    end
    @graph = g
  end

  def settags(t)
    @tags = t
  end

  def setosmid(id)
    @osmid = id.to_i
  end

  def osmid
    @osmid
  end

  def addtags(t)
    if @tags == nil
      return settags(t)
    end
    if @graph != nil
      return @graph.addtags(self, t)
    end
    @tags = @tags + t
  end

  def tags
    @tags
  end
end #}}}
class Node < MapObject #{{{
  def initialize(lon, lat)# {{{
    @lon = lon.to_f
    @lat = lat.to_f
    @segments = []
    super()
  end

  # }}}
  def addsegment(seg)# {{{
    if seg != nil
      if not @segments.include?(seg)
        @segments.push(seg)
      end
    end
  end

  # }}}
  def segments# {{{
    @segments
  end

  # }}}
  def x# {{{
    (@lon + 180) / 360
  end

  # }}}
  def y# {{{
    ly = projf(85.0511)
    y = projf(@lat)
    (ly - y) / (2 * ly)
  end

  # }}}
  def to_s# {{{
    #"node(" + @lon.to_s + "," + @lat.to_s + ")"
    "node(#" + @osmid.to_s + ")"
  end
  
  # }}}

  private

  def projf(la)# {{{
    la = la * (3.1415926535897932384/180)
    Math.log10(Math.tan(la) + (1/Math.cos(la)))
  end

# }}}
end

#}}}
class Segment < MapObject #{{{
  def initialize(from, to)
    @fnode = from
    @tnode = to
    super()
    @fnode.addsegment(self)
    @tnode.addsegment(self)
  end

  def from
    @fnode
  end

  def to
    @tnode
  end

  def to_s
    "segment(#" + @osmid.to_s + ", " + @fnode.to_s + "->" + @tnode.to_s + ")"
  end
end

#}}}
class Path < MapObject# {{{
  def initialize(segments)# {{{
    super()
    @segments = segments
    @tags = segments[0].tags
    @segments.each do |s|
      if s.tags != @tags
        raise "all segments in a path must have the same tags"
      end
    end
  end

# }}}
  def rendercount# {{{
    2
  end

  # }}}
  def render# {{{
    ps = ""
    @tags.renderlayers.sort.each do |l|
      ps += renderlayer(l)
    end
    ps
  end

  # }}}
  def renderlayer(l)# {{{
    tr = @tags.renderlayer(l)
    if tr == ""
      return ""
    end
    ps = "mark #{@segments[0].from.x} #{@segments[0].from.y}\n"
    @segments.each do |s|
      ps += "#{s.to.x} #{s.to.y}\n"
    end
    ps += tr
    ps += "cleartomark\n"
    ps
  end

  # }}}
end

# }}}
class Graph#{{{
  def initialize# {{{
    @nodes = {}
    @segments = {}
    @tags = {}
    @groups = {}
    @paths = {}
    @styles = []
  end
  
  # }}}
  def addnode(node)# {{{
    node.setgraph(self)
    @nodes[node] = {}
    # this call will use findtags to make sure that duplicate tags
    # are stored in the same Tag object
    setobjtags(node, node.tags)
  end
  
  # }}}
  def addsegment(segment)# {{{
    segment.setgraph(self)
    @segments[segment] = {}
    # this call will use findtags to make sure that duplicate tags
    # are stored in the same Tag object
    setobjtags(segment, segment.tags)
  end
  
  # }}}
  def addpath(path)# {{{
    path.setgraph(self)
    @paths[path] = 1
  end
  
  # }}}
  def addstyle(style)# {{{
    @styles.push(style)
  end
  
  # }}}
  def makepaths# {{{
    @paths = {}
    seg = @segments.keys.dup

    while (seg.length > 0)
      p = Path.new(buildpath(seg))
      addpath(p)
    end
  end

  # }}}
  def nodes# {{{
    @nodes.keys
  end
  
  # }}}
  def segments# {{{
    @segments.keys
  end
  
  # }}}
  def tags# {{{
    @tags
  end
  
  # }}}
  def addtags(obj, tags)# {{{
    if tags == nil
      return
    end
    if obj.tags == nil
      newtags = tags
    else
      newtags = obj.tags.tags.merge(tags)
    end
    setobjtags(obj, newtags)
  end
  
  # }}}
  def findtags(tags)# {{{
    @tags.keys.each do |t|
      if t.tags == tags
        return t
      end
    end
    nil
  end
  
  # }}}
  def setobjtags(obj, tags)# {{{
    tagdec(obj.tags)
    t = findtags(tags)
    if t == nil
      t = Tags.new(tags)
    end
    obj.settags(t)
    taginc(t)
  end
  
  # }}}
  def to_s# {{{
    str = "== nodes ==\n"
    @nodes.keys.each do |n|
      str += "#{n} "
      str += "#{n.tags}\n"
    end

    str = "== segments ==\n"
    @segments.keys.each do |s|
      str += "#{s} "
      str += "#{s.tags}\n"
    end

    str += "== tags ==\n"
    @tags.keys.each do |t|
      str += "#{t.to_s}\n"
    end
    str
  end
  
  # }}}

  def bbox# {{{
    mm = {}
    mm[:miny] = mm[:maxy] = @nodes.keys[0].y
    mm[:minx] = mm[:maxx] = @nodes.keys[0].x

    @nodes.keys.each do |n|
      if n.y <  mm[:miny]
        mm[:miny] = n.y
      end
      if n.x < mm[:minx]
        mm[:minx] = n.x
      end
      if n.y >  mm[:maxy]
        mm[:maxy] = n.y
      end
      if n.x > mm[:maxx]
        mm[:maxx] = n.x
      end
    end

    mm
  end

  #}}}
  def eps(width, height)# {{{
    ps = "%!PS-Adobe-3.0 EPSF-2.0\n"
    ps += "%%Creator: osmgraph.rb\n"
    ps += "%%BoundingBox: 0 0 #{width} #{height}\n"
    mm = bbox()
    ps += PSResource
    ps += <<EOP
/mx #{mm[:minx]} def
/Mx #{mm[:maxx]} def
/my #{mm[:miny]} def
/My #{mm[:maxy]} def
/width #{width} def
/height #{height} def
% scale
/sc {
  width Mx mx sub div
  height My my sub div
  2 copy gt {exch pop} {pop} ifelse
} bd
% x and y translations
/x { mx sub sc mul } bd
/y { my sub sc mul height exch sub} bd
EOP

#    ps += "% nodes\n"
#    ps += "newpath\n"
#    @nodes.keys.each do |n|
#      ps += "#{n.x} x #{n.y} y n\n"
#    end
#    ps += "stroke\n"

    ps += "\n"
    ps += "0.1 setlinewidth\n"
    ps += "0 setgray\n"
    ps += "\n"
    ps += "% paths\n"
    ps += "1 setlinecap 1 setlinejoin\n"

    layers = {}
    @tags.keys.each do |tag|
      tag.renderprepare(@styles)
      tag.renderlayers.each do |l|
        layers.has_key?(l) ? layers[l].push(tag) : layers[l] = [tag]
      end
    end

    layers.keys.sort.each do |l|
      @tags.keys.each do |tag|
        if layers[l].include?(tag)
          content = ""
          @paths.keys.each do |p|
            if p.tags == tag
              content += p.renderlayer(l)
            end
          end
          if content != ""
            ps += "% #{tag.to_s}\n"
            ps += tag.renderpre
            ps += content
            ps += tag.renderpost
          end
        end
      end
    end
    
    ps += "showpage\n"
    ps
  end

 

# }}}
  private

  def taginc(tags)# {{{
    if tags == nil
      return
    end
    if @tags.has_key?(tags)
      @tags[tags] += 1
    else
      @tags[tags] = 1
    end
  end
  
  # }}}
  def tagdec(tags)# {{{
    if tags == nil
      return
    end
    if @tags.has_key?(tags)
      @tags[tags] -= 1
      if @tags[tags] == 0
        @tags.delete(tags)
      end
    end
  end
  
  # }}}
  def buildpath(segments)# {{{
  # this finds a path in the segments by whether adjacent segments
  # have the same tags or not.
  # it is slightly broken - need to check for currseg.to OR
  # currseg.from depending on the node we came from (a path could
  # consist of segments that point in different directions)
  # hmm - maybe... ----oneway---><---oneway---- would break this, so
  # probably it is ok.
  # It _does_ need to be extended to always find the longest
  # possible path, otherwise names can occasionally be placed on
  # roads in unusual places!
    path = []
    debug = false

    currseg = segments[0]

    tags = currseg.tags
    # forward
    while currseg != nil
      path.push(currseg)
      segments.delete(currseg)
      candidates = []
      if debug
        puts "considering F segment #{currseg.to_s}"
      end
      currseg.to.segments.each do |s|
        if debug
          puts "  looking at segment #{s.to_s}"
          if s.tags != tags
            puts "    tags don't match"
          end
          if path.include?(s)
            puts "    path includes this"
          end
        end
        if s.from == currseg.to and s.tags == tags and not path.include?(s)
          if segments.include?(s)
            candidates.push(s)
          end
        end
      end
      currseg = nil
      if candidates.length > 0
        currseg = candidates[0]
      end
    end

    currseg = path[0]
    path.shift
    # backwards
    while currseg != nil
      path.unshift(currseg)
      segments.delete(currseg)
      if debug
        puts "considering B segment #{currseg.to_s}"
      end
      candidates = []
      currseg.from.segments.each do |s|
        if debug
          puts "  looking at segment #{s.to_s}"
          if s.tags != tags
            puts "    tags don't match"
          end
          if path.include?(s)
            puts "    path includes this"
          end
        end
        if s.to == currseg.from and s.tags == tags and not path.include?(s)
          if segments.include?(s)
            candidates.push(s)
          end
        end
      end
      currseg = nil
      if candidates.length > 0
        currseg = candidates[0]
      end
    end

    path
  end

  # }}}

end# }}}

def importosm(file) #{{{
  # open xml document
  doc = XML::Document.file(file)
  root = doc.root

  g = Graph.new

  # hash to keep track of osm ids
  nodes = {}
  # read nodes
  doc.find("//osm/node").each do |n|
    tags = {}
    n.each_child do |c|
      if c.name == "tag"
        tags[c['k']] = c['v']
      end
    end
    node = Node.new(n['lon'], n['lat'])
    node.settags(tags)
    node.setosmid(n['id'])
    g.addnode(node)
    nodes[n['id']] = node
  end

  # hash for osm segment ids
  segments = {}
  # read segments
  doc.find("//osm/segment").each do |s|
    tags = {}
    s.each_child do |c|
      if c.name == "tag" and c['k'] != "created_by"
        tags[c['k']] = c['v']
      end
    end
    segment = Segment.new(nodes[s['from']], nodes[s['to']])
    segment.settags(tags)
    segment.setosmid(s['id'])
    g.addsegment(segment)
    segments[s['id']] = segment
  end

  # pull in all the ways. we don't care about them per se, but
  # we do care that the segments in a way get the way's tags
  doc.find("//osm/way").each do |w|
    tags = {}
    segs = {}
    w.each_child do |c|
      if c.name == "tag" and c['k'] != "created_by"
        tags[c['k']] = c['v']
      end
      if c.name == "seg"
        # add segment to segs list only if it existed in the osm file
        if segments.has_key?(c['id'])
          segs[segments[c['id']]] = 1
        end
      end
    end
    segs.keys.each do |s|
      s.addtags(tags)
    end
  end

  g
end #}}}

if not FileTest.exist?(ARGV[0].to_s)
  puts "Syntax: #{$0} <filename.osm>"
  puts "PostScript it output on STDOUT"
  exit
end

g = importosm(ARGV[0])

# {{{ styles
#
#
# layer numbers
#
# 0    - 999     Land use / areas below everything else
# 1000 - 1999    Water features
# 2000 - 2999    Highway
# 3000 - 3999    Railway
# 4000 - 4999    Highway bridges
# 5000 - 5999    Railway bridges
#
#
#
# Land areas 0-999
#

  s = Style.new()
  s.addtag("landuse", nil)
  s.setarea
  s.adddrawps(0, "220 220 240 c pathcopy area")  # fill in the area
  g.addstyle(s)


  s = Style.new()
  s.addtag("landuse", "residential")
  s.setarea
  s.adddrawps(10, "220 220 220 c pathcopy area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("landuse", "retail")
  s.setarea
  s.adddrawps(10, "255 220 220 c pathcopy area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("amenity", "university")
  s.setarea
  s.adddrawps(10, "225 180 255 c pathcopy area")
  g.addstyle(s)


  s = Style.new()
  s.addtag("leisure", "park")
  s.setarea
  s.adddrawps(15, "155 215 70 c pathcopy area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("amenity", "parking")
  s.setarea
  s.adddrawps(20, "220 220 100 c pathcopy area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("landuse", "green_space")
  s.setarea
  s.adddrawps(20, "220 255 220 c pathcopy area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("leisure", "pitch")
  s.setarea
  s.adddrawps(20, "30 160 30 c pathcopy area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("landuse", "wood")
  s.addtag("natural", "wood")
  s.setarea
  s.adddrawps(20, "10 70 10 c pathcopy area")
  g.addstyle(s)


# Water 1000-1999

  s = Style.new()
  s.addtag("waterway", "river")
  s.setpath
  s.adddrawps(1100, "1.8 lw 120 120 220 c pathcopy line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("waterway", "canal")
  s.setpath
  s.adddrawps(1200, "0.7 lw 70 70 220 c pathcopy line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("waterway", "stream")
  s.setpath
  s.adddrawps(1300, "0.7 lw 120 120 220 c pathcopy line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("natural", "water")
  s.setarea
  s.adddrawps(1400, "120 120 220 c pathcopy area")
  s.adddrawps(1401, "0.1 lw 80 80 220 c pathcopy cline")
  g.addstyle(s)

# Highway 2000-2999

def setroad(sty, l1, l2, width, bridge, casecol, corecol)
  brcasew = width * 1.6
  brcorew = width * 1.4
  corew = width * 0.7
  sty.setpath
  if bridge
    sty.addtag("bridge", "yes")
    sty.adddrawps(4000, "0 setlinecap #{brcasew} lw 0 0 0 c pathcopy line")
    sty.adddrawps(4001, "0 setlinecap #{brcorew} lw 255 255 255 c pathcopy line")
    if l1 != nil
      sty.adddrawps(l1 + 2000, "0 setlinecap #{width} lw #{casecol} c pathcopy line") # casing over bridge
    end
    sty.adddrawps(l2 + 2100, "0 setlinecap #{corew} lw #{corecol} c pathcopy line") # core
  else 
    if l1 != nil
      sty.adddrawps(l1, "#{width} lw #{casecol} c pathcopy line")  # casing
    end
    sty.adddrawps(l2, "#{corew} lw #{corecol} c pathcopy line pathcopy")  # core
    sty.adddrawps(2999, "pathcopy")  # name
    sty.adddrawtagstring(2999, "name")
    sty.adddrawps(2999, "roadname")
  end
end

for bridge in [false, true]
  bl = bridge ? 2000 : 0
  s = Style.new()
  s.addtag("highway", "motorway")
  setroad(s, 2050, 2500, 1, bridge, "0 0 0", "20 60 200")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "trunk")
  setroad(s, 2050, 2580, 1, bridge, "0 0 0", "50 150 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "primary")
  setroad(s, 2050, 2560, 1, bridge, "0 0 0", "255 50 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "secondary")
  setroad(s, 2050, 2540, 0.9, bridge, "0 0 0", "250 75 10")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "tertiary")
  setroad(s, 2050, 2520, 0.8, bridge, "0 0 0", "220 220 0")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", ["unclassified", "residential"])
  setroad(s, 2050, 2500, 0.8, bridge, "0 0 0", "240 240 240")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "motorway_link")
  setroad(s, 2050, 2480, 0.7, bridge, "0 0 0", "20 60 200")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "trunk_link")
  setroad(s, 2050, 2460, 0.7, bridge, "0 0 0", "50 150 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "service")
  setroad(s, 2050, 2440, 0.5, bridge, "0 0 0", "250 250 250")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "track")
  setroad(s, 2030, 2420, 0.5, bridge, "80 30 0", "250 250 250")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", ["footway", "steps"])
  setroad(s, nil, 2140, 0.2, bridge, nil, "80 30 0")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "cycleway")
  setroad(s, nil, 2160, 0.3, bridge, nil, "0 80 0")
  g.addstyle(s)
end

# Rail 3000 - 3999

  s = Style.new()
  s.addtag("railway", "rail")
  s.setpath
  s.adddrawps(3500, "1 lw 0 0 0 c pathcopy line")  # core
  g.addstyle(s)

  s = Style.new()
  s.addtag("railway", "preserved")
  s.setpath
  s.adddrawps(3300, "0.6 lw 25 25 25 c pathcopy line")  # core
  g.addstyle(s)

# }}}

g.makepaths
puts g.eps(500,500)

