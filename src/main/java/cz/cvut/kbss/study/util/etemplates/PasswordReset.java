package cz.cvut.kbss.study.util.etemplates;

public class PasswordReset extends BaseEmailTemplate{
    public PasswordReset(String username, String newPassword) {
        this.username = username;
        this.newPassword = newPassword;
    }

    private String username;
    private String newPassword;


    @Override
    public String getSubject() {
        return "Password Change";
    }

    @Override
    public String getHTMLContent() {
        String content = "<div>" +
                "<p>Hello "+ this.username + ", </p>" +
                "<p>your new password is: " + this.newPassword + " </p>" +
                "<p>Your, <br>StudyManager</p>" +
                "</div>";
        return content;
    }
}
