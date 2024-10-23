# OFN Record Manager Setup Guide

## Build

### System Requirements

- JDK 17 or later
- Apache Maven 3.5.x or newer

### Application Configuration

The application uses `src/main/resources/application.properties` to configure:
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
set the `security.provider` (in `application.properties` or via `SECURITY_PROVIDER` via an environment variable) configuration to `oidc` 
and configure the `spring.security.oauth2.resourceserver.jwt.issuer-uri` (in `application.properties` or using an environment variable)
parameter to the URI of the OAuth2 token issuer. When using Keycloak, this corresponds to the URI of the realm through
which Record Manager users authenticate their requests. For example, the value may be `http://localhost:8080/realms/record-manager`.
A client with confidential access and the corresponding valid redirect and origin URIs should be configured in the realm.

If needed, claim used to access user's roles can be configured via `oidc.roleClaim`. The default value corresponds to the
default role mapping in Keycloak. Record Manager will assign `ROLE_USER` to authenticated users by default, any other roles
must be available in the token.

Note also that it is expected that user metadata corresponding to the user extracted from the access token exist in the
repository. They are paired via the `preferred_username` claim value (see `SecurityUtils`).

## Docker Compose Deployment

This repo contains an example Docker Compose configuration that can be used to quickly spin up Record Manager with its frontend,
a GraphDB repository, S-pipes form generator and Keycloak as the authentication service. The configuration uses the Record Manager
code from this repository. Published frontend image is used.

The deployment is pretty much self-contained, it sets up the corresponding repositories, imports a realm where clients
are configured for both the Record Manager backend and frontend. All the services (except PostgreSQL used by Keycloak) 
in the deployment export their ports to the host system, so ensure the following ports are available on your system: 
3000, 8080, 8081, 8088.

To run the deployment for the first time, follow these steps:

1. Create the `.env` file and set the following variables in it: `KC_ADMIN_USER`, `KC_ADMIN_PASSWORD`.
2. Run `docker compose up -d db-server` first. It uses a script that creates GraphDB repositories needed by the system.
3. Wait approximately 20s (check the log and wait for GraphDB to be fully up).
4. Start the rest of the system by running `docker compose up -d --build` (`--build` is used because Record Manager backend needs to be build)
5. Go to [http://localhost:8088](http://localhost:8088), login to the Keycloak admin console using `KC_ADMIN_USER` and `KC_ADMIN_PASSWORD`.
6. Select realm `record-manager`.
7. Add user accounts as necessary. Do not forget to assign them one of `ROLE_ADMIN` or `ROLE_USER` roles.
8. Go to [http://localhost:3000](http://localhost:3000) and log in using one of the created user accounts.

When running the deployment next time, just execute `docker compose up -d --build` and go to [http://localhost:3000](http://localhost:3000).
