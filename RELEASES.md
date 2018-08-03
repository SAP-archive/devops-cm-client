# Release Notes
* 2.0.1 Allow also empty DevelopmentSystemIds
  * Bug fixes:
    * The client is now able to deal with empty development system ids.
* 2.0.0 Support for DevelopmentSystemID property
  * Incompatible changes:
    * Creating a transport requires a new property 'developmentSystemID' (`-dID`)
* 1.0.0 Additing commands for interacting with CTS.
  * Incompatible changes: 
    * changeId and transportId needs to be provided as option `-cID`, `-tID` rather than as argument.
    * new option `-t`, `--backend-type` needs to provided for each call in order to distinguish between SOLMAN and CTS use cases.
  * Bug fixes:
    * bug fix in launcher script `bin/cmclient`. Calling the command line client failed in case of having a symbolic link e.g.
      in `/usr/local/bin`. `CMCLIENT_HOME` was not taken into account for setting up the class path in this case.
* 0.0.1 Initial release, providing commands for interacting with SAP SolutionManager.
