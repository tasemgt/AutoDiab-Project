#!/usr/bin/python

from __future__ import absolute_import, print_function, unicode_literals

from optparse import OptionParser, make_option
from subprocess import *
import os
import sys
import socket
import time
import uuid
import dbus
import dbus.service
import dbus.mainloop.glib
try:
  from gi.repository import GObject
except ImportError:
  import gobject as GObject

class Profile(dbus.service.Object):         #Class to implement Serial port profile 
	fd = -1

	@dbus.service.method("org.bluez.Profile1",
					in_signature="", out_signature="")
	def Release(self):
		print("Release")
		mainloop.quit()

	@dbus.service.method("org.bluez.Profile1",
					in_signature="", out_signature="")
	def Cancel(self):
		print("Cancel")

	@dbus.service.method("org.bluez.Profile1",
				in_signature="oha{sv}", out_signature="")
	def NewConnection(self, path, fd, properties):
		self.fd = fd.take()
		print("NewConnection(%s, %d)" % (path, self.fd))

		#Creates server socket from file descriptor
		server_sock = socket.fromfd(self.fd, socket.AF_UNIX, socket.SOCK_STREAM)
		server_sock.setblocking(1)
		try:
		    print("Sending data to mobile App...")
		    with open("temp.txt", "r") as file_read:                #open up file as read
                	while True:
                    		tobluetooth = file_read.readline()          #read file line by line
                    		data = tobluetooth
                    		if len(data) == 0: break                    
                    		server_sock.send("%s" % data)               #send data as string.
		except IOError:
		    pass

		server_sock.close()
		#print()
		time.sleep(1.5)
		sys.exit("All done... Program is exiting!!!")


	@dbus.service.method("org.bluez.Profile1",
				in_signature="o", out_signature="")
	def RequestDisconnection(self, path):
		print("RequestDisconnection(%s)" % (path))

		if (self.fd > 0):
			os.close(self.fd)
			self.fd = -1

if __name__ == '__main__':
	dbus.mainloop.glib.DBusGMainLoop(set_as_default=True)

	bus = dbus.SystemBus()

	manager = dbus.Interface(bus.get_object("org.bluez",
				"/org/bluez"), "org.bluez.ProfileManager1")

	option_list = [
			make_option("-C", "--channel", action="store",
					type="int", dest="channel",
					default=None),
			]

	parser = OptionParser(option_list=option_list)

	(options, args) = parser.parse_args()

	options.uuid = "1101"                   #specifying a serial port uuid 
	options.psm = "3"
	options.role = "server"                 #Bluetooth server
	options.name = "Edison SPP Loopback"
	options.service = "spp char loopback"
	options.path = "/foo/bar/profile"
	options.auto_connect = False
	options.record = ""

	profile = Profile(bus, options.path)

	mainloop = GObject.MainLoop()

	opts = {
			"AutoConnect" :	options.auto_connect,
		}

	if (options.name):
		opts["Name"] = options.name

	if (options.role):
		opts["Role"] = options.role

	if (options.psm is not None):
		opts["PSM"] = dbus.UInt16(options.psm)

	if (options.channel is not None):
		opts["Channel"] = dbus.UInt16(options.channel)

	if (options.record):
		opts["ServiceRecord"] = options.record

	if (options.service):
		opts["Service"] = options.service

	if not options.uuid:
		options.uuid = str(uuid.uuid4())

	manager.RegisterProfile(options.path, options.uuid, opts)

	mainloop.run()


