Android + MQTT & FTP remote camera
============================

This is an Android camera app that connects to an MQTT server and lets you
remote-control it. Images are triggered by sending a "shutter" message and the
camera subsequently sends the image to FTP server, Also it is timelapsed image capturing.

Build
-------------------------
Run below commands in Colab:
```
!apt remove java-common
!apt install openjdk-8-jdk
!wget https://dl.google.com/android/repository/commandlinetools-linux-9123335_latest.zip
!mkdir -p sdk
!unzip commandlinetools-linux-9123335_latest.zip -d sdk
!yes | ./sdk/cmdline-tools/bin/sdkmanager --sdk_root=/content/sdk "tools"
!git clone https://github.com/marzban2030/mqtt_camera
!chmod -c 755 /content/mqtt_camera/android/gradlew
!export ANDROID_HOME=/content/sdk && cd /content/mqtt_camera/android && ./gradlew assembleDebug
from google.colab import files
files.download('/content/mqtt_camera/android/app/build/outputs/apk/app-debug.apk')
```
Note: Maybe URLs and paths are different for you, So manually change these.

Releases version 
----------------
Ver 2.1 changes:

Added True/False result on `camera/{uuid}/image` MQTT topic

Ver 2.0 changes:

Added new stable FTP image transfer method

Removed old HTTP/HTTPS and FTP image transfer methods

Removed HTTP/HTTPS setting in menu

Removed PHP script

Ver 1.4 changes:

Fixed some handles to prevent hanging while network is unreachable.

Ver 1.3 changes:

Added timelapsed capturing

Fixed some handles to prevent hanging while network is unreachable

Ver 1.2 changes:

Added FTP image transfer

Added FTP settings in menu

Added changing taken image quality 

Added setting up image quality in menu

Ver 1.1 changes:

Removed MQTT image transfer 

Added HTTP/HTTPS image transfer

Added HTTP/HTTPS settings in menu

Removed Python script

Added PHP script

Ver 1.0 changes:

Nothing added or removed

Features
--------

* MQTT connectivity
* Remote trigger
* Battery & charge monitoring
* Focus-lock mode
* Screen dimming feature
* Multiple camera support
* FTP image transfer
* Timelapsed image capturing

Usage
-----

Install the app and configure MQTT for your server/broker. Once the app is
running and MQTT is configured, MQTT will attempt to connect whenever the app
is open and will disconnect when navigated away.
Setup ftp connection in app settings menu to getting images.
Timelapsed image capturing period time can be set in "MqttRemote.java" file from "int delay=60000" in milliseconds.

MQTT Messages
-------------

When the app first starts, it generates a distinct UUID for that given device.
All messages will be sent under the topic `camera/{uuid}` where `{uuid}` is the
UUID of the camera. This ID can be configured in the settings if desired.

### Topic: `camera/{uuid}/status`

This represents the MQTT connectivity status of the camera.

Payload:

* `connected` - the app is running and connected to the MQTT broker
* `disconnected` - the app has disconnected from the MQTT broker

### Topic: `camera/{uuid}/shutter`

Send this message to trigger the shutter. Any payload will be ignored.

### Topic: `camera/{uuid}/image`

The payload of this message is "True" if image sent to FTP server.

### Topic: `camera/{uuid}/battery`

Payload:

A JSON object that looks like this:

```
{
  "charging": true,
  "percentage": 96,
  "plugType": "ac"
}
```

### Topic: `camera/{uuid}/focus`

When this message is received, the camera will re-focus the image.

### Topic: `camera/{uuid}/setting/auto_focus`

Payload:

* `1` (default) — the camera will attempt to re-focus the image each time the
  shutter is triggered
* `0` — the camera will not change focus when the shutter is triggered. This is
  meant to be used with the `focus` message.

### Topic: `camera/{uuid}/setting/dim_screen`

* `1` — the camera will dim the screen and disable the preview image to help
  save battery
* `0` (default) — the camera will show the preview image and leave the screen
  brightness at the default
