if { ![ info exists za ] } { puts "call this file only using verify-*.tcl"; exit }

set files [ glob osm-map-features-z*.xml ]
set zooms {}
foreach f $files {
   set z [string range [lindex [split [file rootname [file tail $f]] "-"] 3] 1 end]
   lappend zooms $z
   set fi [ open $f r ]
   set in [ read $fi ]
   close $fi
   set style($z) ""
   set flag 0
   foreach line [ split $in "\n" ] {
      if { [ string first "<style"  $line ] >= 0 } { set flag 1; continue }
      if { [ string first "</style" $line ] >= 0 } { set flag 0; continue }
      if { $flag } {
         set line [ string trim $line ]
         append style($z) " $line"
      }
   }
}
puts $zooms
set zooms [ lsort -integer $zooms ]
foreach z $zooms {
   set widths($z) {}
   set st [ split $style($z) "{}" ]
   foreach { a b } $st {
      set w ""
      set i [ string last "." $a ]
      incr i
      set a [ string trim [ string range $a $i end ] ]
      set bb [ split $b ":;" ]
      foreach { c d } $bb {
         set i [ string first "p" $d ]
         if { $i >= 0 } { incr i -1; set d [ string trim [ string range $d 0 $i ] ] }
         if { [ string trim $c ] == "stroke-width" } { set w $d }
      }
      lappend widths($z) $a
      set width($z,$a) $w
   }
}
