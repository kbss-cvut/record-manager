package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.export.ExportRecord;
import cz.cvut.kbss.study.model.export.Path;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.CodeListValuesDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.util.Utils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExcelRecordExporter {

    private final PatientRecordDao patientRecordDao;
    private final CodeListValuesDao codeListValuesDao;

    public ExcelRecordExporter(PatientRecordDao patientRecordDao, CodeListValuesDao codeListValuesDao) {
        this.patientRecordDao = patientRecordDao;
        this.codeListValuesDao = codeListValuesDao;
    }

    public InputStream exportRecords(RecordFilterParams filters){
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(Utils.class.getClassLoader().getResourceAsStream("templates/record-export-template.xlsx"));
            List<ExportRecord> exportRecords = findExportRecords(filters);
            addDataToExcel(workbook, exportRecords);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ExportRecord> findExportRecords(RecordFilterParams filters){
        List<RawRecord> rawRecords = patientRecordDao.findAllRecordsRaw(filters);
        Map<URI, String> translatorMap = new HashMap<>();
        List<Path> paths = codeListValuesDao.getBroaderPath(rawRecords.stream().map(r -> r.getAc_comp()).collect(Collectors.toSet()));
        Set<URI> uris = rawRecords.stream().flatMap(r -> Stream.of(
                        r.getClassificationOfOccurrence(), r.getConsequence(), r.getFailureAscertainmentCircumstances(),
                        r.getFailureCause(), r.getAc_comp(), r.getFhaEvent(), r.getMission(), r.getRepair(),
                        r.getRepeatedFailure()))
                .distinct()
                .filter(u -> u != null)
                .collect(Collectors.toSet());

        uris.addAll(paths.stream()
                .flatMap(p -> Stream.of(p.getL1(), p.getL2(), p.getL3(), p.getL4(), p.getL5()))
                .filter(u -> u != null).collect(Collectors.toSet())
        );


        codeListValuesDao.findItems(uris).forEach(i -> translatorMap.put(i.getUri(), i.getName()));
        codeListValuesDao.findAircraft().forEach(i -> translatorMap.put(i.getUri(), i.getName()));
        codeListValuesDao.findInstitutions().forEach(i -> translatorMap.put(i.getUri(), i.getName()));

        Map<URI, List<String>> pathMap = new HashMap<>();
        paths.forEach(p -> pathMap.put(p.getUri(), Arrays.asList(
                translatorMap.get(p.getL1()),
                translatorMap.get(p.getL2()),
                translatorMap.get(p.getL3()),
                translatorMap.get(p.getL4()),
                translatorMap.get(p.getL5())
        )));

        List<ExportRecord> exportRecords = new ArrayList<>();

        for(RawRecord r : rawRecords){
            ExportRecord er = new ExportRecord();
            exportRecords.add(er);
            er.setPath(pathMap.get(r.getAc_comp()));
            er.setInstitution(translatorMap.get(r.getInstitution()));
            er.setAircraftType(translatorMap.get(r.getAircraftType()));
            er.setAc_compName(translatorMap.get(r.getAc_comp()));
            er.setClassificationOfOccurrence(translatorMap.get(r.getClassificationOfOccurrence()));
            er.setConsequence(translatorMap.get(r.getConsequence()));
            er.setFailureCause(translatorMap.get(r.getFailureCause()));
            er.setFhaEvent(translatorMap.get(r.getFhaEvent()));
            er.setMission(translatorMap.get(r.getMission()));
            er.setRepair(translatorMap.get(r.getRepair()));
            er.setRepeatedFailure(translatorMap.get(r.getRepeatedFailure()));

            er.setFuselage(r.getFuselage());
            er.setUri(r.getUri());
            er.setCreated(r.getCreated());
            er.setLabel(r.getLabel());
            er.setFailDate(r.getFailDate());
            er.setFlightHours(r.getFlightHours());
            er.setNumberOfAirframeOverhauls(r.getNumberOfAirframeOverhauls());
            er.setRepairDuration(r.getRepairDuration());
            er.setAverageNumberOfMenDuringRepairment(r.getAverageNumberOfMenDuringRepairment());
            er.setFailureDescription(r.getFailureDescription());
            er.setDescriptionOfCorrectiveAction(r.getDescriptionOfCorrectiveAction());
            er.setNumberOfOverhaulsOfDefectiveEquipment(r.getNumberOfOverhaulsOfDefectiveEquipment());
            er.setSerialNoOf(r.getSerialNoOf());
            er.setNotes(r.getNotes());
        }

        return exportRecords;
    }


    private void addDataToExcel(XSSFWorkbook workbook, List<ExportRecord> data) throws IOException {
        XSSFSheet s = workbook.getSheetAt(1);

        int rowIndex = 1;
        for(ExportRecord rec : data) {
            XSSFRow r = s.createRow(rowIndex++);
            r.createCell(0).setCellValue(rowIndex);
//            r.createCell(1).setCellValue(rowIndex);
            r.createCell(2).setCellValue(rec.getCreated());
//            r.createCell(3).setCellValue(rec.ed);
//            r.createCell(4).setCellValue(rec.);
//            r.createCell(5).setCellValue(rec.);
            r.createCell(6).setCellValue(rec.getLabel());
            r.createCell(7).setCellValue(rec.getInstitution());
            r.createCell(8).setCellValue(rec.getAircraftType());
            r.createCell(10).setCellValue(rec.getFuselage());
            r.createCell(11).setCellValue(rec.getFailDate());
            r.createCell(12).setCellValue(rec.getFlightHours());
            r.createCell(13).setCellValue(rec.getNumberOfAirframeOverhauls());
            r.createCell(14).setCellValue(rec.getClassificationOfOccurrence());
            r.createCell(15).setCellValue(rec.getFailureAscertainmentCircumstances());
            r.createCell(16).setCellValue(rec.getRepeatedFailure());
            r.createCell(17).setCellValue(rec.getFailureCause());
            r.createCell(18).setCellValue(rec.getConsequence());
            r.createCell(19).setCellValue(rec.getMission());
            r.createCell(20).setCellValue(rec.getRepair());
            r.createCell(21).setCellValue(rec.getRepairDuration());
            r.createCell(22).setCellValue(rec.getAverageNumberOfMenDuringRepairment());
            r.createCell(23).setCellValue(rec.getFailureDescription());
            r.createCell(24).setCellValue(rec.getDescriptionOfCorrectiveAction());
            r.createCell(25).setCellValue(rec.getAc_compName());
            if (rec.getPath() != null){
                r.createCell(26).setCellValue(rec.getPath().get(0));
                r.createCell(27).setCellValue(rec.getPath().get(1));
                r.createCell(28).setCellValue(rec.getPath().get(2));
                r.createCell(29).setCellValue(rec.getPath().get(3));
                r.createCell(30).setCellValue(rec.getPath().get(4));
            }
//            r.createCell(31).setCellValue(rec.get());
//            r.createCell(32).setCellValue(rec.get());
            r.createCell(31).setCellValue(rec.getYearOfProductionOfDefectiveEquipment());

            Optional.ofNullable(rec.getNumberOfOverhaulsOfDefectiveEquipment()).ifPresent(v -> r.createCell(29).setCellValue(v));
            r.createCell(33).setCellValue(rec.getSerialNoOf());
            r.createCell(34).setCellValue(rec.getNotes());
            r.createCell(35).setCellValue(rec.getFhaEvent());
        }
    }

}