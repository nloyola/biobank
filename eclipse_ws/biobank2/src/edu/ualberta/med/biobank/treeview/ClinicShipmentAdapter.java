package edu.ualberta.med.biobank.treeview;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.ClinicShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.forms.ClinicShipmentEntryForm;
import edu.ualberta.med.biobank.forms.ClinicShipmentViewForm;

public class ClinicShipmentAdapter extends AdapterBase {

    public ClinicShipmentAdapter(AdapterBase parent,
        ClinicShipmentWrapper shipment) {
        super(parent, shipment);
        setHasChildren(true);
    }

    public ClinicShipmentWrapper getWrapper() {
        return (ClinicShipmentWrapper) modelObject;
    }

    @Override
    protected String getLabelInternal() {
        ClinicShipmentWrapper shipment = getWrapper();
        Assert.isNotNull(shipment, "shipment is null");
        String label = shipment.getFormattedDateReceived();
        if (shipment.getWaybill() != null) {
            label += " (" + shipment.getWaybill() + ")";
        }
        return label;

    }

    @Override
    public String getTooltipText() {
        ClinicShipmentWrapper shipment = getWrapper();
        ClinicWrapper clinic = shipment.getClinic();
        if (clinic != null) {
            return clinic.getName() + " - " + getTooltipText("Shipment");
        }
        return getTooltipText("Shipment");
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        addEditMenu(menu, "Shipment");
        addViewMenu(menu, "Shipment");
        addDeleteMenu(menu, "Shipment",
            "Are you sure you want to delete this shipment?");
    }

    @Override
    public AdapterBase search(Object searchedObject) {
        if (searchedObject instanceof PatientVisitWrapper) {
            return getChild((ModelWrapper<?>) searchedObject, true);
        }
        return searchChildren(searchedObject);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new PatientVisitAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        Assert.isTrue(child instanceof PatientVisitWrapper);
        return new PatientVisitAdapter(this, (PatientVisitWrapper) child);
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        getWrapper().reload();
        return getWrapper().getPatientVisitCollection();
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return getWrapperChildren().size();
    }

    @Override
    public String getEntryFormId() {
        return ClinicShipmentEntryForm.ID;
    }

    @Override
    public String getViewFormId() {
        return ClinicShipmentViewForm.ID;
    }

}