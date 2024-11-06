# Record Manager

Record Manager is an application for generic form-based collection of data. This repository contains the backend of the application.

## Required Technologies

- JDK 17
- Apache Maven 3.5.x or later

## System Architecture

The system can be split into two parts. __Main application__ provides management of users, organizations and their records. The system manages multiple types of records which are generated dynamically by __Form service__.  

## Configuration

### Further Record Manager Configuration

The following table lists the names of environment variables that can be
passed to Record Manager backend either directly in `docker-compose.yml`, in
an [env_file](https://docs.docker.com/compose/compose-file/compose-file-v3/#env_file), or via command line.

| Variable                                           | Explanation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|:---------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ```RECORDS_ALLOWEDREJECTREASON```\               | Allow user to enter reason when rejecting records. Default value: `false.`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |

## Documentation

Build configuration and deployment are described in [Setup Guide](doc/setup.md).
Development is described in [Development notes](doc/development.md).
