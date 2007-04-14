#!/usr/bin/env ruby

=begin Copyright (C) 2005 Ben Gimpert (ben@somethingmodern.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Port to Ruby by Ben Gimpert
Jun 2005

Reference ellipsoids derived from Peter H. Dana's website-
http://www.utexas.edu/depts/grg/gcraft/notes/datum/elist.html
Department of Geography, University of Texas at Austin
Internet: pdana@mail.utexas.edu
3/22/95

Defense Mapping Agency. 1987b. DMA Technical Report: Supplement to
Department of Defense World Geodetic System 1984 Technical Report.
Part I and II. Washington, DC: Defense Mapping Agency

=end

module Tiger
  
  module Utm
  
    DEG_TO_RAD = Math::PI / 180
    RAD_TO_DEG = 180 / Math::PI
  
    ELLIPSOIDS = {
      :airy => ["Airy", 6377563, 0.00667054],
      :australian_national => ["Australian National", 6378160, 0.006694542],
      :bessel_1841 => ["Bessel 1841", 6377397, 0.006674372],
      :bessel_1841_nambia => ["Bessel 1841 (Nambia)", 6377484, 0.006674372],
      :clarke_1886 => ["Clarke 1866", 6378206, 0.006768658],
      :clarke_1880 => ["Clarke 1880", 6378249, 0.006803511],
      :everest => ["Everest", 6377276, 0.006637847],
      :fischer_1960_mercury => ["Fischer 1960 (Mercury)", 6378166, 0.006693422],
      :fischer_1968 => ["Fischer 1968", 6378150, 0.006693422],
      :grs_1967 => ["GRS 1967", 6378160, 0.006694605],
      :grs_1980 => ["GRS 1980", 6378137, 0.00669438],
      :helmert_1906 => ["Helmert 1906", 6378200, 0.006693422],
      :hough => ["Hough", 6378270, 0.00672267],
      :international => ["International", 6378388, 0.00672267],
      :krassovsky => ["Krassovsky", 6378245, 0.006693422],
      :modified_airy => ["Modified Airy", 6377340, 0.00667054],
      :modified_everest => ["Modified Everest", 6377304, 0.006637847],
      :modified_fischer_1960 => ["Modified Fischer 1960", 6378155, 0.006693422],
      :south_american_1969 => ["South American 1969", 6378160, 0.006694542],
      :wgs_60 => ["WGS 60", 6378165, 0.006693422],
      :wgs_66 => ["WGS 66", 6378145, 0.006694542],
      :wgs_72 => ["WGS-72", 6378135, 0.006694318],
      :wgs_84 => ["WGS-84", 6378137, 0.00669438],
    }

    def Utm.utm_letter_designator(lat)
      # This routine determines the correct UTM letter designator for the given latitude
      # Written by Chuck Gantz- chuck.gantz@globalstar.com
      # ported to Ruby by Ben Gimpert- ben@somethingmodern.com
      if ((84 >= lat) && (lat >= 72))
        return "X"
      elsif ((72 > lat) && (lat >= 64))
        return "W"
      elsif ((64 > lat) && (lat >= 56))
        return "V"
      elsif ((56 > lat) && (lat >= 48))
        return "U"
      elsif ((48 > lat) && (lat >= 40))
        return "T"
      elsif ((40 > lat) && (lat >= 32))
        return "S"
      elsif ((32 > lat) && (lat >= 24))
        return "R"
      elsif ((24 > lat) && (lat >= 16))
        return "Q"
      elsif ((16 > lat) && (lat >= 8))
        return "P"
      elsif ((8 > lat) && (lat >= 0))
        return "N"
      elsif ((0 > lat) && (lat >= -8))
        return "M"
      elsif ((-8 > lat) && (lat >= -16))
        return "L"
      elsif ((-16 > lat) && (lat >= -24))
        return "K"
      elsif ((-24 > lat) && (lat >= -32))
        return "J"
      elsif ((-32 > lat) && (lat >= -40))
        return "H"
      elsif ((-40 > lat) && (lat >= -48))
        return "G"
      elsif ((-48 > lat) && (lat >= -56))
        return "F"
      elsif ((-56 > lat) && (lat >= -64))
        return "E"
      elsif ((-64 > lat) && (lat >= -72))
        return "D"
      elsif ((-72 > lat) && (lat >= -80))
        return "C"
      else
        raise "latitude is outside the UTM limits"
      end
    end
  
    def Utm.to_utm(lat, long, reference_ellipsoid_symbol = :wgs_84)
      # converts lat/long to UTM coords. Equations from USGS Bulletin 1532
      # North latitudes are positive, South latitudes are negative
      # East longitudes are positive, West longitudes are negative
      # lat and long are in decimal degrees
      # Written by Chuck Gantz- chuck.gantz@globalstar.com
      # ported to Ruby by Ben Gimpert- ben@somethingmodern.com
  
      reference_ellipsoid = ELLIPSOIDS[reference_ellipsoid_symbol]
      small_a = reference_ellipsoid[1]
      ecc_squared = reference_ellipsoid[2]
      k0 = 0.9996
    
      # make sure the longitude is between -180.00 .. 179.9
      long_temp = (long + 180) - (((long + 180) / 360).floor * 360) - 180
  
      lat_rad = lat * DEG_TO_RAD
      long_rad = long_temp * DEG_TO_RAD
    
      zone_number = ((long_temp + 180) / 6).floor + 1
      zone_number = 32 if (lat >= 56.0) && (lat < 64.0) && (long_temp >= 3.0) && (long_temp < 12.0)
      # special zones for Svalbard
      if (lat >= 72.0) && (lat < 84.0) 
        if (long_temp >= 0.0) && (long_temp < 9.0)
          zone_number = 31
        elsif (long_temp >= 9.0)  && (long_temp < 21.0)
          zone_number = 33
        elsif (long_temp >= 21.0) && (long_temp < 33.0)
          zone_number = 35
        elsif (long_temp >= 33.0) && (long_temp < 42.0)
          zone_number = 37
        end
      end
  
      long_origin = (zone_number - 1)*6 - 180 + 3  # +3 puts origin in middle of zone
      long_origin_rad = long_origin * DEG_TO_RAD
  
      ecc_prime_squared = ecc_squared / (1 - ecc_squared)
      
      n = small_a / Math.sqrt(1 - ecc_squared * Math.sin(lat_rad) * Math.sin(lat_rad))
      t = Math.tan(lat_rad) * Math.tan(lat_rad)
      c = ecc_prime_squared * Math.cos(lat_rad) * Math.cos(lat_rad)
      a = Math.cos(lat_rad) * (long_rad - long_origin_rad)
  
      m = small_a * (((1 - ecc_squared/4 - 3*ecc_squared*ecc_squared/64 - 5*ecc_squared*ecc_squared*ecc_squared/256)*lat_rad) - ((3*ecc_squared/8 + 3*ecc_squared*ecc_squared/32 + 45*ecc_squared*ecc_squared*ecc_squared/1024)*Math.sin(2*lat_rad)) + ((15*ecc_squared*ecc_squared/256 + 45*ecc_squared*ecc_squared*ecc_squared/1024)*Math.sin(4*lat_rad)) - ((35*ecc_squared*ecc_squared*ecc_squared/3072)*Math.sin(6*lat_rad)))
    
      utm_easting = k0*n*(a+(1-t+c)*a*a*a/6 + (5-18*t+t*t+72*c-58*ecc_prime_squared)*a*a*a*a*a/120) + 500000.0
      utm_northing = k0*(m+n*Math.tan(lat_rad)*(a*a/2+(5-t+9*c+4*c*c)*a*a*a*a/24 + (61-58*t+t*t+600*c-330*ecc_prime_squared)*a*a*a*a*a*a/720))
      utm_northing += 10000000.0 if lat < 0  # offset for southern hemisphere
      utm_zone = "#{zone_number}#{utm_letter_designator(lat)}"
  
      return [utm_easting, utm_northing, utm_zone]
    end
  
    def Utm.to_ll(utm_easting, utm_northing, utm_zone, reference_ellipsoid_symbol = :wgs_84)
      # converts UTM coords to lat/long.  Equations from USGS Bulletin 1532 
      # East longitudes are positive, West longitudes are negative. 
      # North latitudes are positive, South latitudes are negative
      # lat and long are in decimal degrees. 
      # Written by Chuck Gantz- chuck.gantz@globalstar.com
      # ported to Ruby by Ben Gimpert- ben@somethingmodern.com
  
      reference_ellipsoid = ELLIPSOIDS[reference_ellipsoid_symbol]
      small_a = reference_ellipsoid[1]
      ecc_squared = reference_ellipsoid[2]
      k0 = 0.9996
      e1 = (1-Math.sqrt(1-ecc_squared))/(1+Math.sqrt(1-ecc_squared))
      x = utm_easting - 500000.0
      y = utm_northing
      utm_zone =~ /(\d+)([A-Z])/
      zone_number = $1.to_i
      zone_letter = $2
      y -= 10000000.0 if (zone_letter[0] - ?N) < 0
  
      long_origin = (zone_number - 1)*6 - 180 + 3  # +3 puts origin in middle of zone
      ecc_prime_squared = ecc_squared / (1 - ecc_squared)
      m = y / k0
      mu = m / (small_a*(1-ecc_squared/4-3*ecc_squared*ecc_squared/64-5*ecc_squared*ecc_squared*ecc_squared/256))
  
      phi1_rad = mu + (3*e1/2-27*e1*e1*e1/32)*Math.sin(2*mu) + (21*e1*e1/16-55*e1*e1*e1*e1/32)*Math.sin(4*mu) +(151*e1*e1*e1/96)*Math.sin(6*mu)
      phi1 = phi1_rad * RAD_TO_DEG
  
      n1 = small_a/Math.sqrt(1-ecc_squared*Math.sin(phi1_rad)*Math.sin(phi1_rad))
      t1 = Math.tan(phi1_rad)*Math.tan(phi1_rad)
      c1 = ecc_prime_squared*Math.cos(phi1_rad)*Math.cos(phi1_rad)
      r1 = small_a*(1-ecc_squared)/((1-ecc_squared*Math.sin(phi1_rad)*Math.sin(phi1_rad)) ** 1.5)
      d = x / (n1*k0)
  
      lat = phi1_rad - (n1*Math.tan(phi1_rad)/r1)*(d*d/2-(5+3*t1+10*c1-4*c1*c1-9*ecc_prime_squared)*d*d*d*d/24 + (61+90*t1+298*c1+45*t1*t1-252*ecc_prime_squared-3*c1*c1)*d*d*d*d*d*d/720)
      lat = lat * RAD_TO_DEG
  
      long = (d-(1+2*t1+c1)*d*d*d/6+(5-2*c1+28*t1-3*c1*c1+8*ecc_prime_squared+24*t1*t1)*d*d*d*d*d/120)/Math.cos(phi1_rad)
      long = long_origin + (long * RAD_TO_DEG)
  
      return [lat, long]
    end
  
  end

end
