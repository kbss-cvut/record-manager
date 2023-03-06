package cz.cvut.kbss.study.util.etemplates;

import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;

import java.util.HashMap;
import java.util.Map;

public class UserInvite extends BaseEmailTemplate {
    public UserInvite(ConfigReader config, User user) {
        this.config = config;
        this.username = user.getUsername();
        this.token = user.getToken();
        this.name = user.getFirstName() + " " + user.getLastName();
    }

    private ConfigReader config;
    private String username;
    private String token;
    private String name;

    @Override
    public String getSubject() {
        return config.getConfig(ConfigParam.E_INVITATION_SUBJECT);}

    @Override
    public String getHTMLContent() {
        String link = config.getConfig(ConfigParam.APP_CONTEXT) + "login/password-reset/" + this.token;
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("link", link);
        params.put("name", name);
        params.put("appContext", config.getConfig(ConfigParam.APP_CONTEXT));
        return config.getConfigWithParams(ConfigParam.E_INVITATION_CONTENT, params);
    }
}
