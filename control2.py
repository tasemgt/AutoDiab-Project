#!/usr/bin/python
from subprocess import *
import time

call(["python", "/AutoDiab/main.py"])
time.sleep(1)
call(["python", "/AutoDiab/postData.py"])
