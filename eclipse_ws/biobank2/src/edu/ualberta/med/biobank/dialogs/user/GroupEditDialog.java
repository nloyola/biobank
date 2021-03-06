package edu.ualberta.med.biobank.dialogs.user;

import java.util.ArrayList;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.IdResult;
import edu.ualberta.med.biobank.common.action.security.GroupGetOutput;
import edu.ualberta.med.biobank.common.action.security.GroupSaveAction;
import edu.ualberta.med.biobank.common.action.security.GroupSaveInput;
import edu.ualberta.med.biobank.common.action.security.ManagerContext;
import edu.ualberta.med.biobank.common.action.security.MembershipContext;
import edu.ualberta.med.biobank.common.peer.GroupPeer;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.model.Group;
import edu.ualberta.med.biobank.model.HasName;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.widgets.infotables.MembershipInfoTable;
import edu.ualberta.med.biobank.widgets.multiselect.MultiSelectWidget;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class GroupEditDialog extends AbstractSecurityEditDialog {
    private static final I18n i18n = I18nFactory
        .getI18n(GroupEditDialog.class);

    private final String currentTitle;
    private final String titleAreaMessage;

    private final Group group;
    private final MembershipContext membershipContext;
    private final ManagerContext context;

    private MembershipInfoTable membershipInfoTable;
    private MultiSelectWidget<User> usersWidget;

    @SuppressWarnings("nls")
    public GroupEditDialog(Shell parent, GroupGetOutput output,
        ManagerContext context) {
        super(parent);

        this.group = output.getGroup();
        this.membershipContext = output.getContext();
        this.context = context;

        if (group.isNew()) {
            // TR: add group dialog title
            currentTitle = i18n.tr("Add Group");
            // TR: add group dialog message
            titleAreaMessage = i18n.tr("Add a new group");
        } else {
            // TR: edit group dialog title
            currentTitle = i18n.tr("Edit Group");
            // TR: edit group dialog message
            titleAreaMessage =
                i18n.tr("Modify an existing group's information");
        }
    }

    @Override
    protected String getDialogShellTitle() {
        return currentTitle;
    }

    @Override
    protected String getTitleAreaMessage() {
        return titleAreaMessage;
    }

    @Override
    protected String getTitleAreaTitle() {
        return currentTitle;
    }

    @SuppressWarnings("nls")
    @Override
    protected void createDialogAreaInternal(Composite parent)
        throws ApplicationException {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(1, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TabFolder tb = new TabFolder(contents, SWT.TOP);
        tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createGeneralFields(createTabItem(tb,
            i18n.trc("Group Edit Dialog Tab Name", "General"), 2));

        createMembershipsSection(createTabItem(tb,
            i18n.trc("Group Edit Dialog Tab Name", "Roles and Permissions"), 1));

        createUsersSection(createTabItem(tb, User.NAME.plural().toString(), 1));

    }

    @SuppressWarnings("nls")
    private void createGeneralFields(Composite createTabItem) {
        createBoundWidgetWithLabel(createTabItem, BgcBaseText.class,
            SWT.BORDER,
            HasName.PropertyName.NAME.toString(),
            null,
            group, GroupPeer.NAME.getName(),
            new NonEmptyStringValidator(
                // TR: validation message if group name not entered
                i18n.tr("A valid name is required.")));
    }

    private void createMembershipsSection(Composite contents) {
        Button addButton = new Button(contents, SWT.PUSH);
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addMembership();
            }
        });
        addButton.setImage(BgcPlugin.getDefault().getImage(BgcPlugin.Image.ADD));
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        addButton.setLayoutData(gd);

        membershipInfoTable =
            new MembershipInfoTable(contents, group, membershipContext, context);
    }

    @SuppressWarnings("nls")
    private void createUsersSection(Composite contents) {
        usersWidget = new MultiSelectWidget<User>(contents, SWT.NONE,
            // TR: list of available users to choose from
            i18n.tr("Available users"),
            // TR: list of available users chosen
            i18n.tr("Selected users"), 200) {
            @Override
            protected String getTextForObject(User node) {
                return node.getFullName() + " (" + node.getLogin() + ")";
            }
        };

        usersWidget.setSelections(context.getUsers(),
            new ArrayList<User>(group.getUsers()));
    }

    private Composite createTabItem(TabFolder tb, String title, int columns) {
        TabItem item = new TabItem(tb, SWT.NONE);
        item.setText(title);
        Composite contents = new Composite(tb, SWT.NONE);
        contents.setLayout(new GridLayout(columns, false));
        item.setControl(contents);
        return contents;
    }

    protected void addMembership() {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            @Override
            public void run() {
                Membership m = new Membership();
                m.setPrincipal(group);

                Shell shell = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell();

                MembershipEditWizard wiz = new MembershipEditWizard(m, context);
                WizardDialog dlg = new SecurityWizardDialog(shell, wiz);

                int res = dlg.open();
                if (res == Status.OK) {
                    m.setPrincipal(group);
                    group.getMemberships().add(m);

                    membershipInfoTable.setCollection(group.getMemberships());
                    membershipInfoTable.setSelection(m);
                } else {
                    m.setPrincipal(null);
                }
            }
        });
    }

    @Override
    protected void okPressed() {
        // try saving or updating the group inside this dialog so that if there
        // is an error the entered information is not lost
        try {
            group.getUsers().addAll(usersWidget.getAddedToSelection());

            // FIXME: for now it's faster to use the name as the description
            group.setDescription(group.getName());

            IdResult result = SessionManager.getAppService().doAction(
                new GroupSaveAction(new GroupSaveInput(group)));
            group.setId(result.getId());

            close();
        } catch (Throwable t) {
            TmpUtil.displayException(t);
        }
    }
}