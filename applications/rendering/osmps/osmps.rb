#! /usr/bin/ruby
#
# OSMPS 0.03.1
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
/pathisrtol
%  { counttomark 1 sub index
%    2 index gt
  { false
  } def
/area { dup exec eofill } def
/line { dup exec stroke } def
/cline { dup exec closepath stroke } def
/c {
255 div 3 1 roll
255 div 3 1 roll
255 div 3 1 roll
setrgbcolor
} def
/m { exch x exch y moveto } def
/l { exch x exch y lineto } def
/np {newpath} bd
/st {stroke} bd
/lw {setlinewidth} bd
/gs {gsave} bd
/gr {grestore} bd
/cpf {closepath fill} bd
/rgb {setrgbcolor} bd

/osm {/OsmPsMapSymbol findfont 2 scalefont setfont} bd

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
    0 charbaseoffset neg moveto
    char show
    0 charbaseoffset rmoveto
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
    dup exec
    pathisrtol
      { reversepath }
      if
    gsave
    /plen pathlen def
    RoadNameCoreFont
    /tlen text stringwidth pop def
    grestore
    tlen 0.0 gt plen 0.0 gt and
    plen tlen gt and
    { gsave
      1 1 1 setrgbcolor
      RoadNameOutlineFont
      text
      plen 2 div tlen 2 div sub
      pathtext
      grestore
      gsave
      0 0 0 setrgbcolor
      RoadNameCoreFont
      text
      plen 2 div tlen 2 div sub
      pathtext
      grestore
    } if
    end
  } def

/RoadNameFont
  { /Helvetica-Bold findfont 
  } def

/RoadNameOutline 
RoadNameFont
dup maxlength 1 add
exch /UniqueID known not { 1 add } if
dict def

RoadNameFont
{ exch dup /FID ne
  { exch RoadNameOutline 3 1 roll put }
  { pop pop }
  ifelse
} forall

RoadNameOutline
dup /PaintType 2 put
dup /StrokeWidth 300 put
dup /FontName /RoadNameOutline put
dup /UniqueID 1001 put
/RoadNameOutline exch definefont pop

/RoadNameCoreFont
  { RoadNameFont
    0.5 scalefont
    setfont
  } def

/RoadNameOutlineFont
  { /RoadNameOutline findfont
    0.5 scalefont
    setfont
  } def

RoadNameCoreFont

%%EndResource
EOR
# }}}
PSSymbols = <<EOR# {{{
%!PS-Adobe-2.0 EPSF-1.0
%%Creator: Matthew Newton
%%BoundingBox: -100 -100 100 100
%%EndComments

