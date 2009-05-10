class GeocoderController < ApplicationController
  require 'uri'
  require 'net/http'
  require 'rexml/document'

  def search
    query = params[:query]
    results = Array.new

    query.sub(/^\s+/, "")
    query.sub(/\s+$/, "")

    if query.match(/^[+-]?\d+(\.\d*)?\s*[\s,]\s*[+-]?\d+(\.\d*)?$/)
      results.push search_latlon(query)
    elsif query.match(/^\d{5}(-\d{4})?$/)
      results.push search_us_postcode(query)
    elsif query.match(/^(GIR 0AA|[A-PR-UWYZ]([0-9]{1,2}|([A-HK-Y][0-9]|[A-HK-Y][0-9]([0-9]|[ABEHMNPRV-Y]))|[0-9][A-HJKS-UW])\s*[0-9][ABD-HJLNP-UW-Z]{2})$/i)
      results.push search_uk_postcode(query)
      results.push search_osm_namefinder(query)
    elsif query.match(/^[A-Z]\d[A-Z]\s*\d[A-Z]\d$/i)
      results.push search_ca_postcode(query)
    else
      results.push search_osm_namefinder(query)
      results.push search_geonames(query)
    end

    results_count = count_results(results)

    render :update do |page|
      page.replace_html :sidebar_content, :partial => 'results', :object => results

      if results_count == 1
        position = results.collect { |s| s[:results] }.compact.flatten[0]
        page.call "setPosition", position[:lat].to_f, position[:lon].to_f, position[:zoom].to_i
      else
        page.call "openSidebar"
      end
    end
  end
  
  def description
    results = Array.new

    lat = params[:lat]
    lon = params[:lon]

    results.push description_osm_namefinder("cities", lat, lon, 2)
    results.push description_osm_namefinder("towns", lat, lon, 4)
    results.push description_osm_namefinder("places", lat, lon, 10)
    results.push description_geonames(lat, lon)

    render :update do |page|
      page.replace_html :sidebar_content, :partial => 'results', :object => results
      page.call "openSidebar"
    end
  end

