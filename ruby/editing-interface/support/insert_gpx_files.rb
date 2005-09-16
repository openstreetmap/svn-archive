#!/usr/bin/ruby

# FIXME:
# reject points which go too quickly

require 'osm/dao'
require 'time'
require 'zlib'
require 'rexml/parsers/sax2parser'
require 'rexml/text'
require 'net/smtp'

begin
  File.open('/tmp/gpx_insert_running', 'r') do |file|
     

  end
  #$stderr << 'GPX insert already running, exiting...'
  exit! #file was there, lets exit
rescue Exception => e
  #$stderr << 'gpx_insert_running file not there, creating...'
  File.open('/tmp/gpx_insert_running', 'w') do |file|
    file << 'eh-oh'
  end

end

dao = OSM::Dao.instance

dao.delete_sheduled_gpx_files

files = dao.get_scheduled_gpx_uploads


files.each_hash do |row|
  filename = row['tmpname']
  tfile = File.new(filename)
  points = 0
  parser =  ''
  
  begin
    gfile = Zlib::GzipReader.new(tfile)
    parser = REXML::Parsers::SAX2Parser.new( gfile.read )
    $stderr << 'looks like a gzipped file'
  rescue Zlib::Error => e
    tfile.close
    file = File.new( filename )
    parser = REXML::Parsers::SAX2Parser.new( file )
    $stderr << 'looks like a plain file'
  end 

  # got a file, we hope

  user_uid = row['user_uid'].to_i
  original_name = row['originalname']

  $stderr << 'Inserting ' + original_name + ' for user ' + user_uid.to_s + ' from file ' + filename 
  
  
  trackseg = 0
  gpx_uid = dao.new_gpx_file(user_uid, original_name)

  if gpx_uid == 0
    $stderr << 'bad gpx number!'
    exit
  end

  dbh = dao.get_connection #bit hacky, but we need a connection

  $stderr << 'new gpx file uid: ' + gpx_uid.to_s

  lat = -1
  lon = -1
  ele = -1
  date = -1
  gotlatlon = false
  gotele = false
  gotdate = false

  parser.listen( :start_element,  %w{ trkpt }) do |uri,localname,qname,attributes| 
    lat = attributes['lat'].to_f
    lon = attributes['lon'].to_f
    gotlatlon = true
  end
  
  parser.listen( :characters, %w{ ele } ) do |text|
    ele = text.to_f
    gotele = true
  end

  parser.listen( :characters, %w{ time } ) do |text|
    if text && text != ''
      if text.match(/[0-9]{10}/).to_s == text
        #looks like a unix timestamp
        # This is an invalid gpx but we'll accept it to be nice.
        date = text.to_i * 1000
        gotdate = true
      else
        date = Time.parse(text).to_i * 1000
        gotdate = true
      end
    end
  end

  parser.listen( :end_element, %w{ trkseg } ) do |uri, localname, qname|
    trackseg += 1
  end
  
  parser.listen( :end_element, %w{ trkpt } ) do |uri,localname,qname| 
    if gotlatlon && gotdate
      ele = 0 unless gotele
      if lat < 90 && lat > -90 && lon > -180 && lon < 180
        sql = "insert into tempPoints (latitude, longitude, altitude, timestamp, uid, hor_dilution, vert_dilution, trackid, quality, satellites, last_time, visible, dropped_by, gpx_id) values (#{lat}, #{lon}, #{ele}, #{date}, #{user_uid}, -1, -1, #{trackseg}, 255, 0, #{Time.new.to_i * 1000}, 1, 0, #{gpx_uid})"
        points += 1
        dbh.query(sql)
  #      puts sql

      end
 

    end
    gotlatlon = false
    gotele = false
    gotdate = false

    #puts lat.to_s + ' ' + lon.to_s + ' ' + ele.to_s + ' ' + date.to_s
  end

  parser.parse
   
  
  #get rid of the file so we don't insert it again

  dbh.query("delete from gpx_to_insert where tmpname = '#{filename}'")
  File.delete(filename)

  #send them an email indicating success

  email_address = dao.email_from_user_uid(user_uid)

  if email_address && email_address != ''
    msgstr = <<END_OF_MESSAGE
From: webmaster <webmaster@openstreetmap.org>
To: #{email_address}
Bcc: steve@fractalus.com
Subject: Your gpx file

Hi,

It looks like your gpx file, #{original_name}, uploaded to OpenStreetMap's
database ok with #{points} points.

Have fun


END_OF_MESSAGE
   Net::SMTP.start('127.0.0.1', 25) do |smtp|
      smtp.send_message msgstr.untaint,
                       'webmaster@openstreetmap.org'.untaint,
                        email_address.untaint
    end
                                                    
  
  end

end

File.delete('/tmp/gpx_insert_running')

