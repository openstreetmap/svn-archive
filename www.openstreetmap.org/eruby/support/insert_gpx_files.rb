#!/usr/bin/ruby

# FIXME:
# reject points which go too quickly

require 'osm/dao'
require 'osm/servinfo'
require 'time'
require 'zlib'
require 'rexml/parsers/sax2parser'
require 'rexml/text'
require 'net/smtp'

DEBUG = false
USER = 'openstreetmap'

# exit if the lockfile is there

lockfile = "/tmp/gpx_insert_running.#{USER}"

if File.file?(lockfile)
  $stderr << 'exiting, lock file present'
  exit!
else
  #otherwise make one
  File.open(lockfile, 'w') do |file|
    file << 'eh-oh'
  end
end

dao = OSM::Dao.instance

dao.delete_sheduled_gpx_files unless DEBUG

files = dao.get_scheduled_gpx_uploads

puts 'got files...' + files.to_s if DEBUG

files.each_hash do |row|
  puts '.'
  filename = row['tmpname']
  #copy the file here
  if DEBUG
    puts "execing: cp #{filename} #{filename}"
    `cp #{filename} .#{filename}`
  else
     puts "execing: scp 128.40.58.202:#{filename} .#{filename}" if DEBUG
    `scp 128.40.58.202:#{filename} .#{filename}`
  end

  `zcat /home/steve/bin#{filename} > /home/steve/bin#{filename}.yeah`

  realfile = ''
  $stderr << 'file exists: ' + File.file?('.' + filename + '.yeah').to_s

  if File.file?('.' + filename + '.yeah') && File.size('.' + filename + '.yeah') > 0
    realfile = '.' + filename + '.yeah'
  else
    realfile = '.' + filename
  end

  puts realfile if DEBUG

  points = 0
  poss_points = 0
  user_id = row['user_id'].to_i
  name = row['name']
  email_address = dao.details_from_user_id(user_id)['email']
  dbh = dao.get_connection #bit hacky, but we need a connection

  if File.file?( realfile ) && File.size( realfile ) > 0
    file = File.new( realfile )
    parser = REXML::Parsers::SAX2Parser.new( file )
  
    # got a file, we hope

    $stderr << "Inserting #{name} for user #{user_id.to_s} from file #{filename} \n" if DEBUG
  
    trackseg = 0
    
    gpx_id = row['id'].to_i

    if gpx_id == 0
      $stderr << "bad gpx number!\n"
      exit
    end

    $stderr << 'new gpx file id: ' + gpx_id.to_s + "\n" if DEBUG

    lat = -1
    lon = -1
    ele = -1
    date = Time.now();
    gotlatlon = false
    gotele = false
    gotdate = false

    parser.listen( :start_element,  %w{ trkpt }) do |uri,localname,qname,attributes| 
      lat = attributes['lat'].to_f
      lon = attributes['lon'].to_f
      gotlatlon = true
      poss_points += 1
    end
  
    parser.listen( :characters, %w{ ele } ) do |text|
      ele = text
      gotele = true
    end

    parser.listen( :characters, %w{ time } ) do |text|
      if text && text != ''
        date = Time.parse(text)
        gotdate = true
      end
    end

    parser.listen( :end_element, %w{ trkseg } ) do |uri, localname, qname|
      trackseg += 1
    end
  
    parser.listen( :end_element, %w{ trkpt } ) do |uri,localname,qname|
      if gotlatlon && gotdate
        ele = '0' unless gotele
        if lat < 90 && lat > -90 && lon > -180 && lon < 180
          sql = "insert into gps_points (latitude, longitude, altitude, timestamp, user_id, trackid, gpx_id) values (#{lat}, #{lon}, #{ele}, '#{date.strftime('%Y-%m-%d %H:%M:%S')}', #{user_id}, #{trackseg}, #{gpx_id})"
          points += 1
          dbh.query(sql)
        end
 
      end
      gotlatlon = false
      gotele = false
      gotdate = false

      #puts lat.to_s + ' ' + lon.to_s + ' ' + ele.to_s + ' ' + date.to_s
    end

    error = false;
    error_msg = ''
    begin
      parser.parse  
      dao.update_gpx_meta(gpx_id)
      dao.schedule_gpx_delete(gpx_id) if points == 0
    rescue Exception => e
      error = true
      error_msg = e.to_s
   
    end

  
    #get rid of the file so we don't insert it again

    #send them an email indicating success
  error = error || points == 0
  if email_address && email_address != ''
    msgstr = ''
    unless error
      msgstr = <<END_OF_MESSAGE
From: webmaster <webmaster@openstreetmap.org>
To: #{email_address}
Bcc: steve@fractalus.com
Subject: Your gpx file

Hi,

It looks like your gpx file, 
  
  #{name},

uploaded to OpenStreetMap's database ok with #{points} out of
a possible #{poss_points} track points in the GPX.

Please see the link below to find out more about uploading GPX
files to OpenStreetMap.

  http://wiki.openstreetmap.org/index.php/Upload

Have fun


END_OF_MESSAGE
    
    else
      error = false
      msgstr = <<END_OF_MESSAGE
From: webmaster <webmaster@openstreetmap.org>
To: #{email_address}
Bcc: steve@fractalus.com
Subject: Your gpx file

Hi,

It looks like your gpx file, #{name}, failed to get parsed ok.
Please consult the error message below If there is no error then
there may be no points in your file, or it isn't a GPX. If you
think this is a bug please have a look at how to report it:

  http://wiki.openstreetmap.org/index.php/Bug_Reporting

and about uploading in general:

  http://wiki.openstreetmap.org/index.php/Upload

Error message:

#{error_msg}

END_OF_MESSAGE


    end
      unless DEBUG
        Net::SMTP.start('127.0.0.1', 25) do |smtp|
          smtp.send_message msgstr.untaint,
                            'webmaster@openstreetmap.org'.untaint,
                            email_address.untaint

        end
      else
        puts 'I would send this email:'
        puts msgstr
      end
       
   end

  else
    # nil file encountered
    errortext = ''
      if name == ''
        errortext = '(blank file name)'
      else
        errortext = name
      end
      
      msgstr = <<END_OF_MESSAGE
From: webmaster <webmaster@openstreetmap.org>
To: #{email_address}
Bcc: steve@fractalus.com
Subject: Your gpx file

Hi,

Your gpx file, '#{errortext}' didn't get uploaded because it was
empty. This usually means it didn't upload from your browser correctly.
This can happen, try it again. If errors persist, please report a bug:

  http://wiki.openstreetmap.org/index.php/Bug_Reporting

and learn about uploading in general:

  http://wiki.openstreetmap.org/index.php/Upload

have fun


END_OF_MESSAGE
    if !DEBUG
      Net::SMTP.start('127.0.0.1', 25) do |smtp|
        smtp.send_message msgstr.untaint,
                         'webmaster@openstreetmap.org'.untaint,
                          email_address.untaint
      end
    else
      puts 'I would send this email:'
      puts msgstr
    end

  end

  if !DEBUG
    `./make_trace_pic.rb #{gpx_id}`
    dbh.query("update gpx_files set inserted = 1 where tmpname = '#{filename}'")
    puts "execing: scp #{realfile} 128.40.58.202:/home/osm/gpx/#{gpx_id}.gpx"
    `scp #{realfile} 128.40.58.202:/home/osm/gpx/#{gpx_id}.gpx`
    File.delete('/home/steve/bin' + filename)
    File.delete('/home/steve/bin' + filename + '.yeah')
   `ssh 128.40.58.202 rm #{filename}`
  end

end

File.delete(lockfile)

