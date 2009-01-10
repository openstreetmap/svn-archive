#!/usr/bin/env python

# Note: this is sample code for the openmoko's GPS, and not part of rana project.

import dbus
from time import gmtime, strftime

obj = dbus.SystemBus().get_object('org.freesmartphone.ogpsd', '/org/freedesktop/Gypsy')

constatus = obj.GetConnectionStatus(dbus_interface='org.freedesktop.Gypsy.Device')
print 'ConnectionStatus: constatus = %u' % constatus,
print '%s' % constatus and '(TRUE)' or '(FALSE)'

fixstatus = obj.GetFixStatus(dbus_interface='org.freedesktop.Gypsy.Device')
print 'FixStatus: fixstatus = %d' % fixstatus,
if   fixstatus == 1: print '(NONE)'
elif fixstatus == 2: print '(2D)'
elif fixstatus == 3: print '(3D)'
else:                print '(INVALID)'

(fields, tstamp, lat, lon, alt) = obj.GetPosition(dbus_interface='org.freedesktop.Gypsy.Position')
print 'Position:',
print 'fields = %d,' % fields,
print 'tstamp = %d (%s),' % (tstamp, strftime('%F %T', gmtime(tstamp))),
print 'lat = %f (%s),' % (lat, fields & (1 << 0) and 'OK' or 'INVALID'),
print 'lon = %f (%s),' % (lon, fields & (1 << 1) and 'OK' or 'INVALID'),
print 'alt = %f (%s)'  % (alt, fields & (1 << 2) and 'OK' or 'INVALID')

(fields, pdop, hdop, vdop) = obj.GetAccuracy(dbus_interface='org.freedesktop.Gypsy.Accuracy')
print 'Accuracy:',
print 'fields = %d,' % fields,
print 'pdop = %f (%s),' % (pdop, fields & (1 << 0) and 'OK' or 'INVALID'),
print 'hdop = %f (%s),' % (hdop, fields & (1 << 1) and 'OK' or 'INVALID'),
print 'vdop = %f (%s)'  % (vdop, fields & (1 << 2) and 'OK' or 'INVALID')

(fields, tstamp, speed, heading, climb) = obj.GetCourse(dbus_interface='org.freedesktop.Gypsy.Course')
print 'Course:',
print 'fields = %d,' % fields,
print 'tstamp = %d (%s),' % (tstamp, strftime('%F %T', gmtime(tstamp))),
print 'speed = %f (%s),'   % (speed,   fields & (1 << 0) and 'OK' or 'INVALID'),
print 'heading = %f (%s),' % (heading, fields & (1 << 1) and 'OK' or 'INVALID'),
print 'climb = %f (%s)'    % (climb,   fields & (1 << 2) and 'OK' or 'INVALID')

time = obj.GetTime(dbus_interface='org.freedesktop.Gypsy.Time')
print 'Time: time = %d (%s)' % (time, strftime('%F %T', gmtime(time)))

satellites = obj.GetSatellites(dbus_interface='org.freedesktop.Gypsy.Satellite')
print 'Satellites:'
for (satIndex, sat) in enumerate(satellites):
        print '\tindex = %d,' % satIndex,
        print 'prn = %u,' % sat[0],
        print 'used = %u (%s),' % (sat[1], sat[1] and 'TRUE' or 'FALSE'),
        print 'elevation = %u,' % sat[2],
        print 'azimuth = %u,' % sat[3],
        print 'snr = %u' % sat[4] 