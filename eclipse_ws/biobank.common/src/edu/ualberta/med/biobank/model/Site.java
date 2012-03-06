package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import edu.ualberta.med.biobank.validator.constraint.Empty;
import edu.ualberta.med.biobank.validator.group.PreDelete;

@Entity
@DiscriminatorValue("Site")
@Empty.List({
    @Empty(property = "containers", groups = PreDelete.class),
    @Empty(property = "containerTypes", groups = PreDelete.class),
    @Empty(property = "processingEvents", groups = PreDelete.class)
})
public class Site extends Center {
    private static final long serialVersionUID = 1L;

    private Set<Study> studies = new HashSet<Study>(0);
    private Set<ContainerType> containerTypes = new HashSet<ContainerType>(0);
    private Set<Container> containers = new HashSet<Container>(
        0);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "SITE_STUDY",
        joinColumns = { @JoinColumn(name = "SITE_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "STUDY_ID", nullable = false, updatable = false) })
    public Set<Study> getStudies() {
        return this.studies;
    }

    public void setStudies(Set<Study> studies) {
        this.studies = studies;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "site")
    public Set<ContainerType> getContainerTypes() {
        return this.containerTypes;
    }

    public void setContainerTypes(Set<ContainerType> containerTypes) {
        this.containerTypes = containerTypes;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "site")
    public Set<Container> getContainers() {
        return this.containers;
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

}
