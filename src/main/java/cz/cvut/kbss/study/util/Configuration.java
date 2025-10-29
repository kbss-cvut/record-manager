package cz.cvut.kbss.study.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "")
public class Configuration {

    /**
     * Public URL of the frontend of record-manager application that is used for password reset emails. e.g. https://study.example.com/record-manager/ (must have "/" at the end)
     */
    String appContext;

    /**
     * Persistence driver to manage triple stores
     */
    String persistenceDriver;

    /**
     * URL of repository that holds main data of the application
     */
    String repositoryUrl;

    /**
     * URL of repository where output and configuration of form-generator should be held
     */
    String formGenRepositoryUrl;

    /**
     *  REST endpoint of form generator service
     */
    String formGenServiceUrl;

    Smtp smtp = new Smtp();
    Email email = new Email();
    Security security = new Security();
    Records records = new Records();

    public String getAppContext() {
        return appContext;
    }

    public void setAppContext(String appContext) {
        this.appContext = appContext;
    }

    public String getPersistenceDriver() {
        return persistenceDriver;
    }

    public void setPersistenceDriver(String persistenceDriver) {
        this.persistenceDriver = persistenceDriver;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getFormGenRepositoryUrl() {
        return formGenRepositoryUrl;
    }

    public void setFormGenRepositoryUrl(String formGenRepositoryUrl) {
        this.formGenRepositoryUrl = formGenRepositoryUrl;
    }

    public String getFormGenServiceUrl() {
        return formGenServiceUrl;
    }

    public void setFormGenServiceUrl(String formGenServiceUrl) {
        this.formGenServiceUrl = formGenServiceUrl;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public void setSmtp(Smtp smtp) {
        this.smtp = smtp;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Records getRecords() {
        return records;
    }

    public void setRecords(Records records) {
        this.records = records;
    }


    public static class Smtp {
        /**
         * SMTP host
         */
        String host;

        /**
         * SMTP port
         */
        String port;

        /**
         * SMTP user
         */
        String user;

        /**
         * SMTP password
         */
        String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

    }

    public static class Email {
        /**
         * Email display name
         */
        String displayName;

        /**
         * if email.from is not entered, smtp.user is used instead
         */
        String from;

        /**
         * Email cc addresses where all invitations will be sent. For more use delimiter "," (can remain empty)
         */
        String replyTo;

        /**
         * Email addresses to be carbon-copied, separated by a comma (optional, can be empty).
         */
        String cc;

        /**
         * Email addresses to be blind carbon-copied, separated by a comma (optional, can be empty).
         */
        String bcc;

        /**
         * You can use variables in email contents by using {{variable}}, available variables are listed before email content property
         * Password Reset email subject
         */
        String passwordResetSubject;

        /**
         * PasswordReset email html content, variables: username, link, appContext
         */
        String passwordResetContent;

        /**
         * serInvite email subject
         */
        String invitationSubject;

        /**
         * UserInvite email html content, variables: username, link, name, appContext
         */
        String invitationContent;

        /**
         * Password change email
         */
        String passwordChangeSubject;

        /**
         * PasswordReset email html content, variables: username, appContext
         */
        String passwordChangeContent;

        /**
         * Profile update email
         */
        String profileUpdateSubject;

        /**
         * PasswordReset email html content, variables: username, appContext
         */
        String profileUpdateContent;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }

        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        public String getBcc() {
            return bcc;
        }

        public void setBcc(String bcc) {
            this.bcc = bcc;
        }

        public String getPasswordResetSubject() {
            return passwordResetSubject;
        }

        public void setPasswordResetSubject(String passwordResetSubject) {
            this.passwordResetSubject = passwordResetSubject;
        }

        public String getPasswordResetContent() {
            return passwordResetContent;
        }

        public void setPasswordResetContent(String passwordResetContent) {
            this.passwordResetContent = passwordResetContent;
        }

        public String getInvitationSubject() {
            return invitationSubject;
        }

        public void setInvitationSubject(String invitationSubject) {
            this.invitationSubject = invitationSubject;
        }

        public String getInvitationContent() {
            return invitationContent;
        }

        public void setInvitationContent(String invitationContent) {
            this.invitationContent = invitationContent;
        }

        public String getPasswordChangeSubject() {
            return passwordChangeSubject;
        }

        public void setPasswordChangeSubject(String passwordChangeSubject) {
            this.passwordChangeSubject = passwordChangeSubject;
        }

        public String getPasswordChangeContent() {
            return passwordChangeContent;
        }

        public void setPasswordChangeContent(String passwordChangeContent) {
            this.passwordChangeContent = passwordChangeContent;
        }

        public String getProfileUpdateSubject() {
            return profileUpdateSubject;
        }

        public void setProfileUpdateSubject(String profileUpdateSubject) {
            this.profileUpdateSubject = profileUpdateSubject;
        }

        public String getProfileUpdateContent() {
            return profileUpdateContent;
        }

        public void setProfileUpdateContent(String profileUpdateContent) {
            this.profileUpdateContent = profileUpdateContent;
        }
    }

    public static class Security {

        /**
         *  Provider of application security. Possible values are 'internal' for internally stored users and 'oidc' for using an
         *  OIDC-compatible authentication service. Its URL is configured via Spring Boot configuration parameters
         */
        String provider;

        /**
         * Option to pass sameSite attribute for set-cookie headers. Possible values are None,Lax,Strict. In case of None value also attribute "Secure;" is added.
         */
        String sameSite;
        private Oidc oidc = new Oidc();
        private Cors cord = new Cors();

        public String getSameSite() {
            return sameSite;
        }


        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Oidc getOidc() {
            return oidc;
        }

        public void setOidc(Oidc oidc) {
            this.oidc = oidc;
        }

        public Cors getCord() {
            return cord;
        }

        public void setCord(Cors cord) {
            this.cord = cord;
        }

        public static class Oidc {
            /**
             * Claim containing user roles in the OIDC access token (applies only when 'oidc' security provider is selected). Use
             * dot notation for nested objects
             */
            String roleClaim;

            public String getRoleClaim() {
                return roleClaim;
            }

            public void setRoleClaim(String roleClaim) {
                this.roleClaim = roleClaim;
            }
        }

        public static class Cors {
            /**
             * Configures allowed origins for CORS (e.g. http://localhost:3000). Use a comma to separate multiple values
             */
            String allowedOrigins;

            public String getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(String allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }
        }
    }

    public static class Records {

        /**
         *  it indicates functionality allowing users to specify a reason for rejection is enabled.
         */
        boolean allowedRejectReason;

        /**
         *  it indicates functionality allowing users to create records without being assigned to any institution.
         */
        boolean allowedCreationWithoutInstitution;

        public boolean isAllowedRejectReason() {
            return allowedRejectReason;
        }

        public boolean isAllowedCreationWithoutInstitution() {
            return allowedCreationWithoutInstitution;
        }

        public void setAllowedRejectReason(boolean allowedRejectReason) {
            this.allowedRejectReason = allowedRejectReason;
        }

        public void setAllowedCreationWithoutInstitution(boolean allowedCreationWithoutInstitution) {
            this.allowedCreationWithoutInstitution = allowedCreationWithoutInstitution;
        }
    }
}
