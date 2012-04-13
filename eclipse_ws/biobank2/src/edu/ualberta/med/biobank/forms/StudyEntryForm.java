package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.specimenType.SpecimenTypeGetAllAction;
import edu.ualberta.med.biobank.common.action.study.StudyGetClinicInfoAction.ClinicInfo;
import edu.ualberta.med.biobank.common.action.study.StudyGetInfoAction;
import edu.ualberta.med.biobank.common.action.study.StudyInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.AliquotedSpecimenSaveInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.SourceSpecimenSaveInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.StudyEventAttrSaveInfo;
import edu.ualberta.med.biobank.common.peer.StudyPeer;
import edu.ualberta.med.biobank.common.wrappers.AliquotedSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.CommentWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.common.wrappers.GlobalEventAttrWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SourceSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.gui.common.BgcLogger;
import edu.ualberta.med.biobank.gui.common.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.gui.common.widgets.BgcEntryFormWidgetListener;
import edu.ualberta.med.biobank.gui.common.widgets.MultiSelectEvent;
import edu.ualberta.med.biobank.gui.common.widgets.utils.ComboSelectionUpdate;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.AliquotedSpecimen;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.EventAttrCustom;
import edu.ualberta.med.biobank.model.SourceSpecimen;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.model.StudyEventAttr;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.admin.StudyAdapter;
import edu.ualberta.med.biobank.widgets.EventAttrWidget;
import edu.ualberta.med.biobank.widgets.infotables.CommentsInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.entry.AliquotedSpecimenEntryInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.entry.ClinicAddInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.entry.SourceSpecimenEntryInfoTable;
import edu.ualberta.med.biobank.widgets.utils.GuiUtil;

public class StudyEntryForm extends BiobankEntryForm {
    public static final String ID =
        "edu.ualberta.med.biobank.forms.StudyEntryForm"; //$NON-NLS-1$

    private static final String MSG_NEW_STUDY_OK =
        Messages.StudyEntryForm_creation_msg;

    private static final String MSG_STUDY_OK =
        Messages.StudyEntryForm_edition_msg;

    private static final String DATE_PROCESSED_INFO_FIELD_NAME =
        Messages.study_visit_info_dateProcessed;

    protected static BgcLogger LOGGER = BgcLogger
        .getLogger(StudyEntryForm.class.getName());

    private static class StudyEventAttrCustom extends EventAttrCustom {
        public EventAttrWidget widget;
        public boolean inStudy;
    }

    private final StudyWrapper study = new StudyWrapper(
        SessionManager.getAppService());

    private ClinicAddInfoTable contactEntryTable;

    private final List<StudyEventAttrCustom> pvCustomInfoList;

    private AliquotedSpecimenEntryInfoTable aliquotedSpecimenEntryTable;

    private final BgcEntryFormWidgetListener listener =
        new BgcEntryFormWidgetListener() {
            @Override
            public void selectionChanged(MultiSelectEvent event) {
                setDirty(true);
            }
        };

    private ComboViewer activityStatusComboViewer;

    private SourceSpecimenEntryInfoTable sourceSpecimenEntryTable;

    private CommentsInfoTable commentEntryTable;

    private final CommentWrapper comment = new CommentWrapper(
        SessionManager.getAppService());

    private StudyInfo studyInfo;

    private List<SpecimenTypeWrapper> specimenTypeWrappers;

    public StudyEntryForm() {
        super();
        pvCustomInfoList = new ArrayList<StudyEventAttrCustom>();
    }

    @Override
    public void init() throws Exception {
        Assert.isTrue((adapter instanceof StudyAdapter),
            "Invalid editor input: object of type " //$NON-NLS-1$
                + adapter.getClass().getName());

        updateStudyInfo(adapter.getId());

        String tabName;
        if (study.isNew()) {
            tabName = Messages.StudyEntryForm_title_new;
            study.setActivityStatus(ActivityStatus.ACTIVE);
        } else {
            tabName =
                NLS.bind(Messages.StudyEntryForm_title_edit,
                    study.getNameShort());
        }
        setPartName(tabName);
    }

