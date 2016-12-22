# Unified Push Server Test Suite
Some tools and scripts for a UPS test suite

## MockData

Buld the software with maven:

```bash
mvn clean install
```

uncompress the produced archive in a directory of your choice:

```bash
tar xvfz target/aerogear-mock-data-loader-1.0-SNAPSHOT-bin.zip
```

Run the script:

```bash
aerogear-mock-data-loader-1.0-SNAPSHOT/bin/mock-data-loader.sh -u username -p password --apps 200 --variants 3 --tokens 15000
```

Running the script without arguments will show an help screen:

```bash
usage: mock-data-loader.sh -u|--username <username>-u|--password
                           <password>-a|--apps <TOTAL> -t|--tokens <TOTAL>
                           -v|--variants <TOTAL> [-c|--clientid <CLIENTID>
                           -U|--url <UPS URL>]
 -a,--apps <total>          Number of apps to be generated
 -c,--clientid <id>         Client id used to create the apps. Defaults to
                            <unified-push-server-js>
 -p,--password <password>   Username to be used to authenticate to the UPS
 -t,--tokens <total>        Number of tokens to be generated
 -u,--username <username>   Username to be used to authenticate to the UPS
 -U,--url <UPS URL>         URL to the UPS server. Defaults to
                            <http://localhost:8080>
 -v,--variants <total>      Number of variants to be generated

```

## Automation Tools
### WireMock
In order to stress-testing we must not talk directly to FCM, but to a mocked server that is provided by [WireMock](http://wiremock.org/) tool.

### Artillery
[Artillery](https://artillery.io/) is a command-line load testing tool that will send requests directly to UPS's REST API. 

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
Once it is running, start your UPS setting `-Dcustom.aerogear.fcm.push.host` parameter with the fake URL:
```
path/to/jboss/bin/standalone.sh -b 0.0.0.0 --server-config=standalone-full.xml -Dcustom.aerogear.fcm.push.host=http://localhost:3000/fcm/send
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
