package cz.cvut.kbss.study.dto;

import java.net.URI;

/**
 * Created by Miroslav Blasko on 1.9.17.
 */
public class PatientRecordSummaryDto {

    private String localName;
    private URI recordURI;

    public URI getRecordUri() {
        return recordURI;
    }

    public void setRecordURI(URI recordURI) {
        this.recordURI = recordURI;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    //    private String key;

//
//    private User author;

//    private Date dateCreated;
//
//    private Date lastModified;
//
//    private User lastModifiedBy;

//    private Institution institution;
//
//    private Question question;
//

//    @Override
//    public String toString() {
//        return "PatientRecord{" +
//            "localName=" + localName +
//            "dateCreated=" + dateCreated +
//            ", institution=" + institution +
//            "} " + super.toString();
//    }
}
