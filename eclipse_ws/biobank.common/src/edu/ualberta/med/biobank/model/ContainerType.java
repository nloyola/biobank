package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import edu.ualberta.med.biobank.validator.constraint.NotUsed;
import edu.ualberta.med.biobank.validator.constraint.Unique;
import edu.ualberta.med.biobank.validator.group.PreDelete;
import edu.ualberta.med.biobank.validator.group.PreInsert;
import edu.ualberta.med.biobank.validator.group.PreUpdate;

@Entity
@Table(name = "CONTAINER_TYPE",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "SITE_ID", "NAME" }),
        @UniqueConstraint(columnNames = { "SITE_ID", "NAME_SHORT" }) })
@Unique.List({
    @Unique(properties = { "site.id", "name" },
        groups = { PreInsert.class, PreUpdate.class },
        message = "{edu.ualberta.med.biobank.model.ContainerType.name.Unique}"),
    @Unique(properties = { "site.id", "nameShort" },
        groups = { PreInsert.class, PreUpdate.class },
        message = "{edu.ualberta.med.biobank.model.ContainerType.nameShort.Unique}")
})
@NotUsed.List({
    @NotUsed(by = Container.class, property = "containerType",
        groups = PreDelete.class,
        message = "edu.ualberta.med.biobank.model.ContainerType.NotUsed.container.containerType"),
    @NotUsed(by = ContainerType.class, property = "parentContainerTypeCollection",
        groups = PreDelete.class,
        message = "edu.ualberta.med.biobank.model.ContainerType.NotUsed.containerType.parentContainerTypeCollection"),
    @NotUsed(by = ContainerType.class, property = "childContainerTypeCollection",
        groups = PreDelete.class,
        message = "edu.ualberta.med.biobank.model.ContainerType.NotUsed.containerType.childContainerTypeCollection")
})
public class ContainerType extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private String name;
    private String nameShort;
    private boolean topLevel = false;
    private double defaultTemperature;
    private Set<SpecimenType> specimenTypeCollection =
        new HashSet<SpecimenType>(0);
    private Set<ContainerType> childContainerTypeCollection =
        new HashSet<ContainerType>(0);
    private ActivityStatus activityStatus;
    private Set<Comment> commentCollection = new HashSet<Comment>(0);
    private Capacity capacity;
    private Site site;
    private ContainerLabelingScheme childLabelingScheme;
    private Set<ContainerType> parentContainerTypeCollection =
        new HashSet<ContainerType>(0);

    @NotEmpty(message = "{edu.ualberta.med.biobank.model.ContainerType.name.NotEmpty}")
    @Column(name = "NAME")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty(message = "{edu.ualberta.med.biobank.model.ContainerType.name.NotEmpty}")
    @Column(name = "NAME_SHORT")
    public String getNameShort() {
        return this.nameShort;
    }

    public void setNameShort(String nameShort) {
        this.nameShort = nameShort;
    }

    @Column(name = "TOP_LEVEL")
    // TODO: rename to isTopLevel
    public boolean getTopLevel() {
        return this.topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    // TODO: change to decimal
    @Column(name = "DEFAULT_TEMPERATURE")
    public double getDefaultTemperature() {
        return this.defaultTemperature;
    }

    public void setDefaultTemperature(double defaultTemperature) {
        this.defaultTemperature = defaultTemperature;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "CONTAINER_TYPE_SPECIMEN_TYPE",
        joinColumns = { @JoinColumn(name = "CONTAINER_TYPE_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "SPECIMEN_TYPE_ID", nullable = false, updatable = false) })
    public Set<SpecimenType> getSpecimenTypeCollection() {
        return this.specimenTypeCollection;
    }

    public void setSpecimenTypeCollection(
        Set<SpecimenType> specimenTypeCollection) {
        this.specimenTypeCollection = specimenTypeCollection;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "CONTAINER_TYPE_CONTAINER_TYPE",
        joinColumns = { @JoinColumn(name = "PARENT_CONTAINER_TYPE_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "CHILD_CONTAINER_TYPE_ID", nullable = false, updatable = false) })
    public Set<ContainerType> getChildContainerTypeCollection() {
        return this.childContainerTypeCollection;
    }

    public void setChildContainerTypeCollection(
        Set<ContainerType> childContainerTypeCollection) {
        this.childContainerTypeCollection = childContainerTypeCollection;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ContainerType.activityStatus.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_STATUS_ID", nullable = false)
    public ActivityStatus getActivityStatus() {
        return this.activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "CONTAINER_TYPE_COMMENT",
        joinColumns = { @JoinColumn(name = "CONTAINER_TYPE_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", unique = true, nullable = false, updatable = false) })
    public Set<Comment> getCommentCollection() {
        return this.commentCollection;
    }

    public void setCommentCollection(Set<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ContainerType.capacity.NotNull}")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CAPACITY_ID", unique = true, nullable = false)
    public Capacity getCapacity() {
        return this.capacity;
    }

    public void setCapacity(Capacity capacity) {
        this.capacity = capacity;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ContainerType.site.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SITE_ID", nullable = false)
    public Site getSite() {
        return this.site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ContainerType.childLabelingScheme.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_LABELING_SCHEME_ID", nullable = false)
    public ContainerLabelingScheme getChildLabelingScheme() {
        return this.childLabelingScheme;
    }

    public void setChildLabelingScheme(
        ContainerLabelingScheme childLabelingScheme) {
        this.childLabelingScheme = childLabelingScheme;
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "childContainerTypeCollection")
    public Set<ContainerType> getParentContainerTypeCollection() {
        return this.parentContainerTypeCollection;
    }

    public void setParentContainerTypeCollection(
        Set<ContainerType> parentContainerTypeCollection) {
        this.parentContainerTypeCollection = parentContainerTypeCollection;
    }
}
