package edu.ualberta.med.biobank.common.wrappers;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.SampleStorage;
import edu.ualberta.med.biobank.model.SampleType;
import edu.ualberta.med.biobank.model.Study;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class SampleStorageWrapper extends ModelWrapper<SampleStorage> {

    public SampleStorageWrapper(WritableApplicationService appService,
        SampleStorage wrappedObject) {
        super(appService, wrappedObject);
    }

    public SampleStorageWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public StudyWrapper getStudy() {
        Study study = wrappedObject.getStudy();
        if (study == null) {
            return null;
        }
        return new StudyWrapper(appService, study);
    }

    public void setStudy(StudyWrapper study) {
        Study oldStudy = wrappedObject.getStudy();
        Study newStudy = study.wrappedObject;
        wrappedObject.setStudy(newStudy);
        propertyChangeSupport.firePropertyChange("study", oldStudy, newStudy);
    }

    public SampleTypeWrapper getSampleType() {
        SampleType type = wrappedObject.getSampleType();
        if (type == null) {
            return null;
        }
        return new SampleTypeWrapper(appService, type);
    }

    public void setSampleType(SampleTypeWrapper sampleType) {
        SampleType oldSampleType = wrappedObject.getSampleType();
        SampleType newSampleType = null;
        if (sampleType != null) {
            newSampleType = sampleType.getWrappedObject();
        }
        wrappedObject.setSampleType(newSampleType);
        propertyChangeSupport.firePropertyChange("sampleType", oldSampleType,
            newSampleType);
    }

    public Integer getQuantity() {
        return wrappedObject.getQuantity();
    }

    public void setQuantity(Integer quantity) {
        Integer oldQuantity = getQuantity();
        wrappedObject.setQuantity(quantity);
        propertyChangeSupport.firePropertyChange("quantity", oldQuantity,
            quantity);
    }

    public Double getVolume() {
        return wrappedObject.getVolume();
    }

    public void setVolume(Double volume) {
        Double oldVolume = getVolume();
        wrappedObject.setVolume(volume);
        propertyChangeSupport.firePropertyChange("volume", oldVolume, volume);
    }

    public ActivityStatusWrapper getActivityStatus() {
        ActivityStatus activityStatus = wrappedObject.getActivityStatus();
        if (activityStatus == null)
            return null;
        return new ActivityStatusWrapper(appService, activityStatus);
    }

    public void setActivityStatus(ActivityStatusWrapper activityStatus) {
        ActivityStatus oldActivityStatus = wrappedObject.getActivityStatus();
        ActivityStatus rawObject = null;
        if (activityStatus != null) {
            rawObject = activityStatus.getWrappedObject();
        }
        wrappedObject.setActivityStatus(rawObject);
        propertyChangeSupport.firePropertyChange("activityStatus",
            oldActivityStatus, activityStatus);
    }

    @Override
    protected String[] getPropertyChangeNames() {
        return new String[] { "study", "sampleType", "quantity", "volume",
            "activityStatus" };
    }

    @Override
    public Class<SampleStorage> getWrappedClass() {
        return SampleStorage.class;
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException,
        ApplicationException {
    }

    @Override
    protected void persistChecks() throws BiobankCheckException,
        ApplicationException {
        if (getActivityStatus() == null) {
            throw new BiobankCheckException(
                "the sample storage does not have an activity status");
        }
    }

    @Override
    public int compareTo(ModelWrapper<SampleStorage> wrapper) {
        if (wrapper instanceof SampleStorageWrapper) {
            String name1 = wrappedObject.getSampleType().getName();
            String name2 = wrapper.wrappedObject.getSampleType().getName();
            if (name1 != null && name2 != null) {
                return name1.compareTo(name2);
            }
        }
        return 0;
    }

}