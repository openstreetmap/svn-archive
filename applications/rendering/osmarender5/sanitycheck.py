import re, sys

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
  m_symb2= p_symb2.match(line)


  if (m):
    for i in m.group(1).split():
      if i not in dict.keys():
        #print "Used %s" % i
        dict[i]=1
  if (m2):
    if m2.group(1) in dict2.keys():
      print "Double definition of class %s" % m2.group(1)
    else:
      dict2[m2.group(1)]=1
      #print "Defined %s" % m2.group(1)

  if (m_symb):
    i = m_symb.group(1)
    if i not in symbol_used.keys():
      print "Used symbol %s" % i
      symbol_used[i]=1
  if (m_symb2):
    if m_symb2.group(1) in symbol_def.keys():
      print "Double definition of symbol %s" % m_symb2.group(1)
    else:
      symbol_def[m_symb2.group(1)]=1
      print "Defined symbol %s" % m_symb2.group(1)

f.close()

for i in dict.keys():
  if i in dict2.keys():
    #print "Used class %s" % i
    1
  else:
    print "Used but undefined class %s" % i

for i in dict2.keys():
  if not i in dict.keys():
    print "Defined but unused class %s" % i


for i in symbol_used.keys():
  if i in symbol_def.keys():
    #print "Used symbol %s" % i
    1
  else:
    print "Used but undefined symbol %s" % i

for i in symbol_def.keys():
  if not i in symbol_used.keys():
    print "Defined but unused symbol %s" % i