package edu.ualberta.med.biobank.common.wrappers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.AliquotPosition;
import edu.ualberta.med.biobank.model.Container;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class AliquotPositionWrapper extends
    AbstractPositionWrapper<AliquotPosition> {

    public AliquotPositionWrapper(WritableApplicationService appService,
        AliquotPosition wrappedObject) {
        super(appService, wrappedObject);
    }

    public AliquotPositionWrapper(WritableApplicationService appService) {
        super(appService);
    }

    @Override
    protected String[] getPropertyChangeNames() {
        List<String> properties = new ArrayList<String>(Arrays.asList(super
            .getPropertyChangeNames()));
        properties.add("aliquot");
        properties.add("container");
        return properties.toArray(new String[properties.size()]);
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException,
        ApplicationException {

    }

    @Override
    public Class<AliquotPosition> getWrappedClass() {
        return AliquotPosition.class;
    }

    public void setAliquot(Aliquot aliquot) {
        Aliquot oldAliquot = wrappedObject.getAliquot();
        wrappedObject.setAliquot(aliquot);
        propertyChangeSupport
            .firePropertyChange("aliquot", oldAliquot, aliquot);
    }

    public void setAliquot(AliquotWrapper aliquot) {
        setAliquot(aliquot.getWrappedObject());
    }

    public AliquotWrapper getAliquot() {
        Aliquot aliquot = wrappedObject.getAliquot();
        if (aliquot == null) {
            return null;
        }
        return new AliquotWrapper(appService, aliquot);
    }

    private void setContainer(Container container) {
        Container oldContainer = wrappedObject.getContainer();
        wrappedObject.setContainer(container);
        propertyChangeSupport.firePropertyChange("container", oldContainer,
            container);
    }

    private void setContainer(ContainerWrapper container) {
        if (container == null) {
            setContainer((Container) null);
        } else {
            setContainer(container.getWrappedObject());
        }
    }

    private ContainerWrapper getContainer() {
        Container container = wrappedObject.getContainer();
        if (container == null) {
            return null;
        }
        return new ContainerWrapper(appService, container);
    }

    @Override
    public int compareTo(ModelWrapper<AliquotPosition> o) {
        return 0;
    }

    @Override
    public ContainerWrapper getParent() {
        return getContainer();
    }

    @Override
    public void setParent(ContainerWrapper parent) {
        setContainer(parent);
    }

    @Override
    protected void checkObjectAtPosition() throws BiobankCheckException,
        ApplicationException {
        ContainerWrapper parent = getParent();
        if (parent != null) {
            // do a hql query because parent might need a reload - but if we are
            // in the middle of parent.persist, don't want to do that !
            HQLCriteria criteria = new HQLCriteria("from "
                + AliquotPosition.class.getName()
                + " where container.id=? and row=? and col=?", Arrays
                .asList(new Object[] { parent.getId(), getRow(), getCol() }));
            List<AliquotPosition> positions = appService.query(criteria);
            if (positions.size() == 0) {
                return;
            }
            AliquotPositionWrapper aliquotPosition = new AliquotPositionWrapper(
                appService, positions.get(0));
            if (!aliquotPosition.getAliquot().equals(getAliquot())) {
                throw new BiobankCheckException("Position " + getRow() + ":"
                    + getCol() + " in container " + getParent().toString()
                    + " is not available.");
            }
        }

    }

}