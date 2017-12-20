# Description

Simple command line interface to handle basic change management related actions
in Solution Manager via ODATA requests. The client is intended to be used in 
continuous integration and continuous deployment scenarios and supports only the
action necessary within those scenarios. Available actions are:

 - Create transport request
 - Get transport request description
 - Get transport request owner
 - Get transport requests 
 - Check if change is in development
 - Check if transport request is modifiable
 - Release transport request
 - Upload File to transport request

# Requirements

 - Solution Manager 7.2 SP6
 - Java Development Kit 8

# Download and Installation

  - Clone this project from github.com
  - Build the project with maven: `mvn clean package`
  - Create a temporary directory: `mkdir tmp`
  - Extract the commnand line interface into that folder:
    `tar -C tmp -xvf modules/dist.cli/target/dist.cli-${project.version}.tar.gz`
  - Run `tmp/bin/cmclient --help` in order to see all available commands
 
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
