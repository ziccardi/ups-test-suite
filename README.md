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

Further steps in progress ...
