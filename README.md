# Unified Push Server Test Suite
Some tools and scripts for a UPS test suite

## Automation Tools

### WireMock
In order to stress-testing we must not talk directly to FCM, but to a mocked server that is provided by [WireMock](http://wiremock.org/) tool.

### Mock APNs
Simple server app that emulates the official APNs so stress-testing can be made with iOS devices as well. It has been forked from [aaronlevy's Mock APNS Server](https://github.com/aaronlevy/mockapns)

### Mocked Data Loader
A tool to produce mock data (push applications, variants and tokens) can be found into the [mock-data-loader](mock-data-loader) folder.

### Artillery
[Artillery](https://artillery.io/) is a command-line load testing tool that will send requests directly to UPS's REST API. 

## Usage
First step is to install and start up the mocked FCM server. In order to do it, `cd` to the path where you want to install it and run:
```
bash <(curl https://gist.githubusercontent.com/josemigallas/9577750d09f87aaaa570c64d5ce8b58e/raw/83124a8596c93a862bcaefbb2dad4522c5d60828/Start%2520Up%2520WireMock)
```
This script will create the server and start it.
To start it again use:
```
java -jar path/to/server/wiremock-standalone-2.4.1.jar --port 3000
```
After this, start up the APNs mock server (setup instructions here) by running:
```
./apnsmock --cert "/absolute/path/to/certificate"
```
> Warning: please mind that mock-apns will start up even if the path or certificate is wrong.

Once both are running, start your UPS setting `-Dcustom.aerogear.fcm.push.host` parameter with the fake FCM URL and `-Dcustom.aerogear.apns.push.host` with the Mock APNs local IP:
```
path/to/jboss/bin/standalone.sh -b 0.0.0.0 --server-config=standalone-full.xml -Dcustom.aerogear.fcm.push.host=http://localhost:3000/fcm/send -Dcustom.aerogear.apns.push.host=127.0.0.1
```
> Note: in case you need to install a local UPS follow the [installation guide](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#server-installation).

And that's it, any notification request will be sent to the mocked backend.

Next step is run the actual load tests. Firstly install Artillery:
```
npm install -g artillery
```
Then download the ups-artillery.yml file and simply run it:
```
artillery run ups-artillery.yml
```
> Note: modify the YAML file the way it suits your spec. Authorization header must be filled with "appId:secret" b64 encoded. More info in the [artillery documentation](https://artillery.io/docs/script_reference.html).
