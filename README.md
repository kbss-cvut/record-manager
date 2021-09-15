# OFN Record Manager

Manager of records based on OFN (https://opendata.gov.cz/otevřené-formální-normy:start).

## Required Technologies

- JDK 8 (preferably Oracle)
- Apache Maven 3.5.x
- Apache Tomcat 9 or newer (required by Servlet API 4)

## System Architecture

The system can be split into two parts. __Main application__ provides management of users, organizations and their records. The system manages multiple types of records which are generated dynamically by __Form service__ defined in https://github.com/opendata-mvcr/sgov-forms .  

## Documentation

Build configuration and deployment is described in [Setup Guide](doc/setup.md).
Development is described in [Development notes](doc/development.md).

-----

Tento repozitář je udržován v rámci projektu OPZ č. CZ.03.4.74/0.0/0.0/15_025/0004172.
![Evropská unie - Evropský sociální fond - Operační program Zaměstnanost](https://data.gov.cz/images/ozp_logo_cz.jpg)