    private void updateStudyInfo(Integer id) throws Exception {
        if (id != null) {
            studyInfo =
                SessionManager.getAppService().doAction(
                    new StudyGetInfoAction(id));
            study.setWrappedObject(studyInfo.getStudy());
        } else {
            studyInfo = new StudyInfo();
            study.setWrappedObject(new Study());
        }

        comment.setWrappedObject(new Comment());

        List<SpecimenType> specimenTypes =
            SessionManager.getAppService()
                .doAction(new SpecimenTypeGetAllAction()).getList();
        specimenTypeWrappers =
            ModelWrapper.wrapModelCollection(SessionManager.getAppService(),
                specimenTypes, SpecimenTypeWrapper.class);

        ((AdapterBase) adapter).setModelObject(study);
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText(Messages.StudyEntryForm_main_title);
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        page.setLayout(new GridLayout(1, false));

        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        setFirstControl(createBoundWidgetWithLabel(client, BgcBaseText.class,
            SWT.NONE, "Name", null, study,
            StudyPeer.NAME.getName(), new NonEmptyStringValidator(
                Messages.StudyEntryForm_name_validator_msg)));

        createBoundWidgetWithLabel(client, BgcBaseText.class, SWT.NONE,
            "Name Short", null, study,
            StudyPeer.NAME_SHORT.getName(), new NonEmptyStringValidator(
                Messages.StudyEntryForm_nameShort_validator_msg));

        activityStatusComboViewer =
            createComboViewer(client, "Activity status",
                ActivityStatus.valuesList(), study.getActivityStatus(),
                Messages.StudyEntryForm_activity_validator_msg,
                new ComboSelectionUpdate() {
                    @Override
                    public void doSelection(Object selectedObject) {
                        study
                            .setActivityStatus((ActivityStatus) selectedObject);
                    }
                });

        createCommentSection();
        createClinicSection();
        createSourceSpecimensSection();
        createAliquotedSpecimensSection();
        createEventAttrSection();
        createButtonsSection();
    }

