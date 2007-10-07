#!/usr/bin/python2.4
import urllib, time, sys

def countOutstanding():
  url="http://tah.openstreetmap.org/Log/Requests/History/munin.php"
  munin = { 'pending.value': 0, 'new.value': 0, 'active.value': 0, 'done.value': 0 }
  try:
    file = urllib.urlopen(url)
    for line in file:
      key,value = line.split(" ")
      munin[key] = int(value)
    file.close()
  except:
    return 1000000
  return int(munin['pending.value'])+int(munin['new.value'])

def checkOutstanding():
  outstanding = countOutstanding()
  while outstanding>20:
    print "Lots (%s) outstanding, pausing" % outstanding
    time.sleep(30)
    outstanding = countOutstanding()
  print "%s outstanding - continuing" % outstanding

def go(xmin,ymin,xmax,ymax,xskip,yskip,name):
  totalStartTime = time.time()
  count = 0
  for x in range(xskip,xmax+1):
    if x==xskip:
      ystart=yskip
    else:
      ystart=ymin
    for y in range(ystart,ymax+1):
      if not count%50:
        checkOutstanding()
      print "%s,%s # (%s in %s seconds):" % (x,y,count+1,time.time()-totalStartTime),
      sys.stdout.flush()
      url = "http://dev.openstreetmap.org/~ojw/NeedRender/?x=%s&y=%s&priority=2&src=bulk0.1_%s" % (x,y,name)
      startTime = time.time()
      file = urllib.urlopen(url)
      print file.read(),
      sys.stdout.flush()
      file.close()
      elapsed = time.time() - startTime
      #print time.time(), startTime, elapsed
      if elapsed > 0.2:
        print "request took %s. Sleeping for %s" % (elapsed,elapsed * 4)
        sys.stdout.flush()
        time.sleep(elapsed * 4)
      count += 1
    print "completed column %s." % x
    print "next %s,%s # " % (x+1,ymin)
  print "completed all"

if __name__=='__main__':
  sys.stderr.write("Don't run this. Write a python script like the below, customise, and run that.\n\n")
  print """#!/usr/bin/python
import bulkRequest

xmin,ymin = 2033,1356 # Top left position on the level 12 map grid
xmax,ymax = 2034,1358 # Bottom right position on the leve 12 map grid
xskip,yskip = xmin,ymin # Use this if you interrupt the script and need to start it part way through, otherwise leave as is.
name = "bulkrequest" # Change this to a short descriptive name e.g. rjm_uk
bulkRequest.go(xmin,ymin,xmax,ymax,xskip,yskip,name)
"""
