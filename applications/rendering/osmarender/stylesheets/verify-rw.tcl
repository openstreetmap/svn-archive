puts "tclsh verify-rw.tcl startzoom endzoom  --> comparing railway widths, output to verify-rw.txt"

set za [ lindex $argv 0 ]
set ze [ lindex $argv 1 ]

source verify.tcl
set fs "%6s"

set allways [ list rail narrow preserved light tram subway funicular monorail yard con pla dis aba ]

set ersatzs [ array names ersatz ]

for { set z $za } { $z <= $ze } { incr z } {
   foreach ww $widths($z) {
      set www [ split $ww "-" ]
      if { [ lindex $www 0 ] == "railway" } {
         set w $width($z,$ww)
         if { [ string trim $w ] == "" } continue
         set way [ lindex $www 1 ]

         if { $way == "bridge" && [ lindex $www 2 ] == "casing" } { set bcax [ format $fs $w ]; continue }
         if { $way == "bridge" && [ lindex $www 2 ] == "core"   } { set bcox [ format $fs $w ]; continue }

         if {[lsearch $allways  $way] < 0} {puts "$way missing"}

         set j 2
         set k $j
         if { [ lindex $www $k ] == "1" || [ lindex $www $k ] == "2" } { incr k }

         if { [ lindex $www $j ] == "casing" } {
            set cas($z,$way) [ format $fs $w ]
         } elseif { [ lindex $www $k ] == "core" } {
            if { [ lindex $www $j ] != "2" } {
               set cor($z,$way) [ format $fs $w ]
            } else {
               set co2($z,$way) [ format $fs $w ]
            }
         }
      }
   }
}

for { set z $za } { $z <= $ze } { incr z } {
   foreach way $allways {
      if { [ lsearch $ersatzs $way ] >= 0 } { set e 1 } { set e 0 }

      set bca($z,$way) $bcax
      set bco($z,$way) $bcox

      if { $z < 16 } {
         set bca($z,$way) [ format $fs "" ]
         set bco($z,$way) [ format $fs "" ]
      }
      if { [ string trim $bca($z,$way) ] != "" } {
         set bdi($z,$way) [ format $fs [ expr "$bca($z,$way) - $bco($z,$way)" ] ]
      } else {
         set bdi($z,$way) [ format $fs "" ]
      }
      if { ![ info exists cas($z,$way) ] } {
         if { $e } {
            set cas($z,$way) $cas($z,$ersatz($way))
         } else {
            set cas($z,$way) [ format $fs "" ]
         }
      }
      if { ![ info exists cor($z,$way) ] } {
         if { $e } {
            set cor($z,$way) $cor($z,$ersatz($way))
         } else {
            set cor($z,$way) [ format $fs "" ]
         }
      }
      if { ![ info exists co2($z,$way) ] } {
         if { $e } {
            set co2($z,$way) $co2($z,$ersatz($way))
         } else {
            set co2($z,$way) [ format $fs "" ]
         }
      }

      if { [ string trim $cas($z,$way) ] != "" && [ string trim $cor($z,$way) ] != "" } {
         set dif($z,$way) [ format $fs [ expr "$cas($z,$way) - $cor($z,$way)" ] ]
      } else {
         set dif($z,$way) [ format $fs "" ]
      }
      if { [ string trim $co2($z,$way) ] != "" && [ string trim $cas($z,$way) ] != "" } {
         set bd2($z,$way) [ format $fs [ expr "$co2($z,$way) - $cas($z,$way)" ] ]
      } else {
         set bd2($z,$way) [ format $fs "" ]
      }
   }
}

set fo [ open verify-rw.txt w ]

puts $fo "  :  bcas bcore bdiff     cas  core  diff   core2 diff2  way" 

foreach way $allways {
   for { set z $za } { $z <= $ze } { incr z } {
   set x "$z,$way"
puts $fo "$z:$bca($x)$bco($x)$bdi($x)  $cas($x)$cor($x)$dif($x)  $co2($x)$bd2($x)  $way" 
   }
   puts $fo ""
}
close $fo