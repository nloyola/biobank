package edu.ualberta.med.biobank.common.action.study;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.study.StudyGetClinicInfoAction.ClinicInfo;
import edu.ualberta.med.biobank.common.util.NotAProxy;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.model.User;

public class StudyGetClinicInfoAction implements Action<ArrayList<ClinicInfo>> {
    private static final long serialVersionUID = 1L;

    // @formatter:off
    @SuppressWarnings("nls")
    private static final String STUDY_CONTACTS_HQL =
        "SELECT clinics,contacts "
        + "FROM " + Study.class.getName() + " study"
        + " INNER JOIN study.contactCollection contacts"
        + " INNER JOIN contacts.clinic clinics"
        + " WHERE study.id=?";
    // @formatter:on

    // @formatter:off
    @SuppressWarnings("nls")
    private static final String STUDY_CLINIC_INFO_HQL =
        "SELECT clinics,COUNT(DISTINCT patients)," 
        + " COUNT(DISTINCT cevents)"
        + " FROM " + Study.class.getName() + " study"
        + " INNER JOIN study.contactCollection contacts"
        + " INNER JOIN contacts.clinic clinics"
        + " LEFT JOIN clinics.originInfoCollection originInfo"
        + " LEFT JOIN originInfo.specimenCollection specimens"
        + " LEFT JOIN specimens.collectionEvent cevents"
        + " LEFT JOIN cevents.patient patients"
        + " WHERE study.id = ?"
        + " GROUP BY clinics";
    // @formatter:on

    private final Integer studyId;

    public StudyGetClinicInfoAction(Integer studyId) {
        this.studyId = studyId;
    }

    public StudyGetClinicInfoAction(Study study) {
        this(study.getId());
    }

    @Override
    public boolean isAllowed(User user, Session session) throws ActionException {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<ClinicInfo> run(User user, Session session)
        throws ActionException {

        // first get contacts by clinic
        Map<Clinic, List<Contact>> contactsByClinic =
            new HashMap<Clinic, List<Contact>>();
        Query query = session.createQuery(STUDY_CONTACTS_HQL);
        query.setParameter(0, studyId);

        List<Object[]> results = query.list();
        for (Object[] row : results) {
            Clinic clinic = (Clinic) row[0];
            Contact contact = (Contact) row[1];
            List<Contact> contactList = contactsByClinic.get(clinic);
            if (contactList == null) {
                contactList = new ArrayList<Contact>();
                contactList.add(contact);
                contactsByClinic.put(clinic, contactList);
            } else {
                contactList.add(contact);
            }
        }

        ArrayList<ClinicInfo> infos = new ArrayList<ClinicInfo>();
        query = session.createQuery(STUDY_CLINIC_INFO_HQL);
        query.setParameter(0, studyId);

        results = query.list();
        for (Object[] row : results) {
            Clinic clinic = (Clinic) row[0];
            List<Contact> contactList = contactsByClinic.get(clinic);
            if (contactList == null) {
                throw new ActionException("clinic should have contacts: "
                    + clinic.getNameShort());
            }
            ClinicInfo info = new ClinicInfo(clinic, (Long) row[1],
                (Long) row[2], contactList);
            infos.add(info);
        }

        return infos;
    }

    public static class ClinicInfo implements Serializable, NotAProxy {
        private static final long serialVersionUID = 1L;

        private final Clinic clinic;
        private final Long patientCount;
        private final Long ceventCount;
        private final List<Contact> contacts;

        public ClinicInfo(Clinic clinic, Long patientCount, Long ceventCount,
            List<Contact> contacts) {
            this.clinic = clinic;
            this.patientCount = patientCount;
            this.ceventCount = ceventCount;
            this.contacts = contacts;
        }

        public Clinic getClinic() {
            return clinic;
        }

        public Long getPatientCount() {
            return patientCount;
        }

        public Long getCeventCount() {
            return ceventCount;
        }

        public List<Contact> getContacts() {
            return contacts;
        }
    }
}
