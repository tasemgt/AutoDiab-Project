#
# Copyright (C) 2011 Anders Hammarquist <iko@iko.pp.se>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

#Modified by Michael Tase, 2015
"Lowlevel communication with the meter"

import usb

class _Vendor(int):
    def __new__(self, vid, **kw):
        instance = super(_Vendor, self).__new__(self, vid)
        instance.__dict__ = kw
        return instance

class _vidDb(object):
    def __init__(self, **kw):
        self.__dict__ = kw

ids = _vidDb(Bayer = _Vendor(0x1a79, Contour=0x7410))  #Assigns VID and PID of CNU meter

class USBComm(object):                                  #Class creates a USB object for communication between the glucometer and intel edison
    """
    Communicate with USB HID devices which use Interrupt transfers
    to read and write data.

    It knows the low-level protocol used by the Bayer Contour USB
    glucose meter. If you need to talk some other protocol, refactor!
    """
    blocksize = 64
    
    def __init__(self, **kw):
        dev = usb.core.find(**kw)		#Find USB device
        try:
            dev.set_configuration()		#Set configuration
        except usb.core.USBError:
            pass
        config = dev.get_active_configuration()
        interface = usb.util.find_descriptor(config,				#Set interface
                                             bInterfaceClass=usb.CLASS_HID)
        if dev.is_kernel_driver_active(interface.index):
            dev.detach_kernel_driver(interface.index)

        interface.set_altsetting()
        usb.util.claim_interface(dev, interface)

        self.epin = usb.util.find_descriptor(interface, bEndpointAddress=0x83)      #end points to use as physical comm points for the meters
        self.epout = usb.util.find_descriptor(interface, bEndpointAddress=0x04)

    def read(self):                                     #A read method that takes in a blocksize of 64 bytes every iteration
        result = []
        while True:
            data = self.epin.read(self.blocksize)
            dstr = data.tostring()
            #assert dstr[:3] == 'ABC'
            print '<<<', repr(dstr)
            result.append(dstr[4:data[3]+4])
            if data[3] != self.blocksize-4 or data[6]==82:              #######
                break

        return ''.join(result)

    def write(self, data):
        remain = data
        while remain:
            now = remain[:self.blocksize-4]
            remain = remain[self.blocksize-4:]
            self.epout.write('\0\0\0' + chr(len(now)) + now) # + ('\0' * (self.blocksize - 4 - len(now))))
