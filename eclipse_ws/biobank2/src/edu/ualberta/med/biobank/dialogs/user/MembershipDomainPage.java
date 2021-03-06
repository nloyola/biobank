package edu.ualberta.med.biobank.dialogs.user;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.common.action.security.ManagerContext;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.gui.common.dialogs.BgcWizardPage;
import edu.ualberta.med.biobank.gui.common.widgets.BgcEntryFormWidgetListener;
import edu.ualberta.med.biobank.gui.common.widgets.MultiSelectEvent;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.Domain;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.widgets.multiselect.MultiSelectWidget;

public class MembershipDomainPage extends BgcWizardPage {
    private static final I18n i18n = I18nFactory
        .getI18n(MembershipDomainPage.class);

    @SuppressWarnings("nls")
    // TR: membership domain page title
    private static final String TITLE = i18n.tr("Centers and Studies");

    private final StudiesSelectionHandler studiesSelectionHandler =
        new StudiesSelectionHandler();
    private final CentersSelectionHandler centersSelectionHandler =
        new CentersSelectionHandler();
    private final Membership membership;
    private final Domain domain;
    private final ManagerContext context;

    private Button allCentersButton;
    private Button allStudiesButton;
    private MultiSelectWidget<Center> centersWidget;
    private MultiSelectWidget<Study> studiesWidget;

    private final WritableValue validCenters = new WritableValue(Boolean.FALSE,
        Boolean.class);
    private final WritableValue validStudies = new WritableValue(Boolean.FALSE,
        Boolean.class);

    @SuppressWarnings("nls")
    MembershipDomainPage(Membership membership, ManagerContext context) {
        super(StringUtil.EMPTY_STRING, TITLE, null);

        // TR: membership domain page title area message
        setMessage(i18n.tr("Where the user (or group) is allowed access"));

        this.membership = membership;
        this.domain = membership.getDomain();

        this.context = context;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        updatePageComplete();
    }

    @SuppressWarnings("nls")
    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Group centersGroup = createGroup(container,
            Center.NAME.plural().toString());
        createAllCentersButton(centersGroup);
        createCentersWidget(centersGroup);
        createCentersValidation(
        // TR: validation message if no center is selected
        i18n.tr("Select at least one center"));

        Group studiesGroup =
            createGroup(container, Study.NAME.plural().toString());
        createAllStudiesButton(studiesGroup);
        createStudiesWidget(studiesGroup);
        createStudiesValidation(
        // TR: validation message if no study is selected
        i18n.tr("Select at least one study"));

        centersWidget.addSelectionChangedListener(centersSelectionHandler);
        studiesWidget.addSelectionChangedListener(studiesSelectionHandler);

        updateAllCentersButton();
        updateAllStudiesButton();

