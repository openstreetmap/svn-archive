import os,re,sys,shutils

r = re.compile('(\d+)_(\d+)$')
layer='tile'

basepath = sys.argv[1]
for z in [0,6,12]:
  for i in range(0,5):
    layerpathcomp = "%s_%d_%d" % (layer,z,i)
    if os.path.isdir(os.path.join(basepath,layerpathcomp)):
     for f in os.listdir(os.path.join(basepath,layerpathcomp)):
      m = r.match(f)
      if m:
        base_x,base_y = m.groups()
        base_x,base_y = int(base_x), int(base_y)
        print "mkdir", os.path.join(basepath,"%s_%d" % (layer,z))
        print "mkdir", os.path.join(basepath,"%s_%d" % (layer,z),"%04d" % base_x)
        print "move from ",os.path.join(basepath,f),"to",os.path.join(basepath,"%s_%d" % (layer,z),"%d" % base_x,"%d" % base_y)
        os.mkdir(os.path.join(basepath,"%s_%d" % (layer,z)))
        os.mkdir(os.path.join(basepath,"%s_%d" % (layer,z),"%04d" % base_x))
        os.path.join(basepath,f),"to",os.path.join(basepath,"%s_%d" % (layer,z),"%d" % base_x,"%d" % base_y)
