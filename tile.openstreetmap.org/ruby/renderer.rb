#!/usr/bin/ruby
# styles: colour casing width dash z-index image text

require 'cgi'
require 'osm/dao.rb'
require 'rules.rb'
require 'RMagick'

class TaggedSegment
	attr_reader :segment, :style	
	attr_writer :segment, :style	
end

class Renderer

	def log2 (n,b)
		return Math.log(n) / Math.log(b)
	end

	def initialize(w,s,e,n,width,height)
		@gc = Magick::Draw.new
		@segment_styles = {}
		@node_styles = {}
		@tagged_segments = Array.new 
		@rules = RenderRules.new('new.xml')
		dao = OSM::Dao.instance
		@zoom = (log2((360*width)/(e-w),2)-9).round
		factor  = (@zoom<13) ? 0.5 : 0.5*(2**(@zoom-13))
		@nodes = dao.getnodes(n+(n-s)*factor,w-(e-w)*factor,s-(n-s)*factor,
				e+(e-w)*factor)
		@nodes.each do |id,node|
			keyvals = nil
			style = nil
			keyvals = get_kv(node.tags) unless node.tags == ''
			unless keyvals == nil
				@node_styles[id]=@rules.get_style(keyvals) 
			end
		end

		segments = dao.getlines(@nodes) unless @nodes=={}
		unless segments==nil
			id=0
			segments.each_value do |segment|
				@tagged_segments[id] = TaggedSegment.new
				@tagged_segments[id].segment = segment
				keyvals = nil
				#puts "TAGS"
				#puts segment.tags
				keyvals = get_kv(segment.tags) unless segment.tags == ''
				if keyvals == nil
					#puts "NO KEYVALS"
					@tagged_segments[id].style = {}
					@tagged_segments[id].style['casing'] = "black"
					@tagged_segments[id].style['colour'] = "yellow"
					@tagged_segments[id].style['width']=
						"0,0,0,0,0,0,0,0,0,1,1,1,4,6,8"
					@tagged_segments[id].style['z-index'] = 0
				else
					#puts "KEYVALS"
					#puts keyvals
					@tagged_segments[id].style=@rules.get_style(keyvals)
					#puts "STYLES"
					#puts @tagged_segments[id].style
				end
				id=id+1
			end
			#@tagged_segments.sort! {|a,b| a.style['z-index']<=>b.style['z-index']}
		end

		@proj = OSM::Mercator.new((s+n)/2,(w+e)/2,(e-w)/width,width,height)
		@width = width
		@height = height
		@gc.font_family('helvetica')
  		@gc.stroke_linejoin('round')
  		@gc.stroke_linecap('round')
	end

	def get_kv(tags)
		keyvals = {}
		tags.split(';').each do |kv|
			kv1 = kv.split('=')
			keyvals[kv1[0]] = kv1[1]
		end
		return keyvals
	end

	def draw_casing(seg)
		unless seg.style==nil or seg.style['casing']==nil 
			@gc.stroke_dasharray()
			@gc.stroke(seg.style['casing'])
			@gc.stroke_width(seg.style['width'].split(",")[@zoom-1].
					to_i+2)	
			node_a = @nodes[seg.segment.node_a_id]
			node_b = @nodes[seg.segment.node_b_id]
			unless node_a == nil or node_b == nil 
				x1 = @proj.x(node_a.longitude).to_i
				y1 = @proj.y(node_a.latitude).to_i
				x2 = @proj.x(node_b.longitude).to_i
				y2 = @proj.y(node_b.latitude).to_i
				@gc.line(x1,y1,x2,y2)
			end
		end
	end

	def draw_line(seg)
		unless seg.style==nil or 
				seg.style['colour']==nil 
			#puts "DRAWING LINE"
			#puts seg.style['colour']
			@gc.stroke(seg.style['colour'])
			@gc.stroke_width(seg.style['width'].
						split(",")[@zoom-1])	
			node_a = @nodes[seg.segment.node_a_id]
			node_b = @nodes[seg.segment.node_b_id]
			unless node_a == nil or node_b == nil 
				#puts "ACTUALLY DRAWING"
				x1 = @proj.x(node_a.longitude).to_i
				y1 = @proj.y(node_a.latitude).to_i
				x2 = @proj.x(node_b.longitude).to_i
				y2 = @proj.y(node_b.latitude).to_i 
				if seg.style['dash'] == nil
					@gc.stroke_dasharray()
				else	
					eval("@gc.stroke_dasharray(#{seg.style['dash']})")
				end
				@gc.line(x1,y1,x2,y2)
			end
		end
	end

	def draw_segment_name(seg)
		unless seg.segment==nil or 
				seg.style==nil or 
				seg.style['width']==nil 
			keyvals=get_kv(seg.segment.tags) unless 
					seg.segment.tags==''
			segwidth=(seg.style['width'].split(",")[@zoom-1]).to_i
			unless keyvals==nil or keyvals['name']==nil or segwidth==nil
				metrics=@gc.get_type_metrics(keyvals['name'])
				node_a = @nodes[seg.segment.node_a_id]
				node_b = @nodes[seg.segment.node_b_id]
				unless node_a == nil or node_b == nil 
					p=Array.new
					p[0] = {}
					p[1] = {}
					p[0]['x'] = @proj.x(node_a.longitude).to_i
					p[0]['y'] = @proj.y(node_a.latitude).to_i
					p[1]['x'] = @proj.x(node_b.longitude).to_i
					p[1]['y'] = @proj.y(node_b.latitude).to_i 
					length=line_length(p[0]['x'],p[0]['y'],p[1]['x'],p[1]['y'])
					if keyvals['name'] =~ /^[A-Z]+[0-9]+$/
						avx = p[0]['x'] + ((p[1]['x'] - p[0]['x'])/2)
						avy = p[0]['y'] + ((p[1]['y'] - p[0]['y'])/2)
						x1 = avx-metrics.width/2
						y1 = avy-(metrics.ascent+metrics.descent)/2
						x2 = avx+metrics.width/2
						y2 = avy+(metrics.ascent+metrics.descent)/2
						@gc.fill(seg.style['colour']) unless
							seg.style['colour']==nil
						@gc.rectangle(x1,y1,x2,y2)
						@gc.stroke("black")
						@gc.pointsize(10)
						@gc.stroke_width(1)
						@gc.text(x1,y2,keyvals['name'])
					elsif length >= metrics.width and @zoom>=13
						fs = (@zoom==13) ? 8:10
						interior_angle_text(p,fs,"black",keyvals['name'],
												segwidth,
												metrics.ascent+metrics.descent)	
					end
				end
			end
		end
	end

	def interior_angle_text(p,fs,colour,name,segwidth,text_ht)
		i=0
		i=1 unless p[1]['x'] > p[0]['x']
		p[i]['y'] += segwidth/2 + text_ht/2
		angle_text(p,fs,colour,name)
	end

	def angle_text(p,fs,colour,name)
		angle = slope_angle(p[0]['x'],p[0]['y'],p[1]['x'],p[1]['y'])
		i=0
		i=1 unless p[1]['x'] > p[0]['x']
		@gc.stroke(colour)
		@gc.stroke_width(1)
		@gc.pointsize(fs)
		@gc.translate(p[i]['x'],p[i]['y'])
		@gc.rotate(angle)
		@gc.text(0,0,name)
		@gc.rotate(-angle)
		@gc.translate(-p[i]['x'],-p[i]['y'])
	end

	def slope_angle(x1,y1,x2,y2)
		dx = x2-x1
		dy = y2-y1
		a = 90 
		unless dx==0
			a = (Math.atan(dy/dx)*(180/3.141592654)).round
		end
		return a
	end

	def line_length(x1,y1,x2,y2)
		dx = x2-x1
		dy = y2-y1
		return Math.sqrt(dx**2 + dy**2)
	end
		
	def draw_points_of_interest_icons()
		allnamedata = Array.new
		@nodes.each do |id,node|
			unless @node_styles[id]==nil 
				x = @proj.x(node.longitude).to_i
				y = @proj.y(node.latitude).to_i
				unless @node_styles[id]['image']==nil
					image = Magick::Image.read(@node_styles[id]['image']).first
					@gc.composite(x,y,0,0,image)
					x = x + image.bounding_box.width	
					y = y + image.bounding_box.height	
				end

				unless @node_styles[id]['text']==nil 
					fs=@node_styles[id]['text'].split(",")[@zoom-1].to_i
					if fs>0
						#puts "fs is #{fs}"
						namedata = {} 
						keyvals=get_kv(node.tags) unless node.tags==''
						unless keyvals['name']==nil
							namedata['x']=x
							namedata['y']=y 
							namedata['name'] = keyvals['name']
							namedata['fontsize'] = fs
							allnamedata.push(namedata)
						end
					end
				end
			end
		end
		return allnamedata
	end

	def draw_points_of_interest_names(allnamedata)
		@gc.stroke("black")
		@gc.stroke_width(1)
		@gc.text_antialias(false)
		@gc.font_weight(100)
		@gc.stroke_dasharray()
		allnamedata.each do |namedata|
			#puts "name is #{namedata['name']}"
			words = namedata['name'].split(" ")
			x = namedata['x'].to_i
			y = namedata['y'].to_i
			@gc.pointsize(namedata['fontsize']+2)
			words.each do |word|
				#puts "putting at #{x} #{y}: #{word}"
				@gc.text(x,y,word)
				metrics = @gc.get_type_metrics(word)
				y += (metrics.ascent+metrics.descent)
			end
		end
	end

	def draw_points_of_interest()
		allnamedata = draw_points_of_interest_icons()
		draw_points_of_interest_names(allnamedata)
	end

	def draw_segment_casings()
		unless @tagged_segments == nil
			@tagged_segments.each do |segment|
				#puts segment.style['z-index']
				draw_casing(segment)
			end
		end
	end

	def draw_segments()
		unless @tagged_segments == nil
			@tagged_segments.each do |segment|
				draw_line(segment)
				draw_segment_name(segment)
			end
		end
	end

	def draw()
		draw_segment_casings()
		draw_segments()
		draw_points_of_interest()
	end

	def out()
		canvas = Magick::Image.new(@width, @height) {
        self.background_color = 'transparent'
     	}
		@gc.draw(canvas)

		#transparent_canvas=canvas.transparent('transparent',
		#		Magick::TransparentOpacity)
		#transparent_canvas.format = 'PNG'
		#puts transparent_canvas.to_blob
		canvas.format = 'PNG'
		puts canvas.to_blob

	end