private

  def search_latlon(query)
    results = Array.new

    # decode the location
    if m = query.match(/^([+-]?\d+(\.\d*)?)\s*[\s,]\s*([+-]?\d+(\.\d*)?)$/)
      lat = m[1].to_f
      lon = m[3].to_f
    end

    # generate results
    if lat < -90 or lat > 90
      return { :source => "Internal", :url => "http://openstreetmap.org/", :error => "Latitude #{lat} out of range" }
    elsif lon < -180 or lon > 180
      return { :source => "Internal", :url => "http://openstreetmap.org/", :error => "Longitude #{lon} out of range" }
    else
      results.push({:lat => lat, :lon => lon,
                    :zoom => APP_CONFIG['postcode_zoom'],
                    :name => "#{lat}, #{lon}"})

      return { :source => "Internal", :url => "http://openstreetmap.org/", :results => results }
    end
  end

  def search_us_postcode(query)
    results = Array.new

    # ask geocoder.us (they have a non-commercial use api)
    response = fetch_text("http://rpc.geocoder.us/service/csv?zip=#{escape_query(query)}")

    # parse the response
    unless response.match(/couldn't find this zip/)
      data = response.split(/\s*,\s+/) # lat,long,town,state,zip
      results.push({:lat => data[0], :lon => data[1],
                    :zoom => APP_CONFIG['postcode_zoom'],
                    :prefix => "#{data[2]}, #{data[3]}, ",
                    :name => data[4]})
    end

    return { :source => "Geocoder.us", :url => "http://geocoder.us/", :results => results }
  rescue Exception => ex
    return { :source => "Geocoder.us", :url => "http://geocoder.us/", :error => "Error contacting rpc.geocoder.us: #{ex.to_s}" }
  end

  def search_uk_postcode(query)
    results = Array.new

    # ask npemap.org.uk to do a combined npemap + freethepostcode search
    response = fetch_text("http://www.npemap.org.uk/cgi/geocoder.fcgi?format=text&postcode=#{escape_query(query)}")

    # parse the response
    unless response.match(/Error/)
      dataline = response.split(/\n/)[1]
      data = dataline.split(/,/) # easting,northing,postcode,lat,long
      postcode = data[2].gsub(/'/, "")
      zoom = APP_CONFIG['postcode_zoom'] - postcode.count("#")
      results.push({:lat => data[3], :lon => data[4], :zoom => zoom,
                    :name => postcode})
    end

    return { :source => "NPEMap / FreeThePostcode", :url => "http://www.npemap.org.uk/", :results => results }
  rescue Exception => ex
    return { :source => "NPEMap / FreeThePostcode", :url => "http://www.npemap.org.uk/", :error => "Error contacting www.npemap.org.uk: #{ex.to_s}" }
  end

  def search_ca_postcode(query)
    results = Array.new

    # ask geocoder.ca (note - they have a per-day limit)
    response = fetch_xml("http://geocoder.ca/?geoit=XML&postal=#{escape_query(query)}")

    # parse the response
    if response.get_elements("geodata/error").empty?
      results.push({:lat => response.get_text("geodata/latt").to_s,
                    :lon => response.get_text("geodata/longt").to_s,
                    :zoom => APP_CONFIG['postcode_zoom'],
                    :name => query.upcase})
    end

    return { :source => "Geocoder.CA", :url => "http://geocoder.ca/", :results => results }
  rescue Exception => ex
    return { :source => "Geocoder.CA", :url => "http://geocoder.ca/", :error => "Error contacting geocoder.ca: #{ex.to_s}" }
  end

  def search_osm_namefinder(query)
    results = Array.new

    # ask OSM namefinder
    response = fetch_xml("http://gazetteer.openstreetmap.org/namefinder/search.xml?find=#{escape_query(query)}")

    # parse the response
    response.elements.each("searchresults/named") do |named|
      lat = named.attributes["lat"].to_s
      lon = named.attributes["lon"].to_s
      zoom = named.attributes["zoom"].to_s
      place = named.elements["place/named"] || named.elements["nearestplaces/named"]
      type = named.attributes["info"].to_s.capitalize
      name = named.attributes["name"].to_s
      description = named.elements["description"].to_s

      if name.empty?
        prefix = ""
        name = type
      else
        prefix = "#{type} "
      end

      if place
        distance = format_distance(place.attributes["approxdistance"].to_i)
        direction = format_direction(place.attributes["direction"].to_i)
        placename = format_name(place.attributes["name"].to_s)
        suffix = ", #{distance} #{direction} of #{placename}"

        if place.attributes["rank"].to_i <= 30
          parent = nil
          parentrank = 0
          parentscore = 0

          place.elements.each("nearestplaces/named") do |nearest|
            nearestrank = nearest.attributes["rank"].to_i
            nearestscore = nearestrank / nearest.attributes["distance"].to_f

            if nearestrank > 30 and
               ( nearestscore > parentscore or
                 ( nearestscore == parentscore and nearestrank > parentrank ) )
              parent = nearest
              parentrank = nearestrank
              parentscore = nearestscore
            end
          end

          if parent
            parentname = format_name(parent.attributes["name"].to_s)

            if  place.attributes["info"].to_s == "suburb"
              suffix = "#{suffix}, #{parentname}"
            else
              parentdistance = format_distance(parent.attributes["approxdistance"].to_i)
              parentdirection = format_direction(parent.attributes["direction"].to_i)
              suffix = "#{suffix} (#{parentdistance} #{parentdirection} of #{parentname})"
            end
          end
        end
      else
        suffix = ""
      end

      results.push({:lat => lat, :lon => lon, :zoom => zoom,
                    :prefix => prefix, :name => name, :suffix => suffix,
                    :description => description})
    end

    return { :source => "OpenStreetMap Namefinder", :url => "http://gazetteer.openstreetmap.org/namefinder/", :results => results }
  rescue Exception => ex
    return { :source => "OpenStreetMap Namefinder", :url => "http://gazetteer.openstreetmap.org/namefinder/", :error => "Error contacting gazetteer.openstreetmap.org: #{ex.to_s}" }
  end

  def search_geonames(query)
    results = Array.new

    # ask geonames.org
    response = fetch_xml("http://ws.geonames.org/search?q=#{escape_query(query)}&maxRows=20")

    # parse the response
    response.elements.each("geonames/geoname") do |geoname|
      lat = geoname.get_text("lat").to_s
      lon = geoname.get_text("lng").to_s
      name = geoname.get_text("name").to_s
      country = geoname.get_text("countryName").to_s
      results.push({:lat => lat, :lon => lon,
                    :zoom => APP_CONFIG['geonames_zoom'],
                    :name => name,
                    :suffix => ", #{country}"})
    end

    return { :source => "GeoNames", :url => "http://www.geonames.org/", :results => results }
  rescue Exception => ex
    return { :source => "GeoNames", :url => "http://www.geonames.org/", :error => "Error contacting ws.geonames.org: #{ex.to_s}" }
  end

  def description_osm_namefinder(types, lat, lon, max)
    results = Array.new

    # ask OSM namefinder
    response = fetch_xml("http://gazetteer.openstreetmap.org/namefinder/search.xml?find=#{types}+near+#{lat},#{lon}&max=#{max}")

    # parse the response
    response.elements.each("searchresults/named") do |named|
      lat = named.attributes["lat"].to_s
      lon = named.attributes["lon"].to_s
      zoom = named.attributes["zoom"].to_s
      place = named.elements["place/named"] || named.elements["nearestplaces/named"]
      type = named.attributes["info"].to_s
      name = named.attributes["name"].to_s
      description = named.elements["description"].to_s
      distance = format_distance(place.attributes["approxdistance"].to_i)
      direction = format_direction((place.attributes["direction"].to_i - 180) % 360)
      prefix = "#{distance} #{direction} of #{type} "
      results.push({:lat => lat, :lon => lon, :zoom => zoom,
                    :prefix => prefix.capitalize, :name => name,
                    :description => description})
    end

    return { :type => types.capitalize, :source => "OpenStreetMap Namefinder", :url => "http://gazetteer.openstreetmap.org/namefinder/", :results => results }
  rescue Exception => ex
    return { :type => types.capitalize, :source => "OpenStreetMap Namefinder", :url => "http://gazetteer.openstreetmap.org/namefinder/", :error => "Error contacting gazetteer.openstreetmap.org: #{ex.to_s}" }
  end

  def description_geonames(lat, lon)
    results = Array.new

    # ask geonames.org
    response = fetch_xml("http://ws.geonames.org/countrySubdivision?lat=#{lat}&lng=#{lon}")

    # parse the response
    response.elements.each("geonames/countrySubdivision") do |geoname|
      name = geoname.get_text("adminName1").to_s
      country = geoname.get_text("countryName").to_s
      results.push({:prefix => "#{name}, #{country}"})
    end

    return { :type => "Location", :source => "GeoNames", :url => "http://www.geonames.org/", :results => results }
  rescue Exception => ex
    return { :type => "Location", :source => "GeoNames", :url => "http://www.geonames.org/", :error => "Error contacting ws.geonames.org: #{ex.to_s}" }
  end

  def fetch_text(url)
    return Net::HTTP.get(URI.parse(url))
  end

  def fetch_xml(url)
    return REXML::Document.new(fetch_text(url))
  end

  def format_distance(distance)
    return "less than 1km" if distance == 0
    return "about #{distance}km"
  end

  def format_direction(bearing)
    return "south-west" if bearing >= 22.5 and bearing < 67.5
    return "south" if bearing >= 67.5 and bearing < 112.5
    return "south-east" if bearing >= 112.5 and bearing < 157.5
    return "east" if bearing >= 157.5 and bearing < 202.5
    return "north-east" if bearing >= 202.5 and bearing < 247.5
    return "north" if bearing >= 247.5 and bearing < 292.5
    return "north-west" if bearing >= 292.5 and bearing < 337.5
    return "west"
  end

  def format_name(name)
    return name.gsub(/( *\[[^\]]*\])*$/, "")
  end

  def count_results(results)
    count = 0

    results.each do |source|
      count += source[:results].length if source[:results]
    end

    return count
  end

  def escape_query(query)
    return URI.escape(query, Regexp.new("[^#{URI::PATTERN::UNRESERVED}]", false, 'N'))
  end
end
