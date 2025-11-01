package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.rest.dto.RawJson;
import cz.cvut.kbss.study.service.formgen.FormGenService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/formGen")
public class FormGenController extends BaseController {

    private final FormGenService formGenService;

    public FormGenController(FormGenService formGenService) {
        this.formGenService = formGenService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public RawJson generateForm(@RequestBody Record data) {
        return formGenService.generateForm(data);
    }

    @RequestMapping("/possibleValues")
    public RawJson getPossibleValues(@RequestParam("query") String query) {
        return formGenService.getPossibleValues(query);
    }

    @RequestMapping("/formTemplates")
    public RawJson getFormTemplates() {
        return formGenService.getFormTemplates();
    }
}
