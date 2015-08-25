# AutoDiab-Project

To use;
1. create a directory called "AutoDiab" in the edison's root directory.
2. Copy and paste all python scripts inside the directory.
3. Copy and paste the "test.sh" and "try.sh" scripts into the "/etc/init.d" directory of the Edison
4. Rebooting the Edison should run the script automatically to run the "control.py" script
5.On Eclipse or Android Studio, create a new android project and paste all android "java and xml"

Add the following permissions to the android manifest
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

Modify the "strings.xml" file in layout to:
<string name="app_name">AutoDiab</string>
    <string name="label">Click on Receive to Start!</string>
    <string name="action_settings">Settings</string>
    <string name="receive">Receive!</string>
    <string name="send">Send</string>
    <string name="close">Close</string>
    <string name="title_activity_readings">ReadingsActivity</string>
    <string name="hello_world">Hello world!</string>
	  <string name="post">Post Data!</string>
    <string name="loading">Waiting to post data</string>
