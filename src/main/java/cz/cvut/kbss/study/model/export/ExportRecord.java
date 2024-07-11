package cz.cvut.kbss.study.model.export;

import java.net.URI;
import java.util.Date;
import java.util.List;

public class ExportRecord {

    private URI uri;


    private Date created;

    private Date lastModified;

    private String label;

    private String phase;

    private String institution;

    private String aircraftType;

    private String fuselage;

    private URI ac_comp;

    private String ac_compName;


    private List<String> path;

    private String failDate;


    private String flightHours;

    private Integer numberOfAirframeOverhauls;

    private String classificationOfOccurrence;

    private String failureAscertainmentCircumstances;

    private String repeatedFailure;

    private String failureCause;

    private String consequence;

    private String mission;

    private String repair;

    private String repairDuration;

    private Double averageNumberOfMenDuringRepairment;

    private String failureDescription;

    private String descriptionOfCorrectiveAction;

    private String yearOfProductionOfDefectiveEquipment;

    private Integer numberOfOverhaulsOfDefectiveEquipment;

    private String serialNoOf;

    private String notes;

    private String fhaEvent;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getFuselage() {
        return fuselage;
    }

    public void setFuselage(String fuselage) {
        this.fuselage = fuselage;
    }

    public URI getAc_comp() {
        return ac_comp;
    }

    public void setAc_comp(URI ac_comp) {
        this.ac_comp = ac_comp;
    }

    public String getAc_compName() {
        return ac_compName;
    }

    public void setAc_compName(String ac_compName) {
        this.ac_compName = ac_compName;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getFailDate() {
        return failDate;
    }

    public void setFailDate(String failDate) {
        this.failDate = failDate;
    }

    public String getFlightHours() {
        return flightHours;
    }

    public void setFlightHours(String flightHours) {
        this.flightHours = flightHours;
    }

    public Integer getNumberOfAirframeOverhauls() {
        return numberOfAirframeOverhauls;
    }

    public void setNumberOfAirframeOverhauls(Integer numberOfAirframeOverhauls) {
        this.numberOfAirframeOverhauls = numberOfAirframeOverhauls;
    }

    public String getClassificationOfOccurrence() {
        return classificationOfOccurrence;
    }

    public void setClassificationOfOccurrence(String classificationOfOccurrence) {
        this.classificationOfOccurrence = classificationOfOccurrence;
    }

    public String getFailureAscertainmentCircumstances() {
        return failureAscertainmentCircumstances;
    }

    public void setFailureAscertainmentCircumstances(String failureAscertainmentCircumstances) {
        this.failureAscertainmentCircumstances = failureAscertainmentCircumstances;
    }

    public String getRepeatedFailure() {
        return repeatedFailure;
    }

    public void setRepeatedFailure(String repeatedFailure) {
        this.repeatedFailure = repeatedFailure;
    }

    public String getFailureCause() {
        return failureCause;
    }

    public void setFailureCause(String failureCause) {
        this.failureCause = failureCause;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public String getRepair() {
        return repair;
    }

    public void setRepair(String repair) {
        this.repair = repair;
    }

    public String getRepairDuration() {
        return repairDuration;
    }

    public void setRepairDuration(String repairDuration) {
        this.repairDuration = repairDuration;
    }

    public Double getAverageNumberOfMenDuringRepairment() {
        return averageNumberOfMenDuringRepairment;
    }

    public void setAverageNumberOfMenDuringRepairment(Double averageNumberOfMenDuringRepairment) {
        this.averageNumberOfMenDuringRepairment = averageNumberOfMenDuringRepairment;
    }

    public String getFailureDescription() {
        return failureDescription;
    }

    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription;
    }

    public String getDescriptionOfCorrectiveAction() {
        return descriptionOfCorrectiveAction;
    }

    public void setDescriptionOfCorrectiveAction(String descriptionOfCorrectiveAction) {
        this.descriptionOfCorrectiveAction = descriptionOfCorrectiveAction;
    }

    public String getYearOfProductionOfDefectiveEquipment() {
        return yearOfProductionOfDefectiveEquipment;
    }

    public void setYearOfProductionOfDefectiveEquipment(String yearOfProductionOfDefectiveEquipment) {
        this.yearOfProductionOfDefectiveEquipment = yearOfProductionOfDefectiveEquipment;
    }

    public Integer getNumberOfOverhaulsOfDefectiveEquipment() {
        return numberOfOverhaulsOfDefectiveEquipment;
    }

    public void setNumberOfOverhaulsOfDefectiveEquipment(Integer numberOfOverhaulsOfDefectiveEquipment) {
        this.numberOfOverhaulsOfDefectiveEquipment = numberOfOverhaulsOfDefectiveEquipment;
    }

    public String getSerialNoOf() {
        return serialNoOf;
    }

    public void setSerialNoOf(String serialNoOf) {
        this.serialNoOf = serialNoOf;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFhaEvent() {
        return fhaEvent;
    }

    public void setFhaEvent(String fhaEvent) {
        this.fhaEvent = fhaEvent;
    }
}
