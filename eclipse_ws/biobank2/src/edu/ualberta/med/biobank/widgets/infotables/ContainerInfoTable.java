package edu.ualberta.med.biobank.widgets.infotables;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.formatters.NumberFormatter;
import edu.ualberta.med.biobank.common.permission.container.ContainerDeletePermission;
import edu.ualberta.med.biobank.common.permission.container.ContainerReadPermission;
import edu.ualberta.med.biobank.common.permission.container.ContainerUpdatePermission;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.gui.common.widgets.AbstractInfoTableWidget;
import edu.ualberta.med.biobank.gui.common.widgets.BgcLabelProvider;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableDoubleClickItemListener;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.HasName;
import edu.ualberta.med.biobank.treeview.admin.ContainerAdapter;
import edu.ualberta.med.biobank.treeview.admin.SiteAdapter;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ContainerInfoTable extends InfoTableWidget<Container> {
    public static final I18n i18n = I18nFactory
        .getI18n(ContainerInfoTable.class);

    private static class TableRowData {
        Container container;
        String label;
        String typeNameShort;
        String status;
        String barcode;
        Double temperature;

        @SuppressWarnings("nls")
        @Override
        public String toString() {
            return StringUtils.join(new String[] {
                label,
                typeNameShort,
                status,
                barcode,
                (temperature != null) ? temperature.toString()
                    : StringUtil.EMPTY_STRING }, "\t");
        }
    }

    @SuppressWarnings("nls")
    private static final String[] HEADINGS = new String[] {
        HasName.PropertyName.NAME.toString(),
        ContainerType.NAME.singular().toString(),
        i18n.tr("Status"),
        Container.PropertyName.PRODUCT_BARCODE.toString(),
        Container.PropertyName.TEMPERATURE.toString() };

    private final SiteAdapter siteAdapter;

    public ContainerInfoTable(Composite parent, SiteAdapter site,
        List<Container> containers) {
        super(parent, containers, HEADINGS, 10, ContainerWrapper.class);
        siteAdapter = site;
    }

    @Override
    protected BgcLabelProvider getLabelProvider() {
        return new BgcLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                TableRowData item =
                    (TableRowData) ((BiobankCollectionModel) element).o;
                if (item == null) {
                    if (columnIndex == 0) {
                        return AbstractInfoTableWidget.LOADING;
                    }
                    return StringUtil.EMPTY_STRING;
                }
                switch (columnIndex) {
                case 0:
                    return item.label;
                case 1:
                    return item.typeNameShort;
                case 2:
                    return item.status;
                case 3:
                    return item.barcode;
                case 4:
                    NumberFormatter.format(item.temperature);
                default:
                    return StringUtil.EMPTY_STRING;
                }
            }
        };
    }

    @Override
    public Object getCollectionModelObject(Object obj) throws Exception {
        TableRowData info = new TableRowData();

        Container container = (Container) obj;

        info.container =
            container;
        info.label = info.container.getLabel();
        ContainerType type = info.container.getContainerType();
        if (type != null) {
            info.typeNameShort = type.getNameShort();
        }
        info.status = info.container.getActivityStatus().getName();
        info.barcode = info.container.getProductBarcode();
        info.temperature = info.container.getTopContainer().getTemperature();
        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public Container getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        return ((TableRowData) item.o).container;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }

    @SuppressWarnings("nls")
    @Override
    public void addClickListener(
        IInfoTableDoubleClickItemListener<Container> listener) {
        doubleClickListeners.add(listener);
        MenuItem mi = new MenuItem(getMenu(), SWT.PUSH);
        mi.setText(i18n.tr("Edit"));
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Container selection = ContainerInfoTable.this
                    .getSelection();
                if (selection != null) {
                    new ContainerAdapter(siteAdapter.getContainersGroupNode(),
                        new ContainerWrapper(SessionManager.getAppService(),
                            selection)).openEntryForm();
                }
            }
        });
    }

    @Override
    protected Boolean canEdit(Container target) throws ApplicationException {
        return SessionManager.getAppService().isAllowed(
            new ContainerUpdatePermission(target.getId()));
    }

    @Override
    protected Boolean canDelete(Container target) throws ApplicationException {
        return SessionManager.getAppService().isAllowed(
            new ContainerDeletePermission(target.getId()));
    }

    @Override
    protected Boolean canView(Container target) throws ApplicationException {
        return SessionManager.getAppService().isAllowed(
            new ContainerReadPermission(target));
    }

}
