# Sync utility for ADE

## Goal
Will fetch updates in ADE and publish AMQP message.

## Pre-requisite
In order to install dependencies :

    mvn install:install-file -Dfile=lib/ADEAde.jar -DgroupId=com.adesoft -DartifactId=ade -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true 
    mvn install:install-file -Dfile=lib/ADEConfig.jar -DgroupId=com.adesoft -DartifactId=config -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true 
    mvn install:install-file -Dfile=lib/ADEJDom.jar -DgroupId=com.adesoft -DartifactId=jdom -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true
    mvn install:install-file -Dfile=lib/ADEMisc.jar -DgroupId=com.adesoft -DartifactId=misc -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true
    mvn install:install-file -Dfile=lib/ADEStubs.jar -DgroupId=com.adesoft -DartifactId=stubs -Dversion=6.5.3c -Dpackaging=jar -DgeneratePom=true