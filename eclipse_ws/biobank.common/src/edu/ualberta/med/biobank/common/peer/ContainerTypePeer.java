package edu.ualberta.med.biobank.common.peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.util.TypeReference;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.Capacity;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.ContainerLabelingScheme;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.type.LabelingLayout;

public class ContainerTypePeer {
    public static final Property<Integer, ContainerType> ID = Property.create("id" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Integer>() {
        }
        , new Property.Accessor<Integer, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Integer get(ContainerType model) {
                return model.getId();
            }

            @Override
            public void set(ContainerType model, Integer value) {
                model.setId(value);
            }
        });

    public static final Property<Double, ContainerType> DEFAULT_TEMPERATURE = Property.create("defaultTemperature" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Double>() {
        }
        , new Property.Accessor<Double, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Double get(ContainerType model) {
                return model.getDefaultTemperature();
            }

            @Override
            public void set(ContainerType model, Double value) {
                model.setDefaultTemperature(value);
            }
        });

    public static final Property<String, ContainerType> NAME = Property.create("name" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<String>() {
        }
        , new Property.Accessor<String, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String get(ContainerType model) {
                return model.getName();
            }

            @Override
            public void set(ContainerType model, String value) {
                model.setName(value);
            }
        });

    public static final Property<Boolean, ContainerType> TOP_LEVEL = Property.create("topLevel" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Boolean>() {
        }
        , new Property.Accessor<Boolean, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean get(ContainerType model) {
                return model.getTopLevel();
            }

            @Override
            public void set(ContainerType model, Boolean value) {
                model.setTopLevel(value);
            }
        });

    public static final Property<Boolean, ContainerType> IS_MICROPLATE = Property.create("isMicroplate" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Boolean>() {
        }
        , new Property.Accessor<Boolean, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean get(ContainerType model) {
                return model.getIsMicroplate();
            }

            @Override
            public void set(ContainerType model, Boolean value) {
                model.setIsMicroplate(value);
            }
        });

    public static final Property<String, ContainerType> NAME_SHORT = Property.create("nameShort" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<String>() {
        }
        , new Property.Accessor<String, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String get(ContainerType model) {
                return model.getNameShort();
            }

            @Override
            public void set(ContainerType model, String value) {
                model.setNameShort(value);
            }
        });

    public static final Property<Site, ContainerType> SITE = Property.create("site" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Site>() {
        }
        , new Property.Accessor<Site, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Site get(ContainerType model) {
                return model.getSite();
            }

            @Override
            public void set(ContainerType model, Site value) {
                model.setSite(value);
            }
        });

    public static final Property<ContainerLabelingScheme, ContainerType> CHILD_LABELING_SCHEME = Property.create("childLabelingScheme" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<ContainerLabelingScheme>() {
        }
        , new Property.Accessor<ContainerLabelingScheme, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ContainerLabelingScheme get(ContainerType model) {
                return model.getChildLabelingScheme();
            }

            @Override
            public void set(ContainerType model, ContainerLabelingScheme value) {
                model.setChildLabelingScheme(value);
            }
        });

    public static final Property<Collection<SpecimenType>, ContainerType> SPECIMEN_TYPES = Property.create("specimenTypes" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Collection<SpecimenType>>() {
        }
        , new Property.Accessor<Collection<SpecimenType>, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Collection<SpecimenType> get(ContainerType model) {
                return model.getSpecimenTypes();
            }

            @Override
            public void set(ContainerType model, Collection<SpecimenType> value) {
                model.getSpecimenTypes().clear();
                model.getSpecimenTypes().addAll(value);
            }
        });

    public static final Property<Collection<Comment>, ContainerType> COMMENTS = Property.create("comments" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Collection<Comment>>() {
        }
        , new Property.Accessor<Collection<Comment>, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Collection<Comment> get(ContainerType model) {
                return model.getComments();
            }

            @Override
            public void set(ContainerType model, Collection<Comment> value) {
                model.getComments().clear();
                model.getComments().addAll(value);
            }
        });

    public static final Property<ActivityStatus, ContainerType> ACTIVITY_STATUS = Property.create("activityStatus" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<ActivityStatus>() {
        }
        , new Property.Accessor<ActivityStatus, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ActivityStatus get(ContainerType model) {
                return model.getActivityStatus();
            }

            @Override
            public void set(ContainerType model, ActivityStatus value) {
                model.setActivityStatus(value);
            }
        });

    public static final Property<Capacity, ContainerType> CAPACITY = Property.create("capacity" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Capacity>() {
        }
        , new Property.Accessor<Capacity, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Capacity get(ContainerType model) {
                return model.getCapacity();
            }

            @Override
            public void set(ContainerType model, Capacity value) {
                model.setCapacity(value);
            }
        });

    public static final Property<Collection<ContainerType>, ContainerType> CHILD_CONTAINER_TYPES = Property.create("childContainerTypes" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Collection<ContainerType>>() {
        }
        , new Property.Accessor<Collection<ContainerType>, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Collection<ContainerType> get(ContainerType model) {
                return model.getChildContainerTypes();
            }

            @Override
            public void set(ContainerType model, Collection<ContainerType> value) {
                model.getChildContainerTypes().clear();
                model.getChildContainerTypes().addAll(value);
            }
        });

    public static final Property<Collection<ContainerType>, ContainerType> PARENT_CONTAINER_TYPES = Property.create("parentContainerTypes" //$NON-NLS-1$
    , ContainerType.class
        , new TypeReference<Collection<ContainerType>>() {
        }
        , new Property.Accessor<Collection<ContainerType>, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Collection<ContainerType> get(ContainerType model) {
                return model.getParentContainerTypes();
            }

            @Override
            public void set(ContainerType model, Collection<ContainerType> value) {
                model.getParentContainerTypes().clear();
                model.getParentContainerTypes().addAll(value);
            }
        });

    @SuppressWarnings("nls")
    public static final Property<LabelingLayout, ContainerType> LABELING_LAYOUT = Property.create("labelingLayout"
        , ContainerType.class
        , new TypeReference<LabelingLayout>() {
        }
        , new Property.Accessor<LabelingLayout, ContainerType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public LabelingLayout get(ContainerType model) {
                return model.getLabelingLayout();
            }

            @Override
            public void set(ContainerType model, LabelingLayout value) {
                model.setLabelingLayout(value);
            }
        });

    public static final List<Property<?, ? super ContainerType>> PROPERTIES;
    static {
        List<Property<?, ? super ContainerType>> aList = new ArrayList<Property<?, ? super ContainerType>>();
        aList.add(ID);
        aList.add(DEFAULT_TEMPERATURE);
        aList.add(NAME);
        aList.add(TOP_LEVEL);
        aList.add(IS_MICROPLATE);
        aList.add(NAME_SHORT);
        aList.add(SITE);
        aList.add(CHILD_LABELING_SCHEME);
        aList.add(SPECIMEN_TYPES);
        aList.add(COMMENTS);
        aList.add(ACTIVITY_STATUS);
        aList.add(CAPACITY);
        aList.add(CHILD_CONTAINER_TYPES);
        aList.add(PARENT_CONTAINER_TYPES);
        aList.add(LABELING_LAYOUT);
        PROPERTIES = Collections.unmodifiableList(aList);
    };
}
