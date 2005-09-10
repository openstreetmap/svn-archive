#!/usr/bin/ruby

# FIXME:
# set a flag that we are running so we don't get more than one instance trying to upload the same file
# mail the user when the file has completed
# reject points which go too quickly

require 'osm/dao'
require 'time'
require 'zlib'
require "rexml/document"

dao = OSM::Dao.instance

dao.delete_sheduled_gpx_files

files = dao.get_scheduled_gpx_uploads

files.each_hash do |row|
  filename = row['tmpname']
  tfile = File.new( filename )

  doc = ''
  
  begin
    gfile = Zlib::GzipReader.new(tfile)
    doc = REXML::Document.new gfile.read
    puts 'looks like a gzipped file'
  rescue Zlib::Error => e
    tfile.close
    file = File.new( filename )
    doc = REXML::Document.new file
    puts 'looks like a plain file'
  end 

  # got a file, we hope

  user_uid = row['user_uid'].to_i
  original_name = row['originalname']

  puts 'Inserting ' + original_name + ' for user ' + user_uid.to_s + ' from file ' + filename 
  
  
  trackseg = 0
  gpx_uid = dao.new_gpx_file(user_uid, original_name)

  if gpx_uid == 0
    puts 'bad gpx number!'
    exit
  end

  dbh = dao.get_connection #bit hacky, but we need a connection

  puts 'new gpx file uid: ' + gpx_uid.to_s
  
  doc.elements.each('gpx/trk/trkseg') do |e|
    e.elements.each('trkpt') do |pt|
      lat = pt.attributes['lat'].to_f
      lon = pt.attributes['lon'].to_f
      ele = 0.0
      date = ''
      
      pt.elements.each('ele') do |e|
        ele = e.get_text.value.to_f
      end
      
      pt.elements.each('time') do |e|
        date = e.get_text.value
      end

#      puts '  got point ' + lat.to_s + ',' + lon.to_s + ',' + ele.to_s + ' at ' + (Time.parse(date).to_i * 1000).to_s

      if lat < 90 && lat > -90 && lon > -180 && lon < 180
        sql = "insert into tempPoints (latitude, longitude, altitude, timestamp, uid, hor_dilution, vert_dilution, trackid, quality, satellites, last_time, visible, dropped_by, gpx_id) values (#{lat}, #{lon}, #{ele}, #{Time.parse(date).to_i * 1000}, #{user_uid}, -1, -1, #{trackseg}, 255, 0, #{Time.new.to_i * 1000}, 1, 0, #{gpx_uid})"
       
        dbh.query(sql)

      end
      
    end    

    trackseg += 1
  end

  #get rid of the file so we don't insert it again

  dbh.query("delete from gpx_to_insert where tmpname = '#{filename}'")

end

