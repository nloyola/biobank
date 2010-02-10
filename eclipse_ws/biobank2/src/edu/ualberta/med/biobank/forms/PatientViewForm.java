package edu.ualberta.med.biobank.forms;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.widgets.infotables.PatientVisitInfoTable;

public class PatientViewForm extends BiobankViewForm {
    public static final String ID = "edu.ualberta.med.biobank.forms.PatientViewForm";

    private PatientAdapter patientAdapter;

    private PatientWrapper patient;

    private Text siteLabel;

    private Text visitCountLabel;

    private PatientVisitInfoTable visitsTable;

    @Override
    public void init() throws Exception {
        Assert.isTrue(adapter instanceof PatientAdapter,
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        patientAdapter = (PatientAdapter) adapter;
        patient = patientAdapter.getWrapper();
        retrievePatient();
        setPartName("Patient " + patient.getPnumber());
    }

    private void retrievePatient() throws Exception {
        patient.reload();
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Patient: " + patient.getPnumber());
        form.getBody().setLayout(new GridLayout(1, false));
        form.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        form.setImage(BioBankPlugin.getDefault().getImageRegistry().get(
            BioBankPlugin.IMG_PATIENT));

        createPatientSection();
        createPatientVisitSection();
        setValues();
    }

    private void createPatientSection() {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        siteLabel = createReadOnlyField(client, SWT.NONE, "Site");
        visitCountLabel = createReadOnlyField(client, SWT.NONE, "Total Visits");
    }

    private void createPatientVisitSection() {
        Section section = createSection("Patient Visits");

        visitsTable = new PatientVisitInfoTable(section, patient
            .getPatientVisitCollection());
        section.setClient(visitsTable);
        visitsTable.adaptToToolkit(toolkit, true);
        visitsTable.getTableViewer().addDoubleClickListener(
            collectionDoubleClickListener);
    }

    private void setValues() {
        setTextValue(siteLabel, patient.getStudy().getSite().getName());
        setTextValue(visitCountLabel, patient.getPatientVisitCollection()
            .size());
    }

    @Override
    protected void reload() throws Exception {
        setValues();
        retrievePatient();
        setPartName("Patient " + patient.getPnumber());
        form.setText("Patient: " + patient.getPnumber());
        visitsTable.setCollection(patient.getPatientVisitCollection());
    }

    @Override
    protected String getEntryFormId() {
        return PatientEntryForm.ID;
    }
}
