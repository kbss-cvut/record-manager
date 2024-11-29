| Variable | Description |
| --- | --- |
| ```APPCONTEXT``` | Public URL of the frontend of record-manager application that is used for password reset emails. e.g. https://study.example.com/record-manager/ (must have "/" at the end) |
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
| ```PERSISTENCEDRIVER``` | Persistence driver to manage triple stores |
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

