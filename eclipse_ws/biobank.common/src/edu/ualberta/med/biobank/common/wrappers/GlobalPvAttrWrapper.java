package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.peer.GlobalPvAttrPeer;
import edu.ualberta.med.biobank.common.wrappers.internal.PvAttrTypeWrapper;
import edu.ualberta.med.biobank.model.GlobalPvAttr;
import edu.ualberta.med.biobank.model.PvAttrType;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class GlobalPvAttrWrapper extends ModelWrapper<GlobalPvAttr> {

    private PvAttrTypeWrapper pvAttrType;

    public GlobalPvAttrWrapper(WritableApplicationService appService,
        GlobalPvAttr wrappedObject) {
        super(appService, wrappedObject);
    }

    public GlobalPvAttrWrapper(WritableApplicationService appService) {
        super(appService);
    }

    @Override
    protected List<String> getPropertyChangeNames() {
        return GlobalPvAttrPeer.PROP_NAMES;
    }

    @Override
    public Class<GlobalPvAttr> getWrappedClass() {
        return GlobalPvAttr.class;
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException,
        ApplicationException {
        // FIXME if used by any study then it cannot be deleted
    }

    public String getLabel() {
        return wrappedObject.getLabel();
    }

    public void setLabel(String label) {
        String oldLabel = getLabel();
        wrappedObject.setLabel(label);
        propertyChangeSupport.firePropertyChange("label", oldLabel, label);
    }

    public PvAttrTypeWrapper getPvAttrType() {
        if (pvAttrType == null) {
            PvAttrType p = wrappedObject.getPvAttrType();
            if (p == null) {
                return null;
            }
            pvAttrType = new PvAttrTypeWrapper(appService, p);
        }
        return pvAttrType;
    }

    public void setPvAttrType(PvAttrTypeWrapper pvAttrType) {
        this.pvAttrType = pvAttrType;
        PvAttrType oldPvInfo = wrappedObject.getPvAttrType();
        PvAttrType newPvInfo = null;
        if (pvAttrType != null) {
            newPvInfo = pvAttrType.getWrappedObject();
        }
        wrappedObject.setPvAttrType(newPvInfo);
        propertyChangeSupport.firePropertyChange("pvAttrType", oldPvInfo,
            newPvInfo);
    }

    public String getTypeName() {
        return wrappedObject.getPvAttrType().getName();
    }

    @Override
    public int compareTo(ModelWrapper<GlobalPvAttr> o) {
        return 0;
    }

    @Override
    public String toString() {
        return "" + getId() + ":" + getLabel() + ":"
            + getPvAttrType().getName();
    }

    @Override
    public void reload() throws Exception {
        super.reload();
        pvAttrType = null;
    }

    public static List<GlobalPvAttrWrapper> getAllGlobalPvAttrs(
        WritableApplicationService appService) throws ApplicationException {

        List<GlobalPvAttrWrapper> pvAttrs = new ArrayList<GlobalPvAttrWrapper>();

        HQLCriteria c = new HQLCriteria("from " + GlobalPvAttr.class.getName());
        List<GlobalPvAttr> result = appService.query(c);
        for (GlobalPvAttr pvAttr : result) {
            pvAttrs.add(new GlobalPvAttrWrapper(appService, pvAttr));
        }

        Collections.sort(pvAttrs);
        return pvAttrs;
    }

}