package edu.ualberta.med.biobank.forms;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Section;
import org.springframework.util.Assert;

import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.Sdata;
import edu.ualberta.med.biobank.model.StorageContainer;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.Node;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.treeview.SiteAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;
import edu.ualberta.med.biobank.widgets.BiobankCollectionTable;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class StudyViewForm extends BiobankViewForm {

	public static final String ID =
		"edu.ualberta.med.biobank.forms.StudyViewForm";

	private StudyAdapter studyAdapter;
	private Study study;

	private BiobankCollectionTable patientsTable;
	private BiobankCollectionTable sContainersTable;
	private BiobankCollectionTable sDatasTable;
	
	@Override
	public void init(IEditorSite editorSite, IEditorInput input) 
	throws PartInitException {        
		super.init(editorSite, input);

		Node node = ((FormInput) input).getNode();
		Assert.notNull(node, "Null editor input");

		if (node instanceof StudyAdapter) {
			studyAdapter = (StudyAdapter) node;

			// retrieve info from database because could have been modified after first opening

			//			study = studyAdapter.getStudy();
			retrieveStudy();
			setPartName("Study " + study.getName());
		}
		else {
			Assert.isTrue(false, "Invalid editor input: object of type "
					+ node.getClass().getName());
		}
	}

	protected void createFormContent() {

		if (study.getName() != null) {
			form.setText("Study: " + study.getName());
		}

		createReloadSection();
		
		GridLayout layout = new GridLayout(1, false);
		form.getBody().setLayout(layout);
		form.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite client = toolkit.createComposite(form.getBody());
		client.setLayout(new GridLayout(2, false));
		client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));        
		toolkit.paintBordersFor(client); 

		createBoundWidget(client, Label.class, SWT.NONE, "Short Name",
				PojoObservables.observeValue(study, "nameShort"));

		createBoundWidget(client, Label.class, SWT.NONE, "Activity Status",
				PojoObservables.observeValue(study, "activityStatus"));

		createBoundWidget(client, Label.class, 
				SWT.NONE, "Comments", PojoObservables.observeValue(study, "comment"));

		Node clinicGroupNode = 
			((SiteAdapter) studyAdapter.getParent().getParent()).getClinicGroupNode();

		FormUtils.createClinicSection(toolkit, form.getBody(), clinicGroupNode,
				study.getClinicCollection());


		createPatientsSection();
		createStorageContainerSection();
		createDataCollectedSection();        
	}

	private void createPatientsSection() {        
		Section section = createSection("Patients");  

		PatientAdapter[] patients = loadPatients();

		String [] headings = new String[] {"Patient Number"};      
		patientsTable = new BiobankCollectionTable(section, SWT.NONE, headings, patients);
		section.setClient(patientsTable);
		patientsTable.adaptToToolkit(toolkit);   
		toolkit.paintBordersFor(patientsTable);

		patientsTable.getTableViewer().addDoubleClickListener(
				FormUtils.getBiobankCollectionDoubleClickListener());
	}

	private PatientAdapter[] loadPatients() {
		// hack required here because site.getStudyCollection().toArray(new Study[0])
		// returns Object[].        
		int count = 0;
		Collection<Patient> patients = study.getPatientCollection();
		PatientAdapter [] arr = new PatientAdapter [patients.size()];
		for (Patient patient : patients) {
			arr[count] = new PatientAdapter(studyAdapter, patient);
			++count;
		}
		return arr;
	}

	private void reloadPatients() {
		patientsTable.getTableViewer().setInput(loadPatients());
	}

	private void createStorageContainerSection() {        
		Section section = createSection("Storage Containers");  

		StorageContainer[] sContainers = loadStorageContainers();

		String [] headings = new String[] {"Name", "Status", "Bar Code", "Full", "Temperature"};      
		sContainersTable = new BiobankCollectionTable(section, SWT.NONE, headings, sContainers);
		section.setClient(sContainersTable);
		sContainersTable.adaptToToolkit(toolkit);   
		toolkit.paintBordersFor(sContainersTable);

		sContainersTable.getTableViewer().addDoubleClickListener(
				FormUtils.getBiobankCollectionDoubleClickListener());
	}

	private StorageContainer[] loadStorageContainers() {
		// hack required here because site.getStudyCollection().toArray(new Study[0])
		// returns Object[].        
		int count = 0;
		Collection<StorageContainer> storageContainers = study.getStorageContainerCollection();
		StorageContainer [] arr = new StorageContainer [storageContainers.size()];
		Iterator<StorageContainer> it = storageContainers.iterator();
		while (it.hasNext()) {
			arr[count] = it.next();
			++count;
		}
		return arr;
	}
	
	private void reloadStorageContainers() {
		sContainersTable.getTableViewer().setInput(loadStorageContainers());
	}

	private void createDataCollectedSection() {           
		Section section = createSection("Study Data Collected");

		Sdata[] sDatas = loadSDatas();

		String [] headings = new String[] {"Name", "Valid Values (optional)"};      
		sDatasTable = new BiobankCollectionTable(section, SWT.NONE, headings, sDatas);
		section.setClient(sDatasTable);
		sDatasTable.adaptToToolkit(toolkit); 
		toolkit.paintBordersFor(sDatasTable);

		sDatasTable.getTableViewer().addDoubleClickListener(
				FormUtils.getBiobankCollectionDoubleClickListener());
	}

	private Sdata[] loadSDatas() {
		// hack required here because site.getStudyCollection().toArray(new Study[0])
		// returns Object[].        
		int count = 0;
		Collection<Sdata> sdatas = study.getSdataCollection();
		Sdata [] arr = new Sdata [sdatas.size()];
		Iterator<Sdata> it = sdatas.iterator();
		while (it.hasNext()) {
			arr[count] = it.next();
			++count;
		}
		return arr;
	}

	private void reloadSDatas() {
		sDatasTable.getTableViewer().setInput(loadSDatas());
	}


	@Override
	protected void reload() {    	
		retrieveStudy();
		reloadPatients();	
		reloadStorageContainers();
		reloadSDatas();
	}

	private void retrieveStudy() {
		List<Study> result;
		Study searchStudy = new Study();
		searchStudy.setId(studyAdapter.getStudy().getId());
		try {
			result = studyAdapter.getAppService().search(Study.class, searchStudy);
			Assert.isTrue(result.size() == 1);
			study = result.get(0);
			studyAdapter.setStudy(study);
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
