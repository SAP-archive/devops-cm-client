# Description

Simple command line interface to handle basic change management related actions
in SAP Solution Manager via ODATA requests. The client is intended to be used
in continuous integration and continuous delivery scenarios and supports only
the actions necessary within those scenarios. See section _Usage_ for more details.

# Requirements
 - SAP Solution Manager 7.2 SP6
 - JDK 8 to build this project (to run the client JRE 8 is sufficient)

# Download and Installation

  - Clone this project from github.com
  - Build the project with maven: `mvn clean package`
  - Create a temporary directory: `mkdir tmp`
  - Extract the command line interface into that folder:

    `tar -C tmp -xvf modules/dist.cli/target/dist.cli-${project.version}.tar.gz`
  - Run `tmp/bin/cmclient --help` in order to see all available commands

# Usage
````
<CMD> [COMMON_OPTIONS...] <subcommand> [SUBCOMMAND_OPTIONS] <parameters...>
````

| Option                   |     Description         |
|--------------------------|-------------------------|
| `-e`, `--endpoint <arg>` | Service endpoint        |
| `-h`, `--help`           | Prints this help.       |
| `-p`, `--password <arg>` | Service password, if '-' is provided, password will be read from stdin. |
| `-u`, `--user <arg>`     | Service user.           |
| `-v`, `--version`        | Prints the version.     |


| Subcommand                  |                                           |
|-----------------------------|-------------------------------------------|
| `create-transport`          | Creates a new transport entity.           |
| `get-transport-description` | Returns the description of the transport. |
| `get-transport-owner`       | Returns the owner of the transport.       |
| `get-transports`            | Returns the IDs of the transports.        |
| `is-change-in-development`  | Returns 'true' if the change is in development. |
| `is-transport-modifiable`   | Returns 'true' if the transport is modifiable. |
| `release-transport`         | Releases the transport.                   |
| `upload-file-to-transport`  | Uploads a file to a transport.            |

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
