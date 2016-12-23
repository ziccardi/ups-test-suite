# Unified Push Server Test Suite
Some tools and scripts for a UPS test suite

## Automation Tools

### WireMock
In order to stress-testing we must not talk directly to FCM, but to a mocked server that is provided by [WireMock](http://wiremock.org/) tool.

### Mock APNs
In case of iOS devices we need to use a mock of the APNs, in this case provided by [Mock APNS Server](https://github.com/aerogear/mockapns).

### Mocked Data Loader
A tool to produce mock data (push applications, variants and tokens) can be found into the [mock-data-loader](mock-data-loader) folder.

### Artillery
[Artillery](https://artillery.io/) is a command-line load testing tool that will send requests directly to UPS's REST API. 

## Usage
First step is to install and start up both mocked servers.
#### FCM server
Firstly `cd` to the path where you want to install it and simply run the next script, it will create the server inside a folder and start it:
```
bash <(curl https://gist.githubusercontent.com/josemigallas/9577750d09f87aaaa570c64d5ce8b58e/raw/83124a8596c93a862bcaefbb2dad4522c5d60828/Start%2520Up%2520WireMock)
```

To start it again only use:
```
java -jar path/to/server/wiremock-standalone-2.4.1.jar --port 3000
```
#### APNs server
After this, install and run the APNs server following [this instructions](https://github.com/aerogear/mockapns).

Now, before starting UPS you will need to add the APNs Mock Server certificate to your JVM trusted certificates list. In order to do that convert it to DER format
```
openssl x509 -in path/to/certificate -out cert.crt -outform DER
```

Then go to your JVM home directory and import the ssl certificate into the trusted certificates list:
```
cd $JAVA_HOME/jre/lib/security
sudo keytool -importcert -keystore cacerts -storepass changeit -file /path/to/cert.crt -trustcacerts
```
You will be prompted to confirm it is a trusted certificate, enter `yes`.
> MacOS: run `$ /usr/libexec/java_home` to locate your $JAVA_HOME dir.

#### UPS
Once the ssl certificate has been added, start your UPS setting `-Dcustom.aerogear.fcm.push.host` and `-Dcustom.aerogear.apns.push.host` with the proper values:
```
path/to/jboss/bin/standalone.sh -b 0.0.0.0 --server-config=standalone-full.xml -Dcustom.aerogear.fcm.push.host=http://localhost:3000/fcm/send -Dcustom.aerogear.apns.push.host=127.0.0.1
```
> Note: in case you need to install a local UPS follow the [installation guide](https://aerogear.org/docs/unifiedpush/ups_userguide/index/#server-installation).

And that's it, any notification request will be sent to the mocked backend.

#### Run test suite
Next step is run the actual load tests. Firstly install Artillery:
```
npm install -g artillery
```

Then download the ups-artillery.yml file and simply run it:
```
artillery run ups-artillery.yml
```
> Note: modify the YAML file the way it suits your spec. Authorization header must be filled with "appId:secret" b64 encoded. More info in the [artillery documentation](https://artillery.io/docs/script_reference.html).