end

def blankpng(width, height)
		canvas = Magick::Image.new(width, height) {
        self.background_color = 'transparent'
     	}
		canvas.format = 'PNG'
		puts canvas.to_blob
end

r = Apache.request
r.content_type = 'image/png'
cgi = CGI.new


bbox = cgi['bbox']
bbox = cgi['BBOX'] if bbox == ''
bbox = '-0.75,51.02,-0.7,51.07' if bbox == ''

bbox.gsub!('%2D', '-')
bbox.gsub!('%2E', '.')
bbox.gsub!('%2C', ',')

bbox = bbox.split(',')

bllon = bbox[0].to_f
bllat = bbox[1].to_f
trlon = bbox[2].to_f
trlat = bbox[3].to_f

width = cgi['width'].to_i
width = cgi['WIDTH'].to_i if width == 0
width = 400 if width == 0

height = cgi['height'].to_i
height = cgi['HEIGHT'].to_i if height == 0
height = 320 if height == 0

#width = 400 
#height = 320
#bllon = -0.5
#bllat = 51.30
#trlon = -0.4
#trlat = 51.40

tile_too_big = width > 256 || height > 256 || ( (trlon - bllon) * (trlat - bllat) ) > 0.0035

if tile_too_big
  blankpng(width, height)
else
  renderer = Renderer.new(bllon, bllat, trlon, trlat, width, height)
  renderer.draw()
  renderer.out()
end


