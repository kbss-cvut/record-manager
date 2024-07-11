package cz.cvut.kbss.study.model.export;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.Vocabulary;

import java.net.URI;
import java.util.Date;

@SparqlResultSetMappings(value =
@SparqlResultSetMapping(name = "RawRecord", entities = {
        @EntityResult(entityClass = RawRecord.class)
})
)
@OWLClass(iri = Vocabulary.s_c_patient_record)
public class RawRecord {
    @Id
    private URI uri;

    @OWLDataProperty(iri = "http://created")
    private Date created;
    @OWLDataProperty(iri = "http://modified")
    private Date lastModified;
    @OWLDataProperty(iri = "http://label")
    private String label;
    @OWLObjectProperty(iri = "http://phase")
    private URI phase;
    @OWLObjectProperty(iri = "http://institution")
    private URI institution;
    @OWLObjectProperty(iri = "http://aircraftType")
    private URI aircraftType;
    @OWLObjectProperty(iri = "http://fuselage")
    private String fuselage;
    @OWLObjectProperty(iri = "http://ac_comp")
    private URI ac_comp;
    @OWLDataProperty(iri = "http://failDate")
    private String failDate;

    @OWLDataProperty(iri = "http://flightHours")
    private String flightHours;
    @OWLDataProperty(iri = "http://numberOfAirframeOverhauls")
    private Integer numberOfAirframeOverhauls;
    @OWLObjectProperty(iri = "http://classificationOfOccurrence")
    private URI classificationOfOccurrence;
    @OWLObjectProperty(iri = "http://failureAscertainmentCircumstances")
    private URI failureAscertainmentCircumstances;
    @OWLObjectProperty(iri = "http://repeatedFailure")
    private URI repeatedFailure;
    @OWLObjectProperty(iri = "http://failureCause")
    private URI failureCause;
    @OWLObjectProperty(iri = "http://consequence")
    private URI consequence;
    @OWLObjectProperty(iri = "http://mission")
    private URI mission;
    @OWLObjectProperty(iri = "http://repair")
    private URI repair;
    @OWLDataProperty(iri = "http://repairDuration")
    private String repairDuration;
    @OWLDataProperty(iri = "http://averageNumberOfMenDuringRepairment")
    private Double averageNumberOfMenDuringRepairment;
    @OWLDataProperty(iri = "http://failureDescription")
    private String failureDescription;
    @OWLDataProperty(iri = "http://descriptionOfCorrectiveAction")
    private String descriptionOfCorrectiveAction;
    @OWLDataProperty(iri = "http://yearOfProductionOfDefectiveEquipment")
    private String yearOfProductionOfDefectiveEquipment;
    @OWLDataProperty(iri = "http://numberOfOverhaulsOfDefectiveEquipment")
    private Integer numberOfOverhaulsOfDefectiveEquipment;
    @OWLDataProperty(iri = "http://serialNoOf")
    private String serialNoOf;
    @OWLDataProperty(iri = "http://notes")
    private String notes;
    @OWLObjectProperty(iri = "http://fhaEvent")
    private URI fhaEvent;


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

    public URI getPhase() {
        return phase;
    }

    public void setPhase(URI phase) {
        this.phase = phase;
    }

    public URI getInstitution() {
        return institution;
    }

    public void setInstitution(URI institution) {
        this.institution = institution;
    }

    public URI getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(URI aircraftType) {
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

    public URI getClassificationOfOccurrence() {
        return classificationOfOccurrence;
    }

    public void setClassificationOfOccurrence(URI classificationOfOccurrence) {
        this.classificationOfOccurrence = classificationOfOccurrence;
    }

    public URI getFailureAscertainmentCircumstances() {
        return failureAscertainmentCircumstances;
    }

    public void setFailureAscertainmentCircumstances(URI failureAscertainmentCircumstances) {
        this.failureAscertainmentCircumstances = failureAscertainmentCircumstances;
    }

    public URI getRepeatedFailure() {
        return repeatedFailure;
    }

    public void setRepeatedFailure(URI repeatedFailure) {
        this.repeatedFailure = repeatedFailure;
    }

    public URI getFailureCause() {
        return failureCause;
    }

    public void setFailureCause(URI failureCause) {
        this.failureCause = failureCause;
    }

    public URI getConsequence() {
        return consequence;
    }

    public void setConsequence(URI consequence) {
        this.consequence = consequence;
    }

    public URI getMission() {
        return mission;
    }

    public void setMission(URI mission) {
        this.mission = mission;
    }

    public URI getRepair() {
        return repair;
    }

    public void setRepair(URI repair) {
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

    public URI getFhaEvent() {
        return fhaEvent;
    }

    public void setFhaEvent(URI fhaEvent) {
        this.fhaEvent = fhaEvent;
    }
}
