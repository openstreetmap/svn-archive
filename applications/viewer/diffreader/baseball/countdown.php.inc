<?php 

function countdown($year, $month, $day, $hour, $minute)
{
   // date/time when the competition ends
   $end_date = mktime($hour, $minute, 0, $month, $day, $year, -1);

   $secs= $end_date - time();
   if ($secs < 0) $secs = 0;
   
   $days_left = floor($secs/60/60/24);
   $hours_left = floor(($secs - $days_left*60*60*24)/60/60);
   $minutes_left = floor(($secs - $days_left*60*60*24 - $hours_left*60*60)/60);
   
   $bgcolour = "transparent";
   if ($days_left==0) $bgcolour = "#FFA";
   if ($days_left==0 && $hours_left<4) $bgcolour = "YELLOW";
   
   if ($days_left>30) {
      $countdown= "This project finishes on ". date("F j", $end_date) . "\n";
   
   } elseif ($secs==0) {
      $countdown= "This project has finished! It finished on ". date("F j Y", $end_date) . "\n";
      
   } else {
      $countdown = "";
      if ($days_left>0) $countdown .= $days_left." days ";
      if ($days_left<2) {
         $countdown .= $hours_left." hours ";
         if ($days_left==0 && $hours_left < 5) {
            $countdown .= $minutes_left." minutes ";
         }
      }
      $countdown .= "remaining";
      
      $countdown = "<span style=\"padding:3px; background:$bgcolour\">" . $countdown . "</span>\n";
   }
   
   return $countdown;
}