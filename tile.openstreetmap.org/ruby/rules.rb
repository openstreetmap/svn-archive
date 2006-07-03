#!/usr/bin/ruby
require 'xml/libxml'

class RenderRules
	# Opens the xml file and reads it into an easier and simplified structure
	def initialize(xmlfile)
		@rules = Array.new 
		document = XML::Document.file(xmlfile)
		document.find("//ruleset/rule").each do |rule|
			currule = {}
			currule['conditions'] = {}
			currule['style'] = {}
			rule.find('condition').each do |condition|
				currule['conditions'][condition['k']] = condition['v']
			end
			rule.find('style').each do |s|
				currule['style']['colour'] = s['colour']
				currule['style']['casing'] = s['casing']
				currule['style']['width'] = s['width']
				currule['style']['dash'] = s['dash']
				currule['style']['z-index'] = s['z-index'].to_i
				currule['style']['image'] = s['image']
				currule['style']['text'] = s['text']
			end
			@rules.push(currule) 
		end
	end

	# TODO: OO this
	def get_style(supplied_keyvals)
		prevhits = 0
		style = {}
		@rules.each do |rule|
			hits = 0
			supplied_keyvals.each do |supplied_key,supplied_val|
				unless rule['conditions'][supplied_key] == nil
					if rule['conditions'][supplied_key] == supplied_val
						hits = hits + 1
					else
						hits = 0
						break
					end
				end
			end

			if hits > prevhits
				prevhits = hits
				style = rule['style']
			end
		end

		style['casing'] = 'black' if style['casing']==nil and 
					style['dash'] == nil
		#style['casing'] = nil if style['casing'] = 'none'
		style['colour'] = 'green' if style['colour']==nil
		style['width'] = "0,0,0,0,0,0,0,0,0,1,1,1,4,6,8" if style['width']==nil
		style['z-index'] = 0 if style['z-index']==nil

		return style
	end
end
