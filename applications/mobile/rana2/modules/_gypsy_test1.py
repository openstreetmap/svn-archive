#!/usr/bin/env python

# Note: this is sample code for the openmoko's GPS, and not part of rana project.

import dbus
from time import sleep

obj = dbus.SystemBus().get_object('org.freesmartphone.ousaged', '/org/freesmartphone/Usage')
dbus.Interface(obj, 'org.freesmartphone.Usage').RequestResource('GPS')

sleep(3600 * 24 * 365)
