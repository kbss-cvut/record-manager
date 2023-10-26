# OFN Record Manager Setup Guide

## Build

### System Requirements

- JDK 17 or later
- Apache Maven 3.5.x or newer

### Application Configuration

The application uses `src/main/resources/config.properties` to configure:
* connection to internal triple store
* REST endpoint of Form service
* SMTP configuration for sending emails
* email templates for invitation, password change, and profile update scenarios

See comments in the configuration file for more information. In addition, supported record types are configured using query in `src/main/resources/query/findFormTypes.rq`.

### Building

Application JAR file can be produced by maven command: `mvn clean package -B`

## Deployment

Deployment requires 4 steps:
1) deploy Record manager RDF4J repository
2) deploy Form service RDF4J repository
2) deploy SGoV models repository
3) deploy Form service
4) deploy Record Manager application

### System Requirements

- JDK 17

### Record Manager RDF4J Repository
 
Main repository of the application is configured by `repositoryUrl` parameter. 
 
### Form service RDF4J Repository
 
Repository dedicated to provide data to Form service is configured by `formGenRepositoryUrl`. Additionally, this repository can contain a configuration of generation of forms fom SGoV model.
 
### SGoV Model Repository
  
This repository is query parameter of Form service call specified in `sgovRepositoryUrl`.

### SForms Service
 
SForms service is configured in `formGenServiceUrl`, the call to the service should contain SGoV model repository as query parameter. Example call:
`formGenRepositoryUrl=`http://localhost:8080/s-pipes/service?_pId=transform&sgovRepositoryUrl=https%3A%2F%2Fgraphdb.onto.fel.cvut.cz%2Frepositories%2Fkodi-slovnik-gov-cz`

### OpenID Connect Authentication

RecordManager can work with an external authentication service implementing the OpenID Connect protocol. To use it,
set the `security.provider` (in `config.properties` or via `SECURITY_PROVIDER` via an environment variable) configuration to `oidc` 
and configure the `spring.security.oauth2.resourceserver.jwt.issuer-uri` (in `application.properties` or using an environment variable)
parameter to the URI of the OAuth2 token issuer. When using Keycloak, this corresponds to the URI of the realm through
which Record Manager users authenticate their requests. For example, the value may be `http://localhost:8080/realms/record-manager`.
A client with confidential access and the corresponding valid redirect and origin URIs should be configured in the realm.

If needed, claim used to access user's roles can be configured via `oidc.roleClaim`. The default value corresponds to the
default role mapping in Keycloak. Record Manager will assign `ROLE_USER` to authenticated users by default, any other roles
must be available in the token.

Note also that it is expected that user metadata corresponding to the user extracted from the access token exist in the
repository. They are paired via the `prefferred_username` claim value (see `SecurityUtils`).
