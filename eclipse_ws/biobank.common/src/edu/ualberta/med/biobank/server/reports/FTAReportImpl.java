package edu.ualberta.med.biobank.server.reports;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.PatientVisit;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class FTAReportImpl extends AbstractReport {

    // note that this will consider patient visits without any aliquots or
    // patient visits without any aliquots of the specific sample type wanted
    private static final String QUERY = "select a from "
        + Aliquot.class.getName()
        + " a where a in (select min(a2) from "
        + Aliquot.class.getName()
        + " a2 where a2.patientVisit.dateProcessed = "
        + " (select min(pv.dateProcessed) from "
        + PatientVisit.class.getName()
        + " pv where pv.patient.id = a2.patientVisit.patient "
        + " and pv.patient.study.nameShort = ? and pv.shipment.site "
        + SITE_OPERATOR
        + SITE_ID
        + ")"
        + " and a2.sampleType.nameShort = '"
        + FTA_CARD_SAMPLE_TYPE_NAME
        + "' and a2.patientVisit.dateProcessed > ? group by a2.patientVisit.patient.pnumber)"
        + " order by a.patientVisit.patient.pnumber";

    public FTAReportImpl(BiobankReport report) {
        super(QUERY, report);
    }

    @Override
    public List<Object> postProcess(WritableApplicationService appService,
        List<Object> results) {
        ArrayList<Object> modifiedResults = new ArrayList<Object>();
        // get the info
        for (Object ob : results) {
            Aliquot a = (Aliquot) ob;
            String pnumber = a.getPatientVisit().getPatient().getPnumber();
            String inventoryId = a.getInventoryId();
            String dateProcessed = DateFormatter.formatAsDate(a
                .getPatientVisit().getDateProcessed());
            String stName = a.getSampleType().getNameShort();
            AliquotWrapper aliquotWrapper = new AliquotWrapper(appService, a);
            String aliquotLabel = aliquotWrapper.getPositionString(true, true);
            modifiedResults.add(new Object[] { pnumber, dateProcessed,
                inventoryId, stName, aliquotLabel });
        }
        return modifiedResults;
    }
}