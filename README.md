# Record Manager

Record Manager is an application for generic form-based collection of data. This repository contains the backend of the application.

## Required Technologies

- JDK 17
- Apache Maven 3.5.x or later

## System Architecture

The system can be split into two parts. __Main application__ provides management of users, organizations and their records. The system manages multiple types of records which are generated dynamically by __Form service__.  

## Configuration

### Further Record Manager Configuration

The table lists environment variables that can be passed to the Record Manager backend through `docker-compose.yml`, an [env_file](https://docs.docker.com/compose/compose-file/compose-file-v3/#env_file), 
or the command line. For more details, see the [Configuration documentation](./doc/configuration.md).

| Variable | Description |
| --- | --- |
| ```APPCONTEXT``` | Public URL of the frontend of record-manager application that is used for password reset emails. e.g. https://study.example.com/record-manager/ (must have "/" at the end) |
| ```DRIVER``` | Driver to manage triple stores |
| ```EMAIL_BCC``` | Email addresses to be blind carbon-copied, separated by a comma (optional, can be empty). |
| ```EMAIL_CC``` | Email addresses to be carbon-copied, separated by a comma (optional, can be empty). |
| ```EMAIL_DISPLAYNAME``` | Email display name |
| ```EMAIL_FROM``` | if email.from is not entered, smtp.user is used instead |
| ```EMAIL_INVITATIONCONTENT``` | UserInvite email html content, variables: username, link, name, appContext |
| ```EMAIL_INVITATIONSUBJECT``` | serInvite email subject |
| ```EMAIL_PASSWORDCHANGECONTENT``` | PasswordReset email html content, variables: username, appContext |
| ```EMAIL_PASSWORDCHANGESUBJECT``` | Password change email |
| ```EMAIL_PASSWORDRESETCONTENT``` | PasswordReset email html content, variables: username, link, appContext |
| ```EMAIL_PASSWORDRESETSUBJECT``` | You can use variables in email contents by using {{variable}}, available variables are listed before email content property<br>Password Reset email subject |
| ```EMAIL_PROFILEUPDATECONTENT``` | PasswordReset email html content, variables: username, appContext |
| ```EMAIL_PROFILEUPDATESUBJECT``` | Profile update email |
| ```EMAIL_REPLYTO``` | Email cc addresses where all invitations will be sent. For more use delimiter "," (can remain empty) |
| ```FORMGENREPOSITORYURL``` | URL of repository where output and configuration of form-generator should be held |
| ```FORMGENSERVICEURL``` | REST endpoint of form generator service |
| ```RECORDS_ALLOWEDREJECTREASON``` | it indicates functionality allowing users to specify a reason for rejection is enabled. |
| ```REPOSITORYURL``` | URL of repository that holds main data of the application |
| ```SECURITY_CORD_ALLOWEDORIGINS``` | Configures allowed origins for CORS (e.g. http://localhost:3000). Use a comma to separate multiple values |
| ```SECURITY_OIDC_ROLECLAIM``` | Claim containing user roles in the OIDC access token (applies only when 'oidc' security provider is selected). Use<br>dot notation for nested objects |
| ```SECURITY_PROVIDER``` | Provider of application security. Possible values are 'internal' for internally stored users and 'oidc' for using an<br>OIDC-compatible authentication service. Its URL is configured via Spring Boot configuration parameters |
| ```SECURITY_SAMESITE``` | Option to pass sameSite attribute for set-cookie headers. Possible values are None,Lax,Strict. In case of None value also attribute "Secure;" is added. |
| ```SMTP_HOST``` | SMTP host |
| ```SMTP_PASSWORD``` | SMTP password |
| ```SMTP_PORT``` | SMTP port |
| ```SMTP_USER``` | SMTP user |

## Documentation

Build configuration and deployment are described in [Setup Guide](doc/setup.md).
Development is described in [Development notes](doc/development.md).
