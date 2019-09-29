package cz.cvut.kbss.study.service.formgen;

import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.formgen.FormGenDao;
import cz.cvut.kbss.study.rest.dto.RawJson;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.service.data.DataLoader;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.ConfigParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Service
public class FormGenService {

    private static final Logger LOG = LoggerFactory.getLogger(FormGenService.class);

    // FormGen repository URL
    private static final String REPOSITORY_URL_PARAM = "repositoryUrl";
    private static final String FORMGEN_REPOSITORY_URL_PARAM = "formGenRepositoryUrl";
    private static final String RECORD_GRAPH_ID_PARAM = "recordGraphId";


    @Autowired
    private FormGenDao formGenDao;

    @Autowired
    private DataLoader dataLoader;

    @Autowired
    private Environment environment;

    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Gets a form from a remote generator service.
     *
     * @param record Record for which the form should be generated
     * @return Form template in JSON-LD
     */
    public RawJson generateForm(PatientRecord record) {
        Objects.requireNonNull(record);
        final User author = securityUtils.getCurrentUser();
        if (author.getInstitution() != null) {
            author.getInstitution().setMembers(null);
        }
        record.setAuthor(author);
        record.setDateCreated(new Date());
        record.setInstitution(author.getInstitution());
        record.setLastModifiedBy(null);
        final URI context = formGenDao.persist(record);
        return loadFormStructure(context);
    }

    private RawJson loadFormStructure(URI context) {
        final String serviceUrl = environment.getProperty(ConfigParam.FORM_GEN_SERVICE_URL.toString(), "");
        final String repoUrl = environment.getProperty(ConfigParam.REPOSITORY_URL.toString(), "");
        final String formGenRepoUrl = environment.getProperty(ConfigParam.FORM_GEN_REPOSITORY_URL.toString(), "");
        if (serviceUrl.isEmpty() || repoUrl.isEmpty()) {
            LOG.error("Form generator service URL or repository URL is missing. Service URL: {}, repository URL: {}.",
                    serviceUrl, repoUrl);
            return new RawJson("");
        }
        final Map<String, String> params = new HashMap<>();
        params.put(RECORD_GRAPH_ID_PARAM, RestUtils.encodeUrl(context.toString()));
        params.put(REPOSITORY_URL_PARAM, RestUtils.encodeUrl(repoUrl));
        params.put(FORMGEN_REPOSITORY_URL_PARAM, RestUtils.encodeUrl(formGenRepoUrl));
        return new RawJson(dataLoader.loadData(serviceUrl, params));
    }

    public RawJson getPossibleValues(String query) {
        Objects.requireNonNull(query);
        return new RawJson(dataLoader.loadData(query, Collections.emptyMap()));
    }
}
