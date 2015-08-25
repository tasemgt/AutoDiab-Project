#!/usr/bin/python
from subprocess import *
import time

call(["python", "/AutoDiab/main.py"])
print "Turning on Bluetooth services...."
time.sleep(2)
call(["rfkill", "unblock", "bluetooth"])
print "Firing up radio...."
time.sleep(2)					#Giving the bluetooth chip time to fire up!
call(["hciconfig", "hci0", "up"])
print "Making Bluetooth discoverable!"
time.sleep(2)
call(["hciconfig", "hci0", "piscan"])
#call(["hciconfig", "hci0", "sspmode"])
print "Starting Bluetooth connection!!"
time.sleep(2)
print "Waiting for bluetooth connection from mobile App!!!"
call(["python", "/AutoDiab/bluetoothspp.py"])
print "Turning Bluetooth off......"
time.sleep(1)
call(["hciconfig", "hci0", "down"])







