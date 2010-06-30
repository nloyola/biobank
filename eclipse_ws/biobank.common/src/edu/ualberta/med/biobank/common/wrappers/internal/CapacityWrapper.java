package edu.ualberta.med.biobank.common.wrappers.internal;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.model.Capacity;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class CapacityWrapper extends ModelWrapper<Capacity> {

    public CapacityWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public CapacityWrapper(WritableApplicationService appService,
        Capacity wrappedObject) {
        super(appService, wrappedObject);
    }

    @Override
    protected String[] getPropertyChangeNames() {
        return new String[] { "rowCapacity", "colCapacity" };
    }

    public void setRow(Integer rowCapacity) {
        Integer oldRowCapacity = wrappedObject.getRowCapacity();
        wrappedObject.setRowCapacity(rowCapacity);
        propertyChangeSupport.firePropertyChange("row", oldRowCapacity,
            rowCapacity);
    }

    public Integer getRowCapacity() {
        return wrappedObject.getRowCapacity();
    }

    public void setCol(Integer colCapacity) {
        Integer oldColCapacity = wrappedObject.getColCapacity();
        wrappedObject.setColCapacity(colCapacity);
        propertyChangeSupport.firePropertyChange("col", oldColCapacity,
            colCapacity);
    }

    public Integer getColCapacity() {
        return wrappedObject.getColCapacity();
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException,
        ApplicationException {
        // do nothing
    }

    @Override
    public Class<Capacity> getWrappedClass() {
        return Capacity.class;
    }

    @Override
    public void persistChecks() throws BiobankCheckException,
        ApplicationException {
        if ((getRowCapacity() == null) || (getColCapacity() == null)
            || (getRowCapacity() < 0) || (getColCapacity() < 0)) {
            throw new BiobankCheckException("Capacity '" + getRowCapacity()
                + ":" + getColCapacity() + "' is invalid");
        }
    }

    @Override
    public int compareTo(ModelWrapper<Capacity> cap) {
        if (cap instanceof CapacityWrapper) {
            Integer rowCapacity1 = wrappedObject.getRowCapacity();
            Integer rowCapacity2 = cap.getWrappedObject().getRowCapacity();
            int compare = rowCapacity1.compareTo(rowCapacity2);
            if (compare == 0) {
                Integer colCapacity1 = wrappedObject.getColCapacity();
                Integer colCapacity2 = cap.getWrappedObject().getColCapacity();

                return ((colCapacity1.compareTo(colCapacity2) > 0) ? 1
                    : (colCapacity1.equals(colCapacity2) ? 0 : -1));

            }
            return (compare > 0) ? 1 : -1;
        }
        return 0;
    }

}
