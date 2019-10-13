package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
@RestController
@RequestMapping("/statistics")
public class StatisticsController extends BaseController {

    @Autowired
    private StatisticsService statisticsService;

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> data = new HashMap<>();
        data.put("number-of-investigators", getNumberOfInvestigators());
        data.put("number-of-processed-records", getNumberOfProcessedRecords());
        return data;
    }

    private int getNumberOfInvestigators() {
        return statisticsService.getNumberOfInvestigators();
    }

    private int getNumberOfProcessedRecords() {
        return statisticsService.getNumberOfProcessedRecords();
    }
}
