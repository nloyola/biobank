package edu.ualberta.med.biobank.treeview;

import java.util.Date;

import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.ClinicShipmentWrapper;

public class ShipmentViewNodeSearchVisitor extends NodeSearchVisitor {

    private Date date;

    public ShipmentViewNodeSearchVisitor(ModelWrapper<?> wrapper) {
        super(wrapper);
    }

    public ShipmentViewNodeSearchVisitor(Date date) {
        super(null);
        this.date = date;
    }

    @Override
    public AdapterBase visit(AbstractTodayNode todayNode) {
        if (wrapper instanceof ClinicWrapper) {
            return todayNode.getChild(wrapper, true);
        }
        return visitChildren(todayNode);
    }

    @Override
    public AdapterBase visit(AbstractSearchedNode searchedNode) {
        if (wrapper == null) {
            return searchedNode.getChild((int) date.getTime());
        } else if (wrapper instanceof ClinicWrapper) {
            return searchedNode.getChild(wrapper, true);
        }
        return visitChildren(searchedNode);
    }

    public AdapterBase visit(DateNode dateNode) {
        if (wrapper instanceof ClinicWrapper) {
            return dateNode.getChild(wrapper, true);
        }
        return visitChildren(dateNode);
    }

    @Override
    public AdapterBase visit(ClinicAdapter clinicAdapter) {
        if (wrapper instanceof ClinicShipmentWrapper) {
            return clinicAdapter.getChild(wrapper, true);
        }
        return null;
    }
}