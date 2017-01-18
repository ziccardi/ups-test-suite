# Mocked data loader

This tools can be used to populate an UPS server with mock applications, variants and tokens

## Build the software

To build from source, you need to have [maven](https://maven.apache.org/) installed on your machine.

```bash
mvn clean install
```

## Load the mock data

uncompress the produced archive in a directory of your choice:

```bash
tar xvfz target/aerogear-mock-data-loader-1.0.0-SNAPSHOT-bin.zip
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
