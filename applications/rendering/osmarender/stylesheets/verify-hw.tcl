puts "tclsh verify-hw.tcl startzoom endzoom  --> comparing highway widths, output to verify-hw.txt"

set za [ lindex $argv 0 ]
set ze [ lindex $argv 1 ]

source verify.tcl
set fs "%5s"

set allways [ list motorway motorway-link trunk trunk-link primary primary-link \
secondary secondary-link tertiary tertiary-link unclassified unsurfaced residential livingstreet cycleroad pedestrian service track bridleway cycleway aisle footway path steps ford ]

set ersatz(residential)  "unclassified"
set ersatz(cycleroad)    "residential"
set ersatz(livingstreet) "residential"
set ersatz(path)         "footway"
set ersatz(ford)         "unclassified"
set ersatzs [ array names ersatz ]

for { set z $za } { $z <= $ze } { incr z } {
   foreach ww $widths($z) {
      set www [ split $ww "-" ]
      if { [ lindex $www 0 ] == "highway" } {
         set w $width($z,$ww)
         if { [ lindex $www 2 ] == "link" } { set j 2 } { set j 1 }
         set k $j; incr k
         set l $k; incr l
         set way [ join [ lrange $www 1 $j ] "-" ]
         if {[lsearch $allways  $way] < 0} {puts "$way missing"}

         if { [ lsearch $www "bridge" ] > 0 } {
            if { [ lsearch $www "casing" ] > 0 } {
               set bca($z,$way) [ format $fs $w ]
            }
            if { [ lsearch $www "core" ] > 0 } {
               set bco($z,$way) [ format $fs $w ]
            }
         } elseif { [ lsearch $www "cy1" ] > 0 } {
            set cy1($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "cy2" ] > 0 } {
            set cy2($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "mr1" ] > 0 } {
            set mr1($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "mr2" ] > 0 } {
            set mr2($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "mrcy1" ] > 0 } {
            set mc1($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "mrcy2" ] > 0 } {
            set mc2($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "mrcy3" ] > 0 } {
            set mc3($z,$way) [ format $fs $w ]
         } elseif { [ lsearch $www "area" ] > 0 && [ lindex $www $l ] == "casing" } {
            set are($z,$way) [ format $fs $w ]
         } elseif { [ lindex $www $k ] == "casing" } {
            set cas($z,$way) [ format $fs $w ]
         } elseif { [ lindex $www $k ] == "core" } {
            set cor($z,$way) [ format $fs $w ]
         } elseif { [ lindex $www $k ] == "core2" } {
            set co2($z,$way) [ format $fs $w ]
         }
      }
   }
}

for { set z $za } { $z <= $ze } { incr z } {
   foreach way $allways {
      if { [ lsearch $ersatzs $way ] >= 0 } { set e 1 } { set e 0 }

      if { ![ info exists bca($z,$way) ] } {
         if { $e } {
            set bca($z,$way) $bca($z,$ersatz($way))
         } else {
            set bca($z,$way) [ format $fs "" ]
         }
      }
      if { ![ info exists bco($z,$way) ] } {
         if { $e } {
            set bco($z,$way) $bco($z,$ersatz($way))
         } else {
            set bco($z,$way) [ format $fs "" ]
         }
      }
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
      if { [ string trim $cas($z,$way) ] != "" } {
         set dif($z,$way) [ format $fs [ expr "$cas($z,$way) - $cor($z,$way)" ] ]
      } else {
         set dif($z,$way) [ format $fs "" ]
      }
      if { [ string trim $bca($z,$way) ] != "" } {
puts $way
         set bd2($z,$way) [ format $fs [ expr "$bco($z,$way) - $cas($z,$way)" ] ]
      } else {
         set bd2($z,$way) [ format $fs "" ]
      }
      if { ![ info exists are($z,$way) ] } {
         if { $e } {
            set are($z,$way) $are($z,$ersatz($way))
         } else {
            set are($z,$way) [ format $fs "" ]
         }
      }
      if { ![ info exists cy1($z,$way) ] } {
         if { $e } {
            set cy1($z,$way) $cy1($z,$ersatz($way))
         } else {
            set cy1($z,$way) [ format $fs "" ]
         }
      }
      if { ![ info exists cy2($z,$way) ] } {
         if { $e } {
            set cy2($z,$way) $cy2($z,$ersatz($way))
         } else {
            set cy2($z,$way) [ format $fs "" ]
         }
      }
      if { [ string trim $cy2($z,$way) ] != "" } {
         set cca($z,$way) [ format $fs [ expr "$cy1($z,$way) - $cy2($z,$way)" ] ]
         set ccy($z,$way) [ format $fs [ expr "$cy2($z,$way) - $cor($z,$way)" ] ]
      } else {
         set cca($z,$way) [ format $fs "" ]
         set ccy($z,$way) [ format $fs "" ]
      }
      if { ![ info exists mr1($z,$way) ] } {
         set mr1($z,$way) [ format $fs "" ]
      }
      if { ![ info exists mr2($z,$way) ] } {
         set mr2($z,$way) [ format $fs "" ]
      }
      if { [ string trim $mr2($z,$way) ] != "" } {
         set mca($z,$way) [ format $fs [ expr "$mr1($z,$way) - $mr2($z,$way)" ] ]
         set mcy($z,$way) [ format $fs [ expr "$mr2($z,$way) - $cor($z,$way)" ] ]
      } else {
         set mca($z,$way) [ format $fs "" ]
         set mcy($z,$way) [ format $fs "" ]
      }
      if { ![ info exists mc1($z,$way) ] } {
         set mc1($z,$way) [ format $fs "" ]
      }
      if { ![ info exists mc2($z,$way) ] } {
         set mc2($z,$way) [ format $fs "" ]
      }
      if { ![ info exists mc3($z,$way) ] } {
         set mc3($z,$way) [ format $fs "" ]
      }
      if { [ string trim $mc2($z,$way) ] != "" } {
         set mma($z,$way) [ format $fs [ expr "$mc1($z,$way) - $mc2($z,$way)" ] ]
         set mmy($z,$way) [ format $fs [ expr "$mc2($z,$way) - $mc3($z,$way)" ] ]
         set mmm($z,$way) [ format $fs [ expr "$mc3($z,$way) - $cor($z,$way)" ] ]
      } else {
         set mma($z,$way) [ format $fs "" ]
         set mmy($z,$way) [ format $fs "" ]
         set mmm($z,$way) [ format $fs "" ]
      }

      if { ![ info exists co2($z,$way) ] } {
         set co2($z,$way) [ format $fs "" ]
      }
   }
}

set fo [ open verify-hw.txt w ]

puts $fo "  : bcas bcor bdif bdi2    cas core diff area cor2   cyc1 cyc2  ccy  cca    mr1  mr2  mcy  mca   mc1  mc2  mc3  mmm  mmy  mma  way" 

foreach way $allways {
   for { set z $za } { $z <= $ze } { incr z } {
   set x "$z,$way"
puts $fo "$z:$bca($x)$bco($x)$bdi($x)$bd2($x)  $cas($x)$cor($x)$dif($x)$are($x)$co2($x)  $cy1($x)$cy2($x)$ccy($x)$cca($x)  $mr1($x)$mr2($x)$mcy($x)$mca($x)  $mc1($x)$mc2($x)$mc3($x)$mmm($x)$mmy($x)$mma($x) $way" 
   }
   puts $fo ""
}
close $fo