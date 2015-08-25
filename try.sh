#!/bin/sh
echo `date` >>/tmp/dump.txt 2>&1
python -u  /AutoDiab/control.py |tee -a /tmp/python.log & 
exit 0
