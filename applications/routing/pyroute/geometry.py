import math

def bearing(a,b):
    dlat = math.radians(b[0] - a[0])
    dlon = math.radians(b[1] - a[1])

    dlon = dlon * math.cos(math.radians(a[0]))
    
    return(math.degrees(math.atan2(dlon, dlat)))

def distance(a,b):
    dlat = math.radians(a[0] - b[0])
    dlon = math.radians(a[1] - b[1])

    dlon = dlon * math.cos(math.radians(a[0]))
    #print "d = %f, %f" % (dlat, dlon)
    # todo: mercator proj
    dRad = math.sqrt(dlat * dlat + dlon * dlon)

    c = 40000 # earth circumference,km
    
    return(dRad * c)
    

if(__name__ == "__main__"):
    a = (51.477,-0.4856)
    b = (51.477,-0.4328)

    print bearing(a,b)
