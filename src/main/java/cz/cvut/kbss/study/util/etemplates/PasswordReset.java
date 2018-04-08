package cz.cvut.kbss.study.util.etemplates;

import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class PasswordReset extends BaseEmailTemplate{
    public PasswordReset(ConfigReader config, String username, String newPassword) {
        this.config = config;
        this.username = username;
        this.newPassword = newPassword;
    }

    private ConfigReader config;
    private String username;
    private String newPassword;


    @Override
    public String getSubject() {
        return config.getConfig(ConfigParam.E_PASSWORD_RESET_SUBJECT);
    }

    @Override
    public String getHTMLContent() {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", newPassword);
        return config.getConfigWithParams(ConfigParam.E_PASSWORD_RESET_CONTENT, params);
    }
}
