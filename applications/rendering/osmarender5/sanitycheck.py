import re, sys

print_debug = 0
print_warnings = 1

#regexes to find definition and uses: 
#class="highway tunnel"
p  = re.compile('class=[\'"]([^\'"]+)[\'"]')
# .landuse-retail
p2 = re.compile('\s*\.([^\s{]+)[\s{]')
#<symbol xlink:href="#airport"
p_symb = re.compile('<symbol\s+xlink:href=[\'"]#([^\'"]+)[\'"]')
#id="airport"
p_symb2 = re.compile('\s*id=[\'"]([^\'"]+)[\'"]')

dict={}
dict2={}
symbol_used={}
symbol_def={}

f = open(sys.argv[1],'r') 

for line in f.readlines():
  m = p.search(line)
  m2= p2.match(line)
  m_symb = p_symb.search(line)
  m_symb2= p_symb2.search(line)


  if (m):
    for i in m.group(1).split():
      if i not in dict.keys():
        if print_debug: print "D: Used %s" % i
        dict[i]=1
  if (m2):
    if m2.group(1) in dict2.keys():
      print "E: Double definition of class %s" % m2.group(1)
    else:
      dict2[m2.group(1)]=1
      if print_debug: print "D: Defined %s" % m2.group(1)

  if (m_symb):
    i = m_symb.group(1)
    if i not in symbol_used.keys():
      if print_debug: print "D: Used symbol %s" % i
      symbol_used[i]=1
  if (m_symb2):
    if m_symb2.group(1) in symbol_def.keys():
      print "E: Double definition of symbol %s" % m_symb2.group(1)
    else:
      symbol_def[m_symb2.group(1)]=1
      if print_debug: print "D: Defined symbol %s" % m_symb2.group(1)

f.close()

# check CSS classes
for i in dict.keys():
  if i in dict2.keys():
    if print_debug: print "D: Defined and used class %s" % i
  else:
    print "E: Used but undefined class %s" % i

for i in dict2.keys():
  if not i in dict.keys():
    if print_warnings: print "W: Defined but unused class %s" % i


# check symbols
for i in symbol_used.keys():
  if i in symbol_def.keys():
    if print_debug: print "D: Defined and used symbol %s" % i
    1
  else:
    print "E: Used but undefined symbol %s" % i

for i in symbol_def.keys():
  if not i in symbol_used.keys():
    if print_warnings: print "W: Defined but unused symbol %s" % i