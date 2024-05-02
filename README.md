## Important Notice

This public repository is read-only and no longer maintained.

![](https://img.shields.io/badge/STATUS-NOT%20CURRENTLY%20MAINTAINED-red.svg?longCache=true&style=flat)

---

# Description

Simple command line interface to handle basic change management related actions
in SAP Solution Manager via ODATA requests. The client is intended to be used
in continuous integration and continuous delivery scenarios and supports only
the actions necessary within those scenarios. See section _Usage_ for more details.

# Requirements
### SAP Solution Manager Functionality
 - SAP Solution Manager 7.2 SP6, SP7 -> cm_client v1.x
 - SAP Solution Manager 7.2 SP 8 and higher -> cm_client v2.0

### General Requirements
 - JDK 8 to build this project (to run the client JRE 8 is sufficient) OR
 - a Docker environment to run the Docker image

# Download and Installation
This command line client can be consumed either as a Java application from [maven.org](https://repo1.maven.org/maven2/com/sap/devops/cmclient/dist.cli) or as a Docker image from [hub.docker.com](https://hub.docker.com/r/ppiper/cm-client).

The public key for verifing the artifacts is available [here](https://keys.openpgp.org/vks/v1/by-fingerprint/D59BDC1A924385CFEE6AA5962F55B9DDAC28BFAF)

## Using the Docker Image

On a Linux machine, you can run:

`docker run --rm ppiper/cm-client cmclient --help`

This prints the help information of the CM Client. For a comprehensive overview of available commands, please read the [usage information](#usage) below.

### How to Build the Docker Image

The Dockerfile is located in a designated branch [`dockerfile`](https://github.com/SAP/devops-cm-client/tree/docker). After checking out the branch, you can run:
`docker build -t cm-client .`

## Using the Java Application from maven.org

  - Download the command line interface package from [maven.org](http://repo1.maven.org/maven2/com/sap/devops/cmclient/dist.cli).
  - Extract the command line interface package into a suitable folder.

    Example:
    ```
       CM_VERSION=2.0.1
       export CMCLIENT_HOME=`pwd`/cm_client
       export PATH=${CMCLIENT_HOME}/bin:${PATH}
       mkdir -p ${CMCLIENT_HOME}
       curl "https://repo1.maven.org/maven2/com/sap/devops/cmclient/dist.cli/${CM_VERSION}/dist.cli-${CM_VERSION}.tar.gz"  \
       |tar -C ${CMCLIENT_HOME} -xvf -
       cmclient --version
       cmclient --help
    ```
It is recommanded to define `CMCLIENT_HOME` and `PATH` in `~/.bash_profile` or in any other suitable way.


# Usage of the CLI

````
<CMD> [COMMON_OPTIONS...] <subcommand> [SUBCOMMAND_OPTIONS] <parameters...>
````

To pass additional Java options to the command (for example, another truststore), set the environment variable `CMCLIENT_OPTS`

| Option                   |     Description         |
|--------------------------|-------------------------|
| `-e`, `--endpoint <arg>` | Service endpoint        |
| `-h`, `--help`           | Prints this help.       |
| `-p`, `--password <arg>` | Service password, if '-' is provided, password will be read from stdin. |
| `-u`, `--user <arg>`     | Service user.           |
| `-v`, `--version`        | Prints the version.     |


| Subcommand                        | Description                                     |
|-----------------------------------|-------------------------------------------------|
| `create-transport`                | Creates a new transport entity.                 |
| `get-transport-description`       | Returns the description of the transport.       |
| `get-transport-owner`             | Returns the owner of the transport.             |
| `get-transports`                  | Returns the IDs of the transports.              |
| `is-change-in-development`        | Returns 'true' if the change is in development. |
| `is-transport-modifiable`         | Returns 'true' if the transport is modifiable.  |
| `release-transport`               | Releases the transport.                         |
| `upload-file-to-transport`        | Uploads a file to a transport.                  |
| `get-transport-development-system`| Returns the target system of the transport.     |

For more information about subcommands and subcommand options run `<CMD> <subcommand> --help`.

# How to obtain support

Feel free to open new issues for feature requests, bugs or general feedback on
the [GitHub issues page of this project][cm-cli-issues].

# Contributing

Read and understand our [contribution guidelines][contribution]
before opening a pull request.


[cm-cli-issues]: https://github.com/SAP/devops-cm-client/issues
[license]: ./LICENSE
[contribution]: ./CONTRIBUTING.md

# Release Notes
The release notes are available [here](RELEASES.md).
