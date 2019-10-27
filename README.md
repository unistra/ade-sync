# Sync utility for ADE

## Goal
Will fetch updates in ADE and publish AMQP message.

Last run is stored as timestamp in *output_file*. If not present, the script will fetch updates during the last 72 hours.

## Configuration
The script will use a configuration file. This is an example :

    {
      "ade": {
        "server": "1.2.3.4",
        "port": 3099,
        "username": "ade_user",
        "password": "grillade",
        "project_id": 10
      },
      "sync": {
        "output_file": "/tmp/ade-run.txt"
      },
      "rabbitmq": {
        "server": "4.3.2.1",
        "port": 5672,
        "username": "rabbit",
        "password": "carrot",
        "queue": "pompom"
      }
    }

## Build
### Pre-requisite
In order to install dependencies :

    mvn install:install-file -Dfile=lib/ADEAde.jar -DgroupId=com.adesoft -DartifactId=ade -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true 
    mvn install:install-file -Dfile=lib/ADEConfig.jar -DgroupId=com.adesoft -DartifactId=config -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true 
    mvn install:install-file -Dfile=lib/ADEJDom.jar -DgroupId=com.adesoft -DartifactId=jdom -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true
    mvn install:install-file -Dfile=lib/ADEMisc.jar -DgroupId=com.adesoft -DartifactId=misc -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true
    mvn install:install-file -Dfile=lib/ADEStubs.jar -DgroupId=com.adesoft -DartifactId=stubs -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true

### Package
Build with *maven*

    mvn package

## Run
You can run script with :

    java -jar ade-sync.jar -c config_file.json