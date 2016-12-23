# APNs Mock Server

## Setup
Before you run the mock server you need to create a ssl certificate
```bash
openssl req -new -x509 -keyout private -out private -nodes
```

Converse it to DER format
```bash
openssl x509 -in private -out cert.crt -outform DER
```

Go to your JVM home directory and import the ssl certificate into the trusted certificates list:
```bash
cd $JAVA_HOME/jre/lib/security
sudo keytool -importcert -keystore cacerts -storepass changeit -file /path/to/cert.crt -trustcacerts
```

Make apnsmock runnable by doing:
```bash
chmod a+x apnsmock
```

## Usage
```
./apnsmock --cert "/absolute/path/to/private"
```