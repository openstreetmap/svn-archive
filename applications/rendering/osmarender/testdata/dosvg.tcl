puts "tclsh dosvg.tcl infile.osm startzoom endzoom"
set f [ lindex $argv 0 ]
set zo [ lindex $argv 2 ]
set zu [ lindex $argv 1 ]
if { $zo == "" } { set zo 17 }
if { $zu == "" } { set zo 12 }

set r [ file rootname $f ]
puts "$f $r"
for { set z $zo } { $z >= $zu } { incr z -1 } {
    set i [ catch { file delete $r$z.svg } o ]
    puts "$z - $i : $o"
    set i [ catch { exec perl.exe "c:/Users/Heiko\ Jacobs/Documents/osm/osmarender/orp/orp.pl" -r "C:/Users/Heiko\ Jacobs/Documents/osm/osmarender/stylesheets/osm-map-features-z$z.xml" $f } o ]
    puts "$i : $o"
    set i [ catch { file rename $r.svg $r$z.svg } o ]
    puts "$i : $o"

}
