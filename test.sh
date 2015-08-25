#!/bin/sh
#
#
# /etc/init.d/kcsplashscreen.sh
# 
#
# System startup script for console splash screens
#
### BEGIN INIT INFO
# Provides: test
# Required-Start:$all 
# Should-Start:   
# Required-Stop:  
# Should-Stop:    
# Default-Start:   2 3 4 5
# Default-Stop:
# Description:    test
### END INIT INFO

do_start(){
	/etc/init.d/try.sh 2>&1|tee /tmp/dump.txt &
	exit 0	
}

case "$1"  in
	start|"")
		do_start
		;;
	restart|reload|force-reload)
		echo "Error: argument  '$1' not supported" >&2
		exit 3
		;;
	stop)
		# no-op
		;;
	*)
		echo "usage :: [start/stop]">&2
		exit 3
		;;
esac

:













