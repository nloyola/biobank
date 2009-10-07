package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.model.Address;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.SampleType;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.Study;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class SiteWrapper extends ModelWrapper<Site> implements
    Comparable<SiteWrapper> {

    private AddressWrapper addressWrapper;

    public SiteWrapper(WritableApplicationService appService, Site wrappedObject) {
        super(appService, wrappedObject);
        Address address = wrappedObject.getAddress();
        if (address == null) {
            address = new Address();
            wrappedObject.setAddress(address);
        }
        addressWrapper = new AddressWrapper(appService, address);
    }

    public SiteWrapper(WritableApplicationService appService) {
        super(appService);
    }

    @Override
    protected String[] getPropertyChangesNames() {
        return new String[] { "name", "activityStatus", "comment",
            "clinicCollection", "siteCollection", "containerCollection",
            "sampleTypeCollection" };
    }

    public AddressWrapper getAddressWrapper() {
        return addressWrapper;
    }

    public void setAddressWrapper(AddressWrapper addressWrapper) {
        this.addressWrapper = addressWrapper;
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public void setName(String name) {
        String oldName = getName();
        wrappedObject.setName(name);
        propertyChangeSupport.firePropertyChange("name", oldName, name);
    }

    public String getActivityStatus() {
        return wrappedObject.getActivityStatus();
    }

    public void setActivityStatus(String activityStatus) {
        String oldStatus = getActivityStatus();
        wrappedObject.setActivityStatus(activityStatus);
        propertyChangeSupport.firePropertyChange("activityStatus", oldStatus,
            activityStatus);
    }

    public String getComment() {
        return wrappedObject.getComment();
    }

    public void setComment(String comment) {
        String oldComment = getComment();
        wrappedObject.setComment(comment);
        propertyChangeSupport
            .firePropertyChange("comment", oldComment, comment);
    }

    @Override
    protected void persistChecks() throws BiobankCheckException, Exception {
        if (!checkSiteNameUnique()) {
            throw new BiobankCheckException("A site with name \"" + getName()
                + "\" already exists.");
        }
    }

    private boolean checkSiteNameUnique() throws ApplicationException {
        HQLCriteria c;

        if (getWrappedObject().getId() == null) {
            c = new HQLCriteria("from " + Site.class.getName()
                + " where name = ?", Arrays.asList(new Object[] { getName() }));
        } else {
            c = new HQLCriteria("from " + Site.class.getName()
                + " as site where site <> ? and name = ?", Arrays
                .asList(new Object[] { getWrappedObject(), getName() }));
        }

        List<Object> results = appService.query(c);
        return (results.size() == 0);
    }

    @Override
    public Class<Site> getWrappedClass() {
        return Site.class;
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException, Exception {
        // TODO Auto-generated method stub
    }

    public int compareTo(SiteWrapper wrapper) {
        String myName = wrappedObject.getName();
        String wrapperName = wrapper.wrappedObject.getName();
        return ((myName.compareTo(wrapperName) > 0) ? 1 : (myName
            .equals(wrapperName) ? 0 : -1));
    }

    @SuppressWarnings("unchecked")
    public List<StudyWrapper> getStudyCollection(boolean sort) {
        List<StudyWrapper> clinicCollection = (List<StudyWrapper>) propertiesMap
            .get("studyCollection");
        if (clinicCollection == null) {
            Collection<Study> children = wrappedObject.getStudyCollection();
            if (children != null) {
                clinicCollection = new ArrayList<StudyWrapper>();
                for (Study study : children) {
                    clinicCollection.add(new StudyWrapper(appService, study));
                }
                propertiesMap.put("studyCollection", clinicCollection);
            }
        }
        if ((clinicCollection != null) && sort)
            Collections.sort(clinicCollection);
        return clinicCollection;
    }

    public Collection<StudyWrapper> getStudyCollection() {
        return getStudyCollection(false);
    }

    @SuppressWarnings("unchecked")
    public List<ClinicWrapper> getClinicCollection(boolean sort) {
        List<ClinicWrapper> clinicCollection = (List<ClinicWrapper>) propertiesMap
            .get("clinicCollection");
        if (clinicCollection == null) {
            Collection<Clinic> children = wrappedObject.getClinicCollection();
            if (children != null) {
                clinicCollection = new ArrayList<ClinicWrapper>();
                for (Clinic clinic : children) {
                    clinicCollection.add(new ClinicWrapper(appService, clinic));
                }
                propertiesMap.put("clinicCollection", clinicCollection);
            }
        }
        if ((clinicCollection != null) && sort)
            Collections.sort(clinicCollection);
        return clinicCollection;
    }

    public List<ClinicWrapper> getClinicCollection() {
        return getClinicCollection(false);
    }

    @SuppressWarnings("unchecked")
    public List<ContainerTypeWrapper> getContainerTypeCollection(boolean sort) {
        List<ContainerTypeWrapper> containerTypeCollection = (List<ContainerTypeWrapper>) propertiesMap
            .get("containerTypeCollection");
        if (containerTypeCollection == null) {
            Collection<ContainerType> children = wrappedObject
                .getContainerTypeCollection();
            if (children != null) {
                containerTypeCollection = new ArrayList<ContainerTypeWrapper>();
                for (ContainerType type : children) {
                    containerTypeCollection.add(new ContainerTypeWrapper(
                        appService, type));
                }
                propertiesMap.put("containerTypeCollection",
                    containerTypeCollection);
            }
        }
        if ((containerTypeCollection != null) && sort)
            Collections.sort(containerTypeCollection);
        return containerTypeCollection;
    }

    public List<ContainerTypeWrapper> getContainerTypeCollection() {
        return getContainerTypeCollection(false);
    }

    public void setContainerTypeCollection(Collection<ContainerType> types,
        boolean setNull) {
        Collection<ContainerType> oldTypes = wrappedObject
            .getContainerTypeCollection();
        wrappedObject.setContainerTypeCollection(types);
        propertyChangeSupport.firePropertyChange("containerTypeCollection",
            oldTypes, types);
        if (setNull) {
            propertiesMap.put("containerTypeCollection", null);
        }
    }

    public void setContainerTypeCollection(List<ContainerTypeWrapper> types) {
        Collection<ContainerType> typeObjects = new HashSet<ContainerType>();
        for (ContainerTypeWrapper type : types) {
            typeObjects.add(type.getWrappedObject());
        }
        setContainerTypeCollection(typeObjects, false);
        propertiesMap.put("containerTypeCollection", types);
    }

    @SuppressWarnings("unchecked")
    public List<ContainerWrapper> getContainerCollection() {
        List<ContainerWrapper> containerCollection = (List<ContainerWrapper>) propertiesMap
            .get("containerCollection");
        if (containerCollection == null) {
            Collection<Container> children = wrappedObject
                .getContainerCollection();
            if (children != null) {
                containerCollection = new ArrayList<ContainerWrapper>();
                for (Container container : children) {
                    containerCollection.add(new ContainerWrapper(appService,
                        container));
                }
                propertiesMap.put("containerCollection", containerCollection);
            }
        }
        return containerCollection;
    }

    public void setContainerCollection(Collection<Container> containers,
        boolean setNull) {
        Collection<Container> oldContainers = wrappedObject
            .getContainerCollection();
        wrappedObject.setContainerCollection(containers);
        propertyChangeSupport.firePropertyChange("containerCollection",
            oldContainers, containers);
        if (setNull) {
            propertiesMap.put("containerCollection", null);
        }
    }

    public void setContainerCollection(List<ContainerWrapper> containers) {
        Collection<Container> containerObjects = new HashSet<Container>();
        for (ContainerWrapper container : containers) {
            containerObjects.add(container.getWrappedObject());
        }
        setContainerCollection(containerObjects, false);
        propertiesMap.put("containerCollection", containers);
    }

    @SuppressWarnings("unchecked")
    public List<ContainerWrapper> getTopContainerCollection(boolean sort)
        throws Exception {
        List<ContainerWrapper> topContainerCollection = (List<ContainerWrapper>) propertiesMap
            .get("topContainerCollection");

        if (topContainerCollection == null) {
            topContainerCollection = new ArrayList<ContainerWrapper>();
            HQLCriteria criteria = new HQLCriteria("from "
                + Container.class.getName()
                + " where site.id = ? and position is null", Arrays
                .asList(new Object[] { wrappedObject.getId() }));
            List<Container> containers = appService.query(criteria);
            for (Container c : containers) {
                topContainerCollection.add(new ContainerWrapper(appService, c));
            }
            if (sort)
                Collections.sort(topContainerCollection);
            propertiesMap.put("topContainerCollection", topContainerCollection);
        }
        return topContainerCollection;
    }

    public List<ContainerWrapper> getTopContainerCollection() throws Exception {
        return getTopContainerCollection(false);
    }

    public void clearTopContainerCollection() {
        propertiesMap.put("topContainerCollection", null);
    }

    @SuppressWarnings("unchecked")
    public List<SampleTypeWrapper> getSampleTypeCollection(boolean sort) {
        List<SampleTypeWrapper> sampleTypeCollection = (List<SampleTypeWrapper>) propertiesMap
            .get("sampleTypeCollection");
        if (sampleTypeCollection == null) {
            Collection<SampleType> children = wrappedObject
                .getSampleTypeCollection();
            if (children != null) {
                sampleTypeCollection = new ArrayList<SampleTypeWrapper>();
                for (SampleType type : children) {
                    sampleTypeCollection.add(new SampleTypeWrapper(appService,
                        type));
                }
                propertiesMap.put("sampleTypeCollection", sampleTypeCollection);
            }
        }
        if ((sampleTypeCollection != null) && sort)
            Collections.sort(sampleTypeCollection);
        return sampleTypeCollection;
    }

    public List<SampleTypeWrapper> getSampleTypeCollection() {
        return getSampleTypeCollection(false);
    }

    public void setSampleTypeCollection(Collection<SampleType> types,
        boolean setNull) {
        Collection<SampleType> oldTypes = wrappedObject
            .getSampleTypeCollection();
        wrappedObject.setSampleTypeCollection(types);
        propertyChangeSupport.firePropertyChange("sampleTypeCollection",
            oldTypes, types);
        if (setNull) {
            propertiesMap.put("sampleTypeCollection", null);
        }
    }

    public void setSampleTypeCollection(List<SampleTypeWrapper> types)
        throws Exception {
        deleteSampleTypeDifference(types);
        Collection<SampleType> typeObjects = new HashSet<SampleType>();
        for (SampleTypeWrapper type : types) {
            typeObjects.add(type.getWrappedObject());
        }
        setSampleTypeCollection(typeObjects, false);
        propertiesMap.put("sampleTypeCollection", types);
    }

    /**
     * Removes the sample type objects that are not contained in the collection.
     * 
     * @param newCollection
     * @throws Exception
     */
    private void deleteSampleTypeDifference(
        List<SampleTypeWrapper> newCollection) throws Exception {
        // no need to remove if study is not yet in the database or nothing in
        // the collection
        if (isNew() || (newCollection.size() == 0))
            return;

        List<SampleTypeWrapper> currSamplesStorage = getSampleTypeCollection();
        if (currSamplesStorage.size() == 0)
            return;

        List<Integer> idList = new ArrayList<Integer>();
        for (SampleTypeWrapper ss : newCollection) {
            idList.add(ss.getId());
        }
        Iterator<SampleTypeWrapper> it = currSamplesStorage.iterator();
        while (it.hasNext()) {
            if (!idList.contains(it.next().getId())) {
                it.next().delete();
            }
        }
    }

}
