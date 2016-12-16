# Unified Push Server Test Suite
Some tools and scripts for a UPS test suite

## Automation Tools
### WireMock
In order to stress-testing we must not talk directly to FCM, but to a mocked server that is provided by [WireMock](http://wiremock.org/) tool.

## Usage
First step is to install and startp up the mocked FCM server. In order to do it, `cd` to the path where you want to install it and run:
```
bash <(curl https://gist.githubusercontent.com/josemigallas/9577750d09f87aaaa570c64d5ce8b58e/raw/83124a8596c93a862bcaefbb2dad4522c5d60828/Start%2520Up%2520WireMock)
```
This script will create the server and start it.
To start it again use:
```
java -jar path/to/server/wiremock-standalone-2.4.1.jar --port 3000
```
Once it is running, start your UPS setting `-Dcustom.aerogear.fcm.push.host` parameter with the facked URL:
```
path/to/jboss/bin/standalone.sh -b 0.0.0.0 --server-config=standalone-full.xml Dcustom.aerogear.fcm.push.host=http://localhost:3000/fcm/send
```
> Note: in case you need to install a local UPS follow the [installation guide](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#server-installation).

And that's it, any notification request will be sent to the mocked backend.
