#!/usr/bin/env python

import dbus
from time import sleep

obj = dbus.SystemBus().get_object('org.freesmartphone.ousaged', '/org/freesmartphone/Usage')
dbus.Interface(obj, 'org.freesmartphone.Usage').RequestResource('GPS')

sleep(3600 * 24 * 365)
