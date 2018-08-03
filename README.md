# Description

Simple command line interface to handle basic change management related actions
in SAP Solution Manager or with CTS via ODATA requests. The client is intended to be used
in continuous integration and continuous delivery scenarios and supports only
the actions necessary within those scenarios. See section _Usage_ for more details.

# Requirements
 - SAP Solution Manager 7.2 SP6
 - JDK 8 to build this project (to run the client JRE 8 is sufficient)

# Download and Installation

  - Download the command line interface package from [mavencentral](http://repo1.maven.org/maven2/com/sap/devops/cmclient/dist.cli) 
  - Extract the command line interface package into suitable folder

  E.g.:
  ```
     CM_VERSION=0.0.1
     mkdir cm_client
     curl "http://repo1.maven.org/maven2/com/sap/devops/cmclient/dist.cli/${CM_VERSION}/dist.cli-${CM_VERSION}.tar.gz"  \
     |tar -C cm_client -xvf -
     cm_client/bin/cmclient --version
     cm_client/bin/cmclient --help
  ```

# Usage
````
<CMD> [COMMON_OPTIONS...] <subcommand> [SUBCOMMAND_OPTIONS] <parameters...>
````

| Option                   |     Description         |
|--------------------------|-------------------------|
| `-t`, `--backend-type`   | `SOLMAN` or `CTS`       |
| `-e`, `--endpoint <arg>` | Service endpoint        |
| `-h`, `--help`           | Prints this help.       |
| `-p`, `--password <arg>` | Service password, if '-' is provided, password will be read from stdin. |
| `-u`, `--user <arg>`     | Service user.           |
| `-v`, `--version`        | Prints the version.     |


| Subcommand                        | Backend Type |    Description                                  |
|-----------------------------------|--------------|-------------------------------------------------|
| `create-transport`                | SOLMAN       | Creates a new transport entity.                 |
| `get-transport-description`       | SOLMAN       | Returns the description of the transport.       |
| `get-transport-owner`             | SOLMAN       | Returns the owner of the transport.             |
| `get-transports`                  | SOLMAN       | Returns the IDs of the transports.              |
| `is-change-in-development`        | SOLMAN       | Returns 'true' if the change is in development. |
| `is-transport-modifiable`         | SOLMAN       | Returns 'true' if the transport is modifiable.  |
| `release-transport`               | SOLMAN       | Releases the transport.                         |
| `upload-file-to-transport`        | SOLMAN       | Uploads a file to a transport.                  |
| `get-transport-development-system | SOLMAN       | Returns the target system of the transport.     |
| `create-transport`                | CTS          | Creates a new transport entity.                 |
| `export-transport`                | CTS          | Exports a transport entity.                     |
| `get-transport-description`       | CTS          | Returns the description of the transport.       |
| `get-transport-owner`             | CTS          | Returns the owner of the transport.             |
| `get-transport-statu    s`        | CTS          | Returns the status of the transport.            |
| `get-transport-t    arget-system` | CTS          | Returns the target system of the transport.     |
| `get-transport-type`              | CTS          | Returns the type of the transport.              |
| `import-transport`                | CTS          | Imports a transport entity.                     |
| `is-transport-modifiable`         | CTS          | Returns 'true' if the transport is modifiable.  |
| `upload-file-to-transport`        | CTS          | Uploads a file to a transport.                  |


For more information about subcommands and subcommand options run `<CMD> <subcommand> --help`.


# How to obtain support

Feel free to open new issues for feature requests, bugs or general feedback on
the [GitHub issues page of this project][cm-cli-issues].

# Contributing

Read and understand our [contribution guidelines][contribution]
before opening a pull request.

# [License][license]

Copyright (c) 2017 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, v. 2 except as noted
otherwise in the [LICENSE file][license]

[cm-cli-issues]: https://github.com/SAP/change-management-cli/issues
[license]: ./LICENSE
[contribution]: ./CONTRIBUTING.md

# Release Notes
The release notes are available [here](RELEASES.md)