    private void createClinicSection() {
        Section section = createSection(Messages.StudyEntryForm_contacts_title);
        List<Contact> contacts = new ArrayList<Contact>();
        for (ClinicInfo clinicInfo : studyInfo.getClinicInfos())
            contacts.addAll(clinicInfo.getContacts());
        contactEntryTable =
            new ClinicAddInfoTable(section, contacts);
        contactEntryTable.adaptToToolkit(toolkit, true);
        contactEntryTable.addSelectionChangedListener(listener);

        addSectionToolbar(section, Messages.StudyEntryForm_contacts_button_add,
            new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    contactEntryTable.createClinicContact();
                }
            });
        section.setClient(contactEntryTable);
    }

    private void createCommentSection() {
        Composite client = createSectionWithClient("Comments");
        GridLayout gl = new GridLayout(2, false);

        client.setLayout(gl);
        commentEntryTable =
            new CommentsInfoTable(client, study.getCommentCollection(false));
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        commentEntryTable.setLayoutData(gd);
        createBoundWidgetWithLabel(client, BgcBaseText.class, SWT.MULTI,
            "Add a comment", null, comment, "message", null); //$NON-NLS-1$
    }

    private void createSourceSpecimensSection() {
        Section section =
            createSection(Messages.StudyEntryForm_source_specimens_title);
        sourceSpecimenEntryTable =
            new SourceSpecimenEntryInfoTable(
                section,
                ModelWrapper.wrapModelCollection(
                    SessionManager.getAppService(),
                    studyInfo.getSourceSpecimens(), SourceSpecimenWrapper.class),
                specimenTypeWrappers);
        sourceSpecimenEntryTable.adaptToToolkit(toolkit, true);
        sourceSpecimenEntryTable.addSelectionChangedListener(listener);

        addSectionToolbar(section,
            Messages.StudyEntryForm_source_specimens_button_add,
            new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    sourceSpecimenEntryTable.addSourceSpecimen();
                    aliquotedSpecimenEntryTable
                        .setAvailableSpecimenTypes(sourceSpecimenEntryTable
                            .getList());
                }
            });
        section.setClient(sourceSpecimenEntryTable);
    }

    private void createAliquotedSpecimensSection() {
        Composite client =
            createSectionWithClient(Messages.StudyEntryForm_aliquoted_specimens_title);
        GridLayout layout = (GridLayout) client.getLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 0;

        toolkit.createLabel(client, Messages.StudyEntryForm_1, SWT.LEFT);

        aliquotedSpecimenEntryTable =
            new AliquotedSpecimenEntryInfoTable(client,
                ModelWrapper.wrapModelCollection(
                    SessionManager.getAppService(),
                    studyInfo.getAliquotedSpcs(),
                    AliquotedSpecimenWrapper.class), true, true);
        aliquotedSpecimenEntryTable.adaptToToolkit(toolkit, true);
        aliquotedSpecimenEntryTable.addSelectionChangedListener(listener);

        aliquotedSpecimenEntryTable
            .setAvailableSpecimenTypes(sourceSpecimenEntryTable
                .getList());

        addSectionToolbar((Section) client.getParent(),
            Messages.StudyEntryForm_aliquoted_specimens_button_add,
            new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    aliquotedSpecimenEntryTable.addAliquotedSpecimen();
                }
            }, AliquotedSpecimenWrapper.class);
    }

    private void createEventAttrSection() throws Exception {
        Composite client =
            createSectionWithClient(Messages.StudyEntryForm_visit_info_title);
        GridLayout gl = (GridLayout) client.getLayout();
        gl.numColumns = 1;

        toolkit.createLabel(client, Messages.StudyEntryForm_2,
            SWT.LEFT);

        StudyEventAttrCustom studyEventAttrCustom;

        Map<String, StudyEventAttr> studyEventAttrLabelMap =
            new HashMap<String, StudyEventAttr>();
        for (StudyEventAttr sea : studyInfo.getStudyEventAttrs()) {
            studyEventAttrLabelMap.put(sea.getGlobalEventAttr().getLabel(),
                sea);
        }

        for (GlobalEventAttrWrapper geAttr : GlobalEventAttrWrapper
            .getAllGlobalEventAttrs(SessionManager.getAppService())) {
            String label = geAttr.getLabel();
            boolean selected = false;
            studyEventAttrCustom = new StudyEventAttrCustom();
            studyEventAttrCustom.setGlobalEventAttr(geAttr.getWrappedObject());
            studyEventAttrCustom.setLabel(label);
            studyEventAttrCustom.setType(geAttr.getTypeName());
            StudyEventAttr sea = studyEventAttrLabelMap.get(label);
            if (sea != null) {
                studyEventAttrCustom.setStudyEventAttrId(sea.getId());
                String permissible = sea.getPermissible();
                if ((permissible != null) && !permissible.isEmpty()) {
                    studyEventAttrCustom.setAllowedValues(permissible
                        .split(";")); //$NON-NLS-1$
                }
                selected =
                    sea.getActivityStatus().equals(ActivityStatus.ACTIVE);
            }
            studyEventAttrCustom.setIsDefault(false);
            studyEventAttrCustom.widget =
                new EventAttrWidget(client, SWT.NONE, studyEventAttrCustom,
                    selected);
            studyEventAttrCustom.widget.addSelectionChangedListener(listener);
            studyEventAttrCustom.inStudy = (sea != null);
            pvCustomInfoList.add(studyEventAttrCustom);
        }
    }

    private void createButtonsSection() {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);
    }

    @Override
    protected String getOkMessage() {
        if (study.getId() == null) {
            return MSG_NEW_STUDY_OK;
        }
        return MSG_STUDY_OK;
    }

    @Override
    protected void saveForm() throws Exception {
        // save of source specimen is made inside the entryinfotable

        study.getWrappedObject().setContacts(
            new HashSet<Contact>(contactEntryTable.getList()));

        final StudySaveAction saveAction = new StudySaveAction();
        saveAction.setId(study.getId());
        saveAction.setName(study.getName());
        saveAction.setNameShort(study.getNameShort());
        saveAction.setActivityStatus(study.getActivityStatus());
        saveAction.setContactIds(getContactInfos());
        saveAction.setSourceSpecimenSaveInfo(getSourceSpecimenInfos());
        saveAction.setAliquotSpecimenSaveInfo(getAliquotedSpecimenInfos());
        saveAction.setStudyEventAttrSaveInfo(getStudyEventAttrInfos());
        saveAction.setCommentText(comment.getMessage());

        Integer id =
            SessionManager.getAppService().doAction(saveAction).getId();
        study.setId(id);
        ((AdapterBase) adapter).setModelObject(study);
    }

    private HashSet<Integer> getContactInfos() {
        HashSet<Integer> contactIds = new HashSet<Integer>();

        for (ContactWrapper wrapper : study.getContactCollection(false)) {
            contactIds.add(wrapper.getId());
        }
        return contactIds;
    }

    private HashSet<SourceSpecimenSaveInfo> getSourceSpecimenInfos() {
        HashSet<SourceSpecimenSaveInfo> sourceSpecimenSaveInfos =
            new HashSet<SourceSpecimenSaveInfo>();

        Set<SourceSpecimen> newSourceSpcs =
            new HashSet<SourceSpecimen>(studyInfo.getSourceSpecimens());

        // remove the ones deleted
        for (SourceSpecimenWrapper wrapper : sourceSpecimenEntryTable
            .getDeletedSourceSpecimens()) {
            newSourceSpcs.remove(wrapper.getWrappedObject());
        }

        // add the ones not modified
        for (SourceSpecimen ss : newSourceSpcs) {
            if (!sourceSpecimenEntryTable
                .getAddedOrModifiedSourceSpecimens().contains(ss)) {
                sourceSpecimenSaveInfos.add(new SourceSpecimenSaveInfo(ss));
            }
        }

        // add the modified ones
        for (SourceSpecimenWrapper wrapper : sourceSpecimenEntryTable
            .getAddedOrModifiedSourceSpecimens()) {
            sourceSpecimenSaveInfos.add(new SourceSpecimenSaveInfo(
                wrapper.getWrappedObject()));
        }
        return sourceSpecimenSaveInfos;
    }

    private HashSet<AliquotedSpecimenSaveInfo> getAliquotedSpecimenInfos() {
        HashSet<AliquotedSpecimenSaveInfo> aliquotedSpecimenSaveInfos =
            new HashSet<AliquotedSpecimenSaveInfo>();

        Set<AliquotedSpecimen> newAliquotedSpcs =
            new HashSet<AliquotedSpecimen>(studyInfo.getAliquotedSpcs());

        // remove the ones deleted
        for (AliquotedSpecimenWrapper wrapper : aliquotedSpecimenEntryTable
            .getDeletedAliquotedSpecimens()) {
            newAliquotedSpcs.remove(wrapper.getWrappedObject());
        }

        // add the ones not modified
        for (AliquotedSpecimen as : newAliquotedSpcs) {
            if (!aliquotedSpecimenEntryTable
                .getAddedOrModifiedAliquotedSpecimens().contains(as)) {
                aliquotedSpecimenSaveInfos
                    .add(new AliquotedSpecimenSaveInfo(as));
            }
        }

        // add the modified ones
        for (AliquotedSpecimenWrapper wrapper : aliquotedSpecimenEntryTable
            .getAddedOrModifiedAliquotedSpecimens()) {
            aliquotedSpecimenSaveInfos.add(new AliquotedSpecimenSaveInfo(
                wrapper.getWrappedObject()));
        }

        return aliquotedSpecimenSaveInfos;
    }

    private HashSet<StudyEventAttrSaveInfo> getStudyEventAttrInfos() {
        final HashSet<StudyEventAttrSaveInfo> studyEventAttrSaveInfos =
            new HashSet<StudyEventAttrSaveInfo>();

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {

                for (StudyEventAttrCustom studyEventAttrCustom : pvCustomInfoList) {
                    String label = studyEventAttrCustom.getLabel();
                    if (label.equals(DATE_PROCESSED_INFO_FIELD_NAME)
                        || (!studyEventAttrCustom.widget.getSelected() && !studyEventAttrCustom.inStudy))
                        continue;

                    StudyEventAttrSaveInfo studyEventAttrSaveInfo =
                        new StudyEventAttrSaveInfo();

                    studyEventAttrSaveInfo.id =
                        studyEventAttrCustom.getStudyEventAttrId();
                    studyEventAttrSaveInfo.globalEventAttrId =
                        studyEventAttrCustom.getGlobalEventAttrId();
                    studyEventAttrSaveInfo.permissible =
                        studyEventAttrCustom.widget.getValues();

                    // TODO: required not used at the moment
                    studyEventAttrSaveInfo.required = false;

                    if (!studyEventAttrCustom.widget.getSelected()
                        && studyEventAttrCustom.inStudy) {
                        studyEventAttrSaveInfo.activityStatus =
                            ActivityStatus.CLOSED;
                    } else if (studyEventAttrCustom.widget.getSelected()) {
                        studyEventAttrSaveInfo.activityStatus =
                            ActivityStatus.ACTIVE;
                    }

                    LOGGER.debug(studyEventAttrSaveInfo.toString());
                    studyEventAttrSaveInfos.add(studyEventAttrSaveInfo);
                }
            }
        });
        return studyEventAttrSaveInfos;
    }

    @Override
    public String getNextOpenedFormId() {
        return StudyViewForm.ID;
    }

    @Override
    public void setValues() throws Exception {
        if (study.isNew()) {
            study.setActivityStatus(ActivityStatus.ACTIVE);
        }

        GuiUtil.reset(activityStatusComboViewer, study.getActivityStatus());
        contactEntryTable.reload();
        aliquotedSpecimenEntryTable.reload();
        sourceSpecimenEntryTable.reload(ModelWrapper.wrapModelCollection(
            SessionManager.getAppService(),
            studyInfo.getSourceSpecimens(), SourceSpecimenWrapper.class));
        commentEntryTable.setList(study.getCommentCollection(false));
        resetPvCustomInfo();
    }

    private void resetPvCustomInfo() throws Exception {
        Map<String, StudyEventAttr> studyEventAttrLabelMap =
            new HashMap<String, StudyEventAttr>();
        for (StudyEventAttr sea : studyInfo.getStudyEventAttrs()) {
            studyEventAttrLabelMap.put(sea.getGlobalEventAttr().getLabel(),
                sea);
        }

        for (StudyEventAttrCustom studyPvAttrCustom : pvCustomInfoList) {
            boolean selected = false;
            StudyEventAttr sea =
                studyEventAttrLabelMap.get(studyPvAttrCustom.getLabel());
            if (sea != null) {
                String permissible = sea.getPermissible();
                if ((permissible != null) && !permissible.isEmpty()) {
                    studyPvAttrCustom.setAllowedValues(permissible
                        .split(";")); //$NON-NLS-1$
                }
                selected = true;
                studyPvAttrCustom.inStudy = true;
            }
            selected |= (studyPvAttrCustom.getAllowedValues() != null);
            studyPvAttrCustom.widget.setSelected(selected);
            studyPvAttrCustom.widget.reloadAllowedValues(studyPvAttrCustom);
        }
    }
}