        setControl(container);
    }

    private void createCentersValidation(String message) {
        WritableValue dummy = new WritableValue(Boolean.FALSE, Boolean.class);
        getWidgetCreator().addBooleanBinding(dummy, validCenters, message);
    }

    private void createStudiesValidation(String message) {
        WritableValue dummy = new WritableValue(Boolean.FALSE, Boolean.class);
        getWidgetCreator().addBooleanBinding(dummy, validStudies, message);
    }

    private Group createGroup(Composite parent, String title) {
        Group group = new Group(parent, SWT.SHADOW_IN);
        group.setText(title);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return group;
    }

    @SuppressWarnings("nls")
    private void createAllCentersButton(Composite parent) {
        allCentersButton = new Button(parent, SWT.CHECK);
        allCentersButton.setText(
            Domain.PropertyName.ALL_CENTERS.toString());
        allCentersButton.setToolTipText(
            // TR: all centers checkbox tooltip
            i18n.tr("Allow access to all current and future centers"));
        allCentersButton.setSelection(domain.isAllCenters());

        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, true);
        allCentersButton.setLayoutData(gd);

        allCentersButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean allCenters = allCentersButton.getSelection();
                domain.setAllCenters(allCenters);
                centersWidget.setEnabled(!allCenters);
                updateCenterSelections();
                updateAllStudiesButton();
                updatePageComplete();
            }
        });
    }

    @SuppressWarnings("nls")
    private void createCentersWidget(Composite parent) {
        centersWidget = new MultiSelectWidget<Center>(parent, SWT.NONE,
            // label for centers that can be selected from a multi-combo box
            i18n.tr("Available Centers"),
            // label for centers that have be selected from a multi-combo box
            i18n.tr("Selected Centers"),
            100) {
            @Override
            protected String getTextForObject(Center center) {
                return center.getNameShort();
            }
        };

        // TODO: move to updateCenterSelections()?
        centersWidget.setEnabled(!domain.isAllCenters());
    }

    @SuppressWarnings("nls")
    private void createAllStudiesButton(Composite parent) {
        allStudiesButton = new Button(parent, SWT.CHECK);
        allStudiesButton.setText(Domain.PropertyName.ALL_STUDIES.toString());
        allStudiesButton.setToolTipText(
            // TR: all studies checkbox tooltip
            i18n.tr("Allow access to all current and future studies"));
        allStudiesButton.setSelection(domain.isAllStudies());

        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, true);
        allStudiesButton.setLayoutData(gd);

        allStudiesButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean allStudies = allStudiesButton.getSelection();
                domain.setAllStudies(allStudies);
                studiesWidget.setEnabled(!allStudies);
                updateStudySelections();
                updateAllCentersButton();
                updatePageComplete();
            }
        });
    }

    @SuppressWarnings("nls")
    private void createStudiesWidget(Composite parent) {
        studiesWidget = new MultiSelectWidget<Study>(parent, SWT.NONE,
            // label for Studies that can be selected from a multi-combo box
            i18n.tr("Available Studies"),
            // label for Studies that have be selected from a multi-combo box
            i18n.tr("Selected Studies"),
            100) {
            @Override
            protected String getTextForObject(Study study) {
                return study.getNameShort();
            }
        };

        // TODO: move to updateStudySelections()?
        studiesWidget.setEnabled(!domain.isAllStudies());
    }

    private void updateAllCentersButton() {
        boolean allowed = isAllowedAllCentersButton();

        if (!allowed) allCentersButton.setSelection(false);
        allCentersButton.setEnabled(allowed);

        updateCenterSelections();
    }

    private void updateAllStudiesButton() {
        boolean allowed = isAllowedAllStudiesButton();

        if (!allowed) allStudiesButton.setSelection(false);
        allStudiesButton.setEnabled(allowed);

        updateStudySelections();
    }

    private boolean isAllowedAllCentersButton() {
        for (Domain d : context.getManager().getManageableDomains()) {
            if (d.containsAllStudies(domain)) {
                if (d.isAllCenters()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAllowedAllStudiesButton() {
        for (Domain d : context.getManager().getManageableDomains()) {
            if (d.containsAllCenters(domain)) {
                if (d.isAllStudies()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updatePageComplete() {
        boolean centersSelected =
            domain.isAllCenters() || !domain.getCenters().isEmpty();
        validCenters.setValue(centersSelected);

        boolean studiesSelected =
            domain.isAllStudies() || !domain.getStudies().isEmpty();
        validStudies.setValue(studiesSelected);

        setPageComplete(centersSelected && studiesSelected);
    }

    private void updateCenterSelections() {
        centersWidget.setSelections(
            getCenterOptions(),
            membership.getDomain().getCenters());
    }

    private void updateStudySelections() {
        studiesWidget.setSelections(
            getStudyOptions(),
            membership.getDomain().getStudies());
    }

    private Set<Center> getCenterOptions() {
        Set<Center> options = new HashSet<Center>();
        for (Domain d : context.getManager().getManageableDomains()) {
            if (d.containsAllStudies(domain)) {
                if (d.isAllCenters()) {
                    options.addAll(context.getCenters());
                    break;
                }
                options.addAll(d.getCenters());
            }
        }
        return options;
    }

    private Set<Study> getStudyOptions() {
        Set<Study> options = new HashSet<Study>();
        for (Domain d : context.getManager().getManageableDomains()) {
            if (d.containsAllCenters(domain)) {
                if (d.isAllStudies()) {
                    options.addAll(context.getStudies());
                    break;
                }
                options.addAll(d.getStudies());
            }
        }
        return options;
    }

    private class CentersSelectionHandler implements BgcEntryFormWidgetListener {
        @Override
        public void selectionChanged(MultiSelectEvent event) {
            Set<Center> domainCenters = domain.getCenters();
            domainCenters.clear();
            domainCenters.addAll(centersWidget.getSelected());
            updateAllStudiesButton();
            updatePageComplete();
        }
    }

    private class StudiesSelectionHandler implements BgcEntryFormWidgetListener {
        @Override
        public void selectionChanged(MultiSelectEvent event) {
            Set<Study> domainStudies = domain.getStudies();
            domainStudies.clear();
            domainStudies.addAll(studiesWidget.getSelected());
            updateAllCentersButton();
            updatePageComplete();
        }
    }
}
