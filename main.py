#!/usr/bin/env python

import usbdev, contournextusb

def main(argv):
    ud = usbdev.USBComm(idVendor=usbdev.ids.Bayer,
                         idProduct=usbdev.ids.Bayer.Contour)
    bc = contournextusb.BayerCOMM(ud)
    cnu = contournextusb.ContourUSB()
    #file_write = open("temp.txt", "w")
    
    for rec in bc.sync():
        cnu.record(rec)
    print "CNU Meter SN: "+bc.sn
    with open("temp.txt", "w") as file_write:
        print file_write.write("CNU Meter SN: "+bc.sn+"\n")             #writing serial number to file
        for resno, res in cnu.result.items():
            print '%s: %s %s %.1f %s' % (resno, res.testtime, type(res), res.value,           ##printing to console
                               ', '.join(res.resultflags))
            
            print file_write.write (str('%s: %s %s %.1f' % (resno, res.testtime, type(res), res.value)+"\n"))  ##writing output to file
        #file_write.close()
      
def type(res):
    rtype = {'mmol/L': 'Glucose', '1': 'Insulin', '3': 'Carbs'}
    x = str(res.unit)
    if x == 'mmol/L':
        return rtype['mmol/L']
    elif x == '1':
        return rtype['1']
    else:
        return rtype['3']
    
 
if __name__ == '__main__':
    import sys, usb.core, time
    x = 0 
    y = 0
    while y < 1:                                   #Loop until device is connected.
        device = usb.core.find(find_all = True)
        print " Connect a CNU Glucometer!!"
        time.sleep(2)
        for cfg in device:
            #sys.stdout.write('Decimal VendorID=' + str(cfg.idVendor) +' & ProductID=' + str(cfg.idProduct) + '\n')
            if str(cfg.idVendor) == "6777":				#break loop once CNU device is connected
            	x += 1
            	break
        #y += 1
        if x == 1:
            time.sleep(2)
            print "Program begins reading!!"
            time.sleep(2)
	    main(sys.argv)						#Start read data
            time.sleep(2)
            break
	       
