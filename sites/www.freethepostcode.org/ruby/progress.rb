#!/usr/bin/ruby
# Figure out our progress, based on the current list
#
# (Reports which inner parts we have coverage for)

# To hold the list of postcode areas, and the inner numbers covered
areas = {}
infile = '/var/www/freethepostcode.org/currentlist'
puts "FreeThePostcode progress as of " + File.ctime(infile).to_s
puts

# Work through the current list
File.open(infile) { |file|
	file.each { |line| 
		lat,long,inner,outer = line.chomp.split(' ')

		re = /^([A-Z]+)(\d+)[A-Z]?$/
		md = re.match(inner)
		if md != nil then 
			area,number = md[1,2]

			if areas[area] == nil then
				areas[area] = {}
			end

			if areas[area][number] == nil then
				areas[area][number] = 1
			end
		end
	}
}

# Report what we found
areas.keys.sort.each { |area|
	numbers = []
	areas[area].each { |number,junk|
		numbers = numbers << number.to_i
	}
	puts area+" "+numbers.sort.join(",")
}