%!PS-AdobeFont-1.0: OsmPsMapSymbol 001.000
%%Title: OsmPsMapSymbol
%Version: 001.000
%%CreationDate: Thu Mar 29 23:20:54 2007
%%Creator: Matthew Newton
%Copyright: Copyright (c) Matthew Newton
% 2007-3-29: Created.
% Generated by FontForge 20061019 (http://fontforge.sf.net/)
%%EndComments

FontDirectory/OsmPsMapSymbol known{/OsmPsMapSymbol findfont dup/UniqueID known{dup
/UniqueID get 4167772 eq exch/FontType get 1 eq and}{pop false}ifelse
{save true}{false}ifelse}{false}ifelse
12 dict begin
/FontType 1 def
/FontMatrix [0.001 0 0 0.001 0 0 ]readonly def
/FontName /OsmPsMapSymbol def
/FontBBox {-500 -500 500 500 }readonly def
/UniqueID 4167772 def
/XUID [1021 864 700174176 11540681] def
/PaintType 0 def
/FontInfo 9 dict dup begin
 /version (001.000) readonly def
 /Notice (Copyright \050c\051 Matthew Newton) readonly def
 /FullName (OsmPsMapSymbol) readonly def
 /FamilyName (OsmPsMapSymbol) readonly def
 /Weight (Medium) readonly def
 /FSType 8 def
 /ItalicAngle 0 def
 /isFixedPitch false def
 /ascent 500 def
end readonly def
/Encoding StandardEncoding def
currentdict end
currentfile eexec
04BFDF59CBE545B9A7608E6A873EAB8F48A2C998996D3FA67E4DE5DDC64DE4B43314CF7D
25A137EC0083CA76D4B52E9651F685281A6DF824636C77DA0B9290F4CAE31C715150BA63
E06D2369BE0F51247BDD07129098BB78A6E98EEACB698CD9D0BB72DBE44936E90A0B8069
2B5896AEE49690958D5D7D00686686EFAC9389B34735B8BA85CD441E9EC6938189E46EB3
FB609C3E875554A4C6970883D6781AC293F8AD67A57BF3924F25A9CB361650FA58A24A2F
AC68E8B5BFE6AF15C9B99F61247D80963F0B2CA7DC82FA9D98C5D3CF107ED227F790A574
36727FAA010F747AC51F0EB179AC1DE05CE8578FFC8ED7C9C5D53FF32EFC9C690B2FD051
097624227ABB5828661664792AD58C3FB49221E4A3E5BCF11BF53C550CD92C321D9B4595
02D52D7B434A6999B023E4E1F18F9D877ADA8857219C8EDE281739A3BD252841D858008F
50ABBE70ACD9391A5F9F4FEFB2C3C5B80F746F7BBB9600BBCC7BCE37DA027AB722D0F3FC
73A15A18C1BA49B808527520A6000F7D01B8CDF722B6F0CDBCF05E89B70193F5778912B4
AD9F8F0A80A9C0B004D77B7C76E30C2ABDAE5B7AA7A01C370B48032FF6DC827944CB20B1
56AE567C8E36868886A89A68948D7654117DD38F1293ED6967B217AB76D618A41BA7794C
FB2504AFB726D1584C42D32C33C32A227A69BDFDEE582BC601E5E686C8CA053A9C79A095
DEAAF8147384F830BA3D48E448BF35746BEF6F7458C56E943FDB4714C4BC7A0AEC2B4C9C
290A40FD87C519F0ACA392C9F64F5F7A623967D7448E749BD16E0C4D91CBE617B1291FEC
503198CA595773A17007A1A47BB1EA0158688D7011265B7535D84F0C6920F1630955638F
5AA801D589668AE9BF1E6922CD77DD1672E5BC0D69014E2220AB69C99D23D661B2D5C242
23C15B580D66016AE416168B429D2445EAB59E2D89AEFA486A787C58D1F67A824C9F8CA1
9D401CAF26C58CA2F7B7B046ACEA410B74D5E6EA82BA53FCFE119FD47DA511CAEAE51569
577C51EDF2BCA368D24A7C61E74965F320CD3E44994F3687E2FF5CF37462CCBB48AA7CE1
A9FC64520A15988BB6A2FE0A9C69F79DC85657B7F5D457266DF1B8FF50FCDDEA2036FFC4
421D9CDE30A3BDE904E1C0F8624913C79B91C47C8D3F78C118F23B88F7615008EA643CEB
E43B2C347E6568D8CAAD5304B355F30C0F9C9A75DB32BAC455BF8775C27AEA6C73EED3EB
2F8DB5E3B70495047870D57DEE2CE1EE6C921D43AD56BA6B5B915CA1BE152A80E2831F1F
067DBA58A0A46A7AE93A723618BBD0D164DFA85C53DC83E94177EB2A3418B1255FB07C91
5411AFA72242A099D69EFA0A822BC6ED58AEB83B72CB902DEE4E13EA2313764F0981CA3A
B0C89468B926E84BADE4B196DE9E61C4DDD729BF3CADEE9AC541E9C6F0A9D6EEE0ECB02A
6E2461B66D86C8404DA6DA6BB1C61ABF0183C9FDCE63C76DED96F2F8FB66DAFA526D86AC
3A23F613E6B46F1FDF7A9B8F6983C2FCE7AFE2584E681C948F07E82C8FEE27C26F057060
3B1A79522D71D391BC195C0B9D7BE7D63CE0E943C4A42063E80317B8AF450B85397BC85B
5E2A9FF6FB979AAF46AA343CE145A2B66ED0F6CCB7C8F4AA670BD4F3192F2744922AF1A8
C75035063C42685953EDED2589FC2D3249BA6A84E64AADABC7F43B75434A137D99BEF151
E46F87CCDF7FB15A53306C379281E453D8BEB8C7CAC16C877C06843D0213FF05368BF39E
0B909CA3C0CA03B501E59EF3BF3BD159D01BF43DFBECDB3D293FAC49739661F030F04518
5F4F0A4B88B2ED20AEABC8889DB0776905D13FBF1EAFB61B7D1A1CED03D5E4A2FA4E18FB
72B1E322F30047FEB3A7F66F3C3F9B02B2A4844BFFB29AA230EE6C486BEC852B6ECA992B
6D7C75BA1E9D47D88BE8C4D20BA775775D9A49F964C853D4147B4A6C1281594189D8EDC7
DEFF26A600CB3F676CBB528C8F9E589A701707A6896A6A192325FAC205BE45F4A4972826
5B4C046F941D3E73A72B267AD612000A564ADF56A2F19F1F8F5E379F4A5B6DDDEF98BA91
0339323A7630152B134146A2523CF46A402100F21833401361DDA5DA0401D457F7F36EC4
8FA3B70A4F149F567F8B979742E9A09F976B5F4F09CCCAE5F024FDD7D388C033FBE8386C
223F07C7F595BC550280A7FDBACBCD1A7C1ABD4354F21B75CAB3AC4A4BDCAA5C8F89549B
EDD03CBBF85BFE6575516F4A4D692BA520AAAA998B74A224A29AC0B6113ACECC88D6DA74
5C23715D684CE9A8C90C5C0E5820E2F48680D98A98FFB02CD134023C6771C5316CDE6D23
CAE1B1B69FD2DF7CBC07BBF43C92F40D5CE76E9D0A4990C79A889EE6670FEC948F771438
5E9FA212CCB24047C5843E60328DA0AD4493419CDF61C65D384E2ABCD716F4B86DCF1FD0
BA8EEAC34A039128E6FD5F3DD2B0A5E43531A4B18D2F867157E61971C34901C04DB41D09
22B9350781A729A646825F7DE1A15DD2A2F20214C0FEA3DACCBBC99602843F8CE05C372E
AE4216B4E3B354C393216A36CFC28B7AFF671AF0EE2D701329216FA203C944810CC303FE
153ED1EC7266903C3176B8ECEB7069C98074B54368E6D321DDA11B4D47649892369BEEFC
5AD6B875B8B4DA42130FA02583A12D899C3BBCFA71C4328AD6CA498EAE7A85D67FB52574
6E27C9DE7589D165583A3412924256D02CC043ECFBB53BCCA7729B5BC8D23053FCE18114
A88C8D237EE4EF0122027C9098A278C632885B53B669DE68E060415BDEB20F0AC1CA61ED
22E353323AF1FED55916ABED28F0775AE4237A827A944C8B3CEEBE688B011BC69E697209
3AF24A74E0DF65A155A6885EE2A46996E6CB1F35608AFC257135B3D3AA50BF420AD6169E
CAA113F785
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
0000000000000000000000000000000000000000000000000000000000000000
cleartomark
{restore}if

%%BeginDocument: osmlogos.eps
/logo-churchofe {
  gs osm
  {0.3 setgray} if
  (c) show
  gr
} def
/logo-church {
  gs osm
  pop
  (C) show
  gr
} def
/logo-pub {
  gs osm
  (B) show
  {0.8 0.5 0 rgb
  (b) show} if
  gr
} def
/logo-hospital {
  gs osm
  {0.8 0 0 rgb} if
  (H) show
  gr
} def
/logo-postoffice {
  gs osm
  dup {0.8 0.2 0 rgb} if
  (P) show
  {0.8 0.8 0 rgb
  (p) show} if
  gr
} def
/logo-parking {
  gs osm
  {0.3 0.3 0.9 rgb} if
  (k) show
  gr
} def
/logo-library {
  gs osm
  pop
  (l) show
  gr
} def
%%EndDocument
%%EOF
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
  def settype(type)# {{{
    if [:area, :path, :node, :any].include?(type)
      @type = type
    else
      raise "bad type"
    end
  end

# }}}
  def type# {{{
    @type
  end

# }}}
  def area?# {{{
    @type == :area
  end

# }}}
  def path?# {{{
    @type == :path
  end

# }}}
  def node?# {{{
    @type == :node
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
    @styletypes = {}
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
  def styles# {{{
    @styles
  end

# }}}
  def styletypes# {{{
    @styletypes.keys
  end

# }}}
  def renderprepare(styles)# {{{
    @styles = []
    styles.each do |s|
      if s.matchtags(@tags)
        @styles.push(s)
        @styletypes[s.type] = 1
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
  def initialize# {{{
    @tags = nil
    @graph = nil
    @osmid = nil
  end

# }}}
  def setgraph(g)# {{{
    if @graph != nil
      raise "error"
    end
    @graph = g
  end

# }}}
  def settags(t)# {{{
    @tags = t
  end

# }}}
  def setosmid(id)# {{{
    @osmid = id.to_i
  end

# }}}
  def osmid# {{{
    @osmid
  end

# }}}
  def addtags(t)# {{{
    if @tags == nil
      return settags(t)
    end
    if @graph != nil
      return @graph.addtags(self, t)
    end
    @tags = @tags + t
  end

# }}}
  def tags# {{{
    @tags
  end

# }}}
end #}}}
class Node < MapObject #{{{
  def initialize(lon, lat)# {{{
    @lon = lon.to_f
    @lat = lat.to_f
    super()
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
end #}}}
class Path < MapObject# {{{
  def initialize(nodelist)# {{{
    super()
    @nodelist = nodelist
    @closed = false
    if nodelist[0] == nodelist[-1]
      @closed = true
    end
  end

# }}}
  def closed?# {{{
    @closed
  end

# }}}
  def setopen# {{{
    @closed = false
  end

# }}}
  def setclosed# {{{
    @closed = true
  end

# }}}
  def split(snode)# {{{
    if @closed
      raise :closedcantsplit
    end
    if snode == @nodelist[0] or snode == @nodelist[-1]
      raise :atendcantsplit
    end
    position = @nodelist.index(snode)
    if position == nil
      raise :nonodetosplit
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
    ps = "{ np "
    ps += pspath()
    ps += "} "
    ps += tr
    ps += "pop\n"
    ps
  end

  # }}}
  def pspath# {{{
    ps = "#{@nodelist[0].x} #{@nodelist[0].y} m\n"
    stop = @nodelist.length-1
    if @closed and @nodelist[0] == @nodelist[stop]
      stop -= 1
    end
    for n in (1..stop)
      ps += "#{@nodelist[n].x} #{@nodelist[n].y} l\n"
    end
    if @closed
      ps += "closepath\n"
    end
    ps
  end

  # }}}
  def to_s# {{{
    "path(#" + @osmid.to_s + ")"
  end

  # }}}
end # }}}
class Area < MapObject# {{{
  def initialize(path)# {{{
    super()
    @paths = []
    @tags = nil
    if path != nil
      @paths = [path]
      @tags = path.tags
    end
  end

# }}}
  def addpath(path)# {{{
    if @tags == nil
      @tags = path.tags
    end
    if @tags != path.tags
      raise "all paths in an area must have the same tags"
    end
    @paths.push(path)
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
    ps = "{ np "
    @paths.each do |p|
      ps += p.pspath
    end
    ps += "} "
    ps += tr
    ps += "pop\n"
    ps
  end

  # }}}
end # }}}
class Graph#{{{
  def initialize# {{{
    @nodes = {}
    @paths = {}
    @areas = {}
    @tags = {}
    @styles = []
    @scale = 1000000
  end
  
  # }}}
  def importosm(file) #{{{
    # open xml document
    doc = XML::Document.file(file)
    root = doc.root

    # hash to keep track of osm ids
    nodes = {}
    # read nodes
    doc.find("//osm/node").each do |n|
      if n['visible'] != 'true'
        next
      end
      tags = {}
      n.each_child do |c|
        if c.name == "tag" and c['k'] != "created_by"
          tags[c['k']] = c['v']
        end
      end
      if n['action'] != 'delete'
        node = Node.new(n['lon'], n['lat'])
        node.settags(Tags.new(tags))
        node.setosmid(n['id'])
        addnode(node)
        nodes[n['id']] = node
      end
    end

    ways = {}
    # pull in all the ways
    doc.find("//osm/way").each do |w|
      tags = {}
      waynodes = []
      if w['action'] == 'delete' or w['visible'] != 'true'
        next
      end
      w.each_child do |c|
        if c.name == "tag" and c['k'] != "created_by"
          tags[c['k']] = c['v']
        end
        if c.name == "nd"
          # add node to nodes list only if it existed in the osm file
          if nodes.has_key?(c['ref'])
            waynodes.push(nodes[c['ref']])
          end
        end
      end

      p = Path.new(waynodes)
      p.settags(Tags.new(tags))
      p.setosmid(w['id'])
      addpath(p)
      ways[w['id']] = p

      if p.closed?
        addarea(Area.new(p))
      end
    end
  end #}}}
  def addnode(node)# {{{
    node.setgraph(self)
    @nodes[node] = {}
    # this call will use findtags to make sure that duplicate tags
    # are stored in the same Tag object
    setobjtags(node, node.tags)
  end
  
  # }}}
  def addpath(path)# {{{
    path.setgraph(self)
    @paths[path] = 1
    setobjtags(path, path.tags)
  end
  
  # }}}
  def addarea(area)# {{{
    area.setgraph(self)
    @areas[area] = 1
  end
  
  # }}}
  def addstyle(style)# {{{
    @styles.push(style)
  end
  
  # }}}
  def nodes# {{{
    @nodes.keys
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
  def findtags(ftags)# {{{
    @tags.keys.each do |t|
      if t.tags == ftags.tags
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
      t = tags
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

    str += "== paths ==\n"
    @paths.keys.each do |p|
      str += "#{p} "
      str += "#{p.tags}\n"
    end

    str += "== tags ==\n"
    @tags.keys.each do |t|
      str += "#{t.to_s}\n"
    end
    str
  end
  
  # }}}
  def epsbbox(userscale = 1)# {{{
    r = {}
    mm = bbox()
    r[:width] = (mm[:maxx] - mm[:minx]) * @scale * userscale
    r[:height] = (mm[:maxy] - mm[:miny]) * @scale * userscale
    r[:nodeminmax] = mm
    r
  end

  # }}}
  def eps(userscale = 1)# {{{
    wh = epsbbox(userscale)
    mm = wh[:nodeminmax]

    ps = "%!PS-Adobe-3.0 EPSF-2.0\n"
    ps += "%%Creator: osmps.rb\n"
    ps += "%%BoundingBox: 0 0 #{wh[:width]} #{wh[:height]}\n"
    ps += "%%EndComments\n"
    ps += "<< /PageSize [ #{wh[:width]} #{wh[:height]} ] /ImagingBBox null >> setpagedevice\n"
    ps += PSResource
    ps += PSSymbols
    ps += <<EOP
/mx #{mm[:minx]} def
/Mx #{mm[:maxx]} def
/my #{mm[:miny]} def
/My #{mm[:maxy]} def
/width #{wh[:width]} def
/height #{wh[:height]} def
% scale and x and y translations
/sc #{@scale} def
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

    # find out what layers we need to render
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
          if tag.styletypes.include?(:area)
            @areas.keys.each do |a|
              if a.tags == tag
#                puts "tag #{tag}"
#                puts "  area #{a} #{a.tags.to_s}"
#                puts "    yes"
                content += a.renderlayer(l)
              end
            end
          end
          if tag.styletypes.include?(:path)
            @paths.keys.each do |p|
#              puts "path #{p}"
              if p.tags == tag
                content += p.renderlayer(l)
              end
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
    
    ps += "% nodes\n"
    @nodes.keys.each do |n|
      if n.tags.tags.include?("amenity")
        if n.tags.tags["amenity"] == "pub"
          ps += "np #{n.x} #{n.y} m true logo-pub\n"
        end
        if n.tags.tags["amenity"] == "parking"
          ps += "np #{n.x} #{n.y} m true logo-parking\n"
        end
      end
    end

    ps += "%%EOF\n"
    ps
  end

 

# }}}

  private

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

end# }}}


if not FileTest.exist?(ARGV[0].to_s)
  puts "Syntax: #{$0} <filename.osm>"
  puts "PostScript is output on STDOUT"
  exit
end

g = Graph.new
g.importosm(ARGV[0])

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
  s.settype(:area)
  s.adddrawps(0, "220 220 240 c area")  # fill in the area
  g.addstyle(s)


  s = Style.new()
  s.addtag("landuse", "residential")
  s.settype(:area)
  s.adddrawps(10, "220 220 220 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("landuse", "retail")
  s.settype(:area)
  s.adddrawps(10, "255 220 220 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("amenity", "university")
  s.settype(:area)
  s.adddrawps(10, "225 180 255 c area")
  g.addstyle(s)


  s = Style.new()
  s.addtag("leisure", "park")
  s.settype(:area)
  s.adddrawps(15, "155 215 70 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("amenity", "parking")
  s.settype(:area)
  s.adddrawps(20, "220 220 100 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("landuse", "green_space")
  s.settype(:area)
  s.adddrawps(20, "220 255 220 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("leisure", "pitch")
  s.settype(:area)
  s.adddrawps(20, "30 160 30 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("landuse", "wood")
  s.addtag("natural", "wood")
  s.settype(:area)
  s.adddrawps(20, "10 70 10 c area")
  g.addstyle(s)

  s = Style.new()
  s.addtag("sport", ["bowls", "tennis"])
  s.settype(:area)
  s.adddrawps(20, "40 150 40 c area")
  g.addstyle(s)


# Water 1000-1999

  s = Style.new()
  s.addtag("waterway", "river")
  s.settype(:path)
  s.adddrawps(1100, "0.8 lw 120 120 220 c line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("waterway", "canal")
  s.settype(:path)
  s.adddrawps(1200, "0.6 lw 70 70 220 c line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("waterway", "stream")
  s.settype(:path)
  s.adddrawps(1300, "0.4 lw 120 120 220 c line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("waterway", "drain")
  s.settype(:path)
  s.adddrawps(1350, "0.2 lw 120 120 220 c line")
  g.addstyle(s)

  s = Style.new()
  s.addtag("natural", "water")
  s.settype(:area)
  s.adddrawps(1400, "120 120 220 c area")
  s.adddrawps(1401, "0.1 lw 80 80 220 c cline")
  g.addstyle(s)

# Highway 2000-2999

# update a style for a road - called with:
#   - style object to set
#   - layer for casing
#   - layer for core
#   - width (of casing)
#   - bridge (true or false)
#   - casing colour (ps snippet "r g b")
#   - core colour (ps snippet "r g b")
def setroad(sty, l1, l2, width, bridge, casecol, corecol, dash = nil)
  brcasew = width * 1.6
  brcorew = width * 1.4
  corew = width * 0.7
  sty.settype(:path)
  dashps = ""
  if dash != nil
    if dash.length % 2 == 0
      dashps = "[" + dash.join(" ") + "] 0 setdash "
    end
  end
  if bridge
    sty.addtag("bridge", "yes")
    sty.adddrawps(4000, "0 setlinecap #{brcasew} lw 0 0 0 c line")
    sty.adddrawps(4001, dashps + "0 setlinecap #{brcorew} lw 255 255 255 c line")
    if l1 != nil
      sty.adddrawps(l1 + 2000, "0 setlinecap #{width} lw #{casecol} c line") # casing over bridge
    end
    sty.adddrawps(l2 + 2100, dashps + "0 setlinecap #{corew} lw #{corecol} c line") # core
  else 
    if l1 != nil
      sty.adddrawps(l1, "#{width} lw #{casecol} c line")  # casing
    end
    sty.adddrawps(l2, dashps + "#{corew} lw #{corecol} c line")  # core
#    sty.adddrawps(2999, "pathcopy")  # name
    sty.adddrawtagstring(2999, "name")
    sty.adddrawps(2999, "roadname")
  end
end

# loop to draw all highway-type ways for bridge / no bridge
for bridge in [false, true]
  # layer number, decremented as we go along
  l = 2500

  s = Style.new()
  s.addtag("highway", "motorway")
  setroad(s, 2050, l-=20, 1, bridge, "0 0 0", "20 60 200")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "trunk")
  setroad(s, 2050, l-=20, 1, bridge, "0 0 0", "50 150 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "primary")
  setroad(s, 2050, l-=20, 1, bridge, "0 0 0", "255 50 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "secondary")
  setroad(s, 2050, l-=20, 0.9, bridge, "0 0 0", "250 75 10")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "tertiary")
  setroad(s, 2050, l-=20, 0.8, bridge, "0 0 0", "220 220 0")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", ["unclassified", "residential"])
  setroad(s, 2050, l-=20, 0.8, bridge, "0 0 0", "240 240 240")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "pedestrian")
  setroad(s, 2050, l-=20, 0.8, bridge, "40 40 40", "200 200 200")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "motorway_link")
  setroad(s, 2050, l-=20, 0.7, bridge, "0 0 0", "20 60 200")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "trunk_link")
  setroad(s, 2050, l-=20, 0.7, bridge, "0 0 0", "50 150 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "primary_link")
  setroad(s, 2050, l-=20, 0.7, bridge, "0 0 0", "255 50 50")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "secondary_link")
  setroad(s, 2050, l-=20, 0.5, bridge, "0 0 0", "250 75 10")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "service")
  setroad(s, 2050, l-=20, 0.5, bridge, "0 0 0", "250 250 250")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "track")
  setroad(s, 2030, l-=20, 0.5, bridge, "80 30 0", "250 250 250")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "cycleway")
  setroad(s, nil, l-=20, 0.3, bridge, nil, "0 80 0")
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", "bridleway")
  setroad(s, nil, l-=20, 0.3, bridge, nil, "80 30 0", [0.5, 0.3])
  g.addstyle(s)

  s = Style.new()
  s.addtag("highway", ["footway", "steps"])
  setroad(s, nil, l-=20, 0.2, bridge, nil, "80 30 0")
  g.addstyle(s)
end

# Rail 3000 - 3999

  s = Style.new()
  s.addtag("railway", "rail")
  s.settype(:path)
  s.adddrawps(3500, "1 lw 0 0 0 c line")  # core
  g.addstyle(s)

  s = Style.new()
  s.addtag("railway", "preserved")
  s.settype(:path)
  s.adddrawps(3300, "0.6 lw 25 25 25 c line")  # core
  g.addstyle(s)

# }}}

puts g.eps()

