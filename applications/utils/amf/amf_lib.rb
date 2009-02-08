
# ====================================================================
# AMF write subroutines

# -----	putdata		envelope data into AMF writeable form
# -----	encodevalue	pack variables as AMF

def putdata(index,n)
	d =encodestring(index+"/onResult")
	d+=encodestring("null")
	d+=[-1].pack("N")
	d+=encodevalue(n)
end

def encodevalue(n)
	case n.class.to_s
		when 'Array'
			a=10.chr+encodelong(n.length)
			n.each do |b|
				a+=encodevalue(b)
			end
			return a
		when 'Hash'
			a=3.chr
			n.each do |k,v|
				a+=encodestring(k)+encodevalue(v)
			end
			return a+0.chr+0.chr+9.chr
		when 'String'
			return 2.chr+encodestring(n)
		when 'Bignum','Fixnum','Float'
			return 0.chr+encodedouble(n)
		when 'NilClass'
			return 5.chr;
	end
end

# -----	encodestring	encode string with two-byte length
# -----	encodedouble	encode number as eight-byte double precision float
# -----	encodelong		encode number as four-byte long

def encodestring(n)
	a,b=n.size.divmod(256)
	a.chr+b.chr+n
end

def encodedouble(n)
	[n].pack('G')
end

def encodelong(n)
	[n].pack('L')
end

# ====================================================================
# Read subroutines

# -----	getint		return two-byte integer
# -----	getlong		return four-byte long
# -----	getstring	return string with two-byte length
# ----- getdouble	return eight-byte double-precision float
# ----- getobject	return object/hash
# ----- getarray	return numeric array

def getint(s)
	s.getc*256+s.getc
end

def getlong(s)
	((s.getc*256+s.getc)*256+s.getc)*256+s.getc
end

def getstring(s)
	len=s.getc*256+s.getc
	s.read(len)
end

def getdouble(s)
	a=s.read(8).unpack('G')			# G big-endian, E little-endian
	a[0]
end

def getarray(s)
	len=getlong(s)
	arr=[]
	for i in (0..len-1)
		arr[i]=getvalue(s)
	end
	arr
end

def getobject(s)
	arr={}
	while (key=getstring(s))
		if (key=='') then break end
		arr[key]=getvalue(s)
	end
	s.getc		# skip the 9 'end of object' value
	arr
end

# -----	getvalue	parse and get value

def getvalue(s)
	case s.getc
		when 0;	return getdouble(s)			# number
		when 1;	return s.getc				# boolean
		when 2;	return getstring(s)			# string
		when 3;	return getobject(s)			# object/hash
		when 5;	return nil					# null
		when 6;	return nil					# undefined
		when 8;	s.read(4)					# mixedArray
				return getobject(s)			#  |
		when 10;return getarray(s)			# array
		else;	return nil					# error
	end
end
