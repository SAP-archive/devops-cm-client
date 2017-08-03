# SAP Change Management Command Line Interface

Simple Command Line Interface to connect to external change management tools like SolMan ChaRM

## How to use the plugin
  - build the project with maven. ```mvn clean package```
  - create a temporary directory. ```mkdir tmp```
  - extract the commnand line interface into that folder ```tar -C -xvf modules/dist.cli/target/dist.cli-${project.version}.tar.gz```
  - run ```tmp/bin/cmclient --help```
 
## Available commands
In order to see the available commands call ```tmp/bin/cmclient --help``` (see above).

