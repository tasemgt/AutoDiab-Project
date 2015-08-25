#!/usr/bin/python

"""
	Program Created by Michael Tase 08/2015
	This program reads data from the text file, formats and posts to the url. 
"""

import json
import requests
import datetime
import time

url = 'https://red.shef.ac.uk/devices.json'


class Process(object):					#class processes record string into date, type, ad value strings
    rect = ''
    d = ''
    t = ''
    v = ''
    
    def __init__(self, rect):
        
        self.rect = rect
        rec = self.rect
        s = ' '
        
        self.d =  rec[rec.index(s)+1:rec.index(s)+13]
        self.t =  rec[rec.index(s)+14:rec.index(s)+15]
        self.v =  (rec[rec.index(s)+14:])[(rec[rec.index(s)+14:]).index(s)+1:]
        
       # print self.d
    def d(self):
        return self.d
    
    def t():
        return float(t)
    
    def v(self):
        return float(self.v)

class Meter(object):
    sn = ''
    kind = ''
    def __init__(self, rec):
        s = ' '
        self.sn = rec[rec.index(s)+11:]
        self.kind = 'CNU'
        
print "Sending data to web server........."   
with open("temp.txt", "r") as file_read:				#opens temporal file to read
    x = file_read.readline()
    meter = Meter(x)
    snn = meter.sn
    kindd = meter.kind
    lst = []
    while(True):
            x = file_read.readline()
            if len(x) == 0: break    
            pro = Process(x)							#A process object
            type = pro.t
            result = float(pro.v)
            date = pro.d
            dt = datetime.datetime.strptime(date, "%Y%m%d%H%M")		#formatting date using datetime module
            ud = long(time.mktime(dt.timetuple()))					#unix representation of date
            sd = str(ud)+"000"
            ld = long(sd)
            
            
            payload = {'count': None, 'readings':[{'readingDate':ld, 					#data to be sent in dictionary
						'type':type, 'result':result}], 'sn':snn, 'type':kindd}
            data = json.dumps(payload)													#json encoding
            lst.append(data)
    headers = {'Authorization': 'Basic emhlbmcuaHVpQGdtYWlsLmNvbTp1c2Vy', 
				'Accept': 'application/json', 'content-type': 'application/json'}
    
    for i in range(len(lst)):
        data = lst[i]
        res = requests.post(url, data = data, headers = headers)					#posting using requests.

    print str(res)        
    print "DATA SENT!!"       
            
            
            
            
