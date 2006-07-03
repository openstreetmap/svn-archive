#!/usr/bin/ruby

require 'net/http'
require 'cgi'
require 'RMagick'

servers = { 'landsat' => 'landsat.openstreetmap.org',
	'gpx' => 'tile.openstreetmap.org',
	'streets' => 'tile.openstreetmap.org',
	'srtm' => 'tile.openstreetmap.org' }

urls = { 'landsat' => '/wms.cgi?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg',
	'gpx' => '/ruby/gpx.rbx?',
	'streets' => '/ruby/renderer.rb?',
	'srtm' => '/srtm.php?' }

type = { 'landsat' => 'jpeg',
	'gpx' => 'png',
	'streets' => 'png',
	'srtm' => 'png' }

ports = { 'landsat' => 3128,
	  'gpx' => 81,
	  'streets' => 80,
	  'srtm' => 80 }

layers = []

r = Apache.request
r.content_type = 'image/jpg'
t = Time.now + (60 * 60)
#r.headers_out.add('Expires', t.to_s)
#r.send_http_header

cgi = CGI.new

layers = cgi['layers'].split(',') 
if layers.length == 0
	layers[0] = 'landsat'
	layers[1] = 'streets'
end
width = cgi['WIDTH']
if width.length == 0
	width = '256'
end
height = cgi['HEIGHT']
if height.length == 0
	height = '128'
end
bbox = cgi['BBOX']
if bbox.length == 0
	bbox = '-0.175781,50.79220659667531,0,50.847732536102676' #-180,-90,180,90'
end

threads = []
images = {} 

for l in layers
	threads << Thread.new(l) { |myL|
	   begin
		if ! servers[ myL ] 
			next
		end

		res = Net::HTTP::Proxy(nil,nil).start(servers[myL],ports[myL]) { |http|
				http.read_timeout=30
				http.get( urls[myL] + "&width=" + width + "&height=" + height + "&bbox=" + bbox )
		}

		case res
		when Net::HTTPSuccess
			images[ myL ] = Magick::Image::from_blob(res.body)
		end
	   rescue Timeout::Error
	   end
	}
end

begin
	threads.each { |aThread|  aThread.join }
rescue Timeout::Error
end

im = Magick::Image.new(width.to_i,height.to_i)
for l in layers
	if images[ l ]
		im = im.composite(images[l][0], 0, 0, Magick::OverCompositeOp )
	end
end 

#puts "Content-type: image/jpg\n\n"
puts im.to_blob{ 
	self.format="JPG"
	self.quality = 60
}
