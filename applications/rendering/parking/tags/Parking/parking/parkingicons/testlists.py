# -*- coding: utf-8 -*-
# by kay - basic functions


def unboth(list,sideindex):
    """ Replace in a list all rows with 'both' with two rows with 'left' and 'right'
    """
    list_both = list[:]
    list_both.reverse()
    list = []
    while len(list_both)>0:
        row = list_both.pop()
        #print "row=", row
        side = row[sideindex]
        if side=='both':
            row_l = row[:]
            row_l[sideindex] = 'left'
            #print 'bothl:', row_l
            list += [row_l]
            row_r = row[:]
            row_r[sideindex] = 'right'
            #print 'bothr:', row_r
            list += [row_r]
        else:
            #print side, ":", row
            list += [row]
    return list

pc_disc_maxstay = []
pc_disc_maxstay += [[1231,49.9,50.1,'1 h',49.0,50.1,49.9,50.1,49.9,50.1,'left']]
pc_disc_maxstay += [[1232,49.9,50.1,'2 h',49.0,50.1,49.9,50.1,49.9,50.1,'right']]
pc_disc_maxstay += [[1233,49.9,50.1,'3 h',49.0,50.1,49.9,50.1,49.9,50.1,'both']]

print "-----------"
print pc_disc_maxstay
print "-----------"

pc_disc_maxstay = unboth(pc_disc_maxstay,10)
"""
pc_disc_maxstay_both = pc_disc_maxstay[:]
pc_disc_maxstay_both.reverse()
pc_disc_maxstay = []
while len(pc_disc_maxstay_both)>0:
    pc_dm = pc_disc_maxstay_both.pop()
    print "pc_dm=", pc_dm
    side = pc_dm[10]
    if side=='both':
        pc_dm_l = pc_dm[:]
        pc_dm_l[10] = 'left'
        print 'bothl:', pc_dm_l
        pc_disc_maxstay += [pc_dm_l]
        pc_dm_r = pc_dm[:]
        pc_dm_r[10] = 'right'
        print 'bothr:', pc_dm_r
        pc_disc_maxstay += [pc_dm_r]
    else:
        print 'side:', pc_dm
        pc_disc_maxstay += [pc_dm]
"""

print "-----------"
print pc_disc_maxstay
print "-----------"

for pc_dm in pc_disc_maxstay:
    print pc_dm
    side = pc_dm[10]
    print str(pc_dm[0]) + " -> " + side
