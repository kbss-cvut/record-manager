# OFN Record Manager Setup Guide

## Build

### System Requirements

- JDK 8 (newer or older versions are not supported at the moment)
- Apache Maven 3.5.x or newer

### Setup

#### Maven Profiles

To build the application for **non**-development deployment, use Maven and select the `production` profile.

### Application Configuration

Application has backend developed in Java and frontend developed in ReactJS frameworks. 

__Backend__ uses src/main/resources/config.properties to configure:
* connection to internal triple store
* rest endpoint of Form service
* smtp configuration for sending emails
* email templates for invitation, password change, and profile update scenarios

See comments in the configuration file for more information. In addition, supported record types are configured using query in src/main/resources/query/findFormTypes.rq.
 
__Frontend__ uses src/main/resources/webapp/.env to configure:
* url of backend
* application title in browser
* internationalization settings

See src/main/resources/webapp/.env.example for detailed description of options. 

### Building

Production war file can be produced by maven command: `mvn clean package -B -P production`

## Deployment

Deployment requires 4 steps:
1) deploy Record manager RDF4J repository
2) deploy Form service RDF4J repository
2) deploy SGoV models repository
3) deploy Form service
4) deploy Record Manager application

### System Requirements

- JDK 8 (newer or older versions are not supported at the moment)
- Apache Tomcat 8.5 or later (9.x is recommended) or any Servlet API 4-compatible application server

 ### Record Manager RDF4J Repository
 
 Main repository of the application is configured by `repositoryUrl` parameter. 
 
 ### Form service RDF4J Repository
 
 Repository dedicated to provide data to Form service is configured by `formGenRepositoryUrl`. Additionally, this repository can contain a configuration of generation of forms fom SGoV model.
 
 ### SGoV Model Repository
  
 This repository is query parameter of Form service call specified in `sgovRepositoryUrl`.

 ### SForms Service
 
 SForms service is configured in `formGenServiceUrl`, the call to the service should contain SGoV model repository as query parameter. Example call:
 `formGenRepositoryUrl=`http://localhost:8080/s-pipes/service?_pId=transform&sgovRepositoryUrl=https%3A%2F%2Fgraphdb.onto.fel.cvut.cz%2Frepositories%2Fkodi-slovnik-gov-cz`



