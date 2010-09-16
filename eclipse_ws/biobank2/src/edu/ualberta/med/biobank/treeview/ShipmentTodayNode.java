package edu.ualberta.med.biobank.treeview;

import java.util.List;

import org.eclipse.core.runtime.Assert;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ClinicShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ShipmentTodayNode extends AbstractTodayNode {

    public ShipmentTodayNode(AdapterBase parent, int id) {
        super(parent, id);
        setName("Today's shipments");
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        Assert.isTrue(child instanceof ClinicWrapper);
        return new ClinicAdapter(this, (ClinicWrapper) child);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new ClinicAdapter(this, null);
    }

    @Override
    protected List<? extends ModelWrapper<?>> getTodayElements()
        throws ApplicationException {
        return ClinicShipmentWrapper.getTodayShipments(SessionManager
            .getAppService(), SessionManager.getInstance().getCurrentSite());

    }

    @Override
    protected boolean isParentTo(ModelWrapper<?> parent, ModelWrapper<?> child) {
        if (child instanceof ClinicShipmentWrapper) {
            return parent.equals(((ClinicShipmentWrapper) child).getClinic());
        }
        return false;
    }

}
