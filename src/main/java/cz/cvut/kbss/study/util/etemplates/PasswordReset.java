package cz.cvut.kbss.study.util.etemplates;

import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;

import java.util.HashMap;
import java.util.Map;

public class PasswordReset extends BaseEmailTemplate{
    public PasswordReset(ConfigReader config, User user) {
        this.config = config;
        this.username = user.getUsername();
        this.token = user.getToken();
    }

    private ConfigReader config;
    private String username;
    private String token;


    @Override
    public String getSubject() {
        return config.getConfig(ConfigParam.E_PASSWORD_RESET_SUBJECT);
    }

    @Override
    public String getHTMLContent() {
        String link = config.getConfig(ConfigParam.APP_CONTEXT) + "login/password-reset/" + this.token;
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("link", link);
        params.put("appContext", config.getConfig(ConfigParam.APP_CONTEXT));
        return config.getConfigWithParams(ConfigParam.E_PASSWORD_RESET_CONTENT, params);
    }
}
