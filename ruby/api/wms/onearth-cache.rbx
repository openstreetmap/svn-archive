require 'net/http'
require 'cgi'
require 'RMagick'
include Math
include Magick

def bbox2tilecache(bb)

width = 256
height = 128
bbox = bb.split(",")
bbox[0] = bbox[0].to_f
bbox[1] = bbox[1].to_f
bbox[2] = bbox[2].to_f
bbox[3] = bbox[3].to_f

mdt = 0.5 #max degree per pixel
ts = 512 #tile size
minlv = 1
maxlv = 12

span = bbox[2] - bbox[0]
level = -1 + ( 1 - ( Math.log( span / (mdt * ts) ) / Math.log(2) ) ).ceil

if level < minlv then level = minlv end
if level > maxlv then level = maxlv end

tiledeg = mdt * ts / (2 ** (level - 1))
spany = (180 / tiledeg).ceil
spanx = (360 / tiledeg).ceil
xe = -180 + (tiledeg * spanx)
xs = 90 - (tiledeg * spany)
ltspan = (90 - xs) / spany
lnspan =  (xe + 180) / spanx

lon = bbox[0]
lat = bbox[1]

bboxs = []

tn = 0
ts = 0
tw = 0
te = 0

0.upto(3) do |x|

case x
when 0 #nw
 lon = bbox[0]
 lat = bbox[3]
when 1 #ne
 lon = bbox[2]
 lat = bbox[3]
when 2 #sw
 lon = bbox[0]
 lat = bbox[1]
when 3 #se
 lon = bbox[2]
 lat = bbox[1]
end

i = ( (lon + 180) / lnspan).floor
j = ( (90 - lat) / ltspan).floor


if i >= 0 && i < spanx && j >= 0 && j < spany then
	n = 90 - (j * ltspan)
	s = 90 - ((j+1) * ltspan)
	w = -180 + (i * lnspan)
	e = -180 + ((i+1) * lnspan)
	tmp = w.to_s + "," + s.to_s + "," + e.to_s + "," + n.to_s
	if x == 0
		bboxs[0] = tmp
		tn = n
		ts = s
		tw = w
		te = e
	elsif x == 1 && tmp != bboxs[0]
		bboxs[1] = tmp
		te = e
	elsif x == 2 &&  tmp != bboxs[0]
		bboxs[2] = tmp
		ts = s
	elsif x == 3 && bboxs[1] != nil && bboxs[2] != nil
		bboxs[3] = tmp
	end
		
end

end
bboxs[4] = tw.to_s + "," + ts.to_s + "," + te.to_s + "," + tn.to_s

return bboxs

end

servers = { 'landsat' => 'onearth.jpl.nasa.gov'}

urls = { 'landsat' => '/wms.cgi?request=GetMap&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg'}

type = { 'landsat' => 'jpeg'}

proxy = { 'landsat' => true }

layers = []

cgi = CGI.new
layers = cgi['layers'].split(',') 
if layers.length == 0
	layers[0] = 'landsat'
end
width = cgi['width']
if width.length == 0
	width = '256'
end
height = cgi['height']
if height.length == 0
	height = '128'
end
bb = cgi['BBOX']
if bb.length == 0
	bb = "-180,-90,180,90" #'-0.175781,50.79220659667531,0,50.847732536102676' #-180,-90,180,90'
end

bbox = bbox2tilecache( bb )

threads = []
images = {} 

0.upto(3) do |b|
	if bbox[b] != nil then
		threads << Thread.new(b) { |myB|
			res = Net::HTTP::Proxy('localhost',3128).start(servers['landsat']) { |http|
				http.get( urls['landsat'] + "&width=512&height=512&bbox=" + bbox[b])
			}
			case res
			when Net::HTTPSuccess
				images[ myB ] = Image::from_blob(res.body)
			end
		}
	end
end

threads.each { |aThread|  aThread.join }

if images[3]
	w = 1024
	h = 1024
elsif images[2]
	w = 512
	h = 1024
elsif images[1]
	w = 1024
	h = 512
else
	w = 512
	h = 512
end

im = Image.new(w,h)
0.upto(3) do |i|
	if images[i]
		case i 
		when 0
			x = 0
			y = 0
		when 1
			x = 512
			y = 0
		when 2
			x = 0
			y = 512
		when 3
			x = 512
			y = 512
		end
		im = im.composite(images[i][0], x, y, Magick::OverCompositeOp )
	end
end 

bba = bb.split(",")
bbb = bbox[4].split(",")

y1 = h * (bbb[3].to_f - bba[3].to_f) / (bbb[3].to_f - bbb[1].to_f)
y2 = h * (bbb[3].to_f - bba[1].to_f) / (bbb[3].to_f - bbb[1].to_f)
x1 = w * (bba[0].to_f - bbb[0].to_f) / (bbb[2].to_f - bbb[0].to_f)
x2 = w * (bba[2].to_f - bbb[0].to_f) / (bbb[2].to_f - bbb[0].to_f)

im = im.crop(x1, y1, x2-x1, y2-y1, true)
im = im.resize(256, 128)
#puts "Content-type: image/jpg\n\n"
puts im.to_blob { 
	self.format="JPG"
	self.quality = 60
}
