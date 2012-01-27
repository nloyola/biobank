package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.NotNull;

/**
 * OriginInfo generated by hbm2java
 */
@Entity
@Table(name = "ORIGIN_INFO")
public class OriginInfo extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private Collection<Comment> commentCollection = new HashSet<Comment>(0);
    private Collection<Specimen> specimenCollection = new HashSet<Specimen>(0);
    private ShipmentInfo shipmentInfo;
    private Center center;
    private Site receiverSite;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(name = "ORIGIN_INFO_COMMENT",
        joinColumns = { @JoinColumn(name = "ORIGIN_INFO_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", unique = true, nullable = false, updatable = false) })
    public Collection<Comment> getCommentCollection() {
        return this.commentCollection;
    }

    public void setCommentCollection(Collection<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "originInfo")
    @Cascade({ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    public Collection<Specimen> getSpecimenCollection() {
        return this.specimenCollection;
    }

    public void setSpecimenCollection(Collection<Specimen> specimenCollection) {
        this.specimenCollection = specimenCollection;
    }

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "SHIPMENT_INFO_ID", unique = true)
    public ShipmentInfo getShipmentInfo() {
        return this.shipmentInfo;
    }

    public void setShipmentInfo(ShipmentInfo shipmentInfo) {
        this.shipmentInfo = shipmentInfo;
    }

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CENTER_ID", nullable = false)
    public Center getCenter() {
        return this.center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_SITE_ID")
    public Site getReceiverSite() {
        return this.receiverSite;
    }

    public void setReceiverSite(Site receiverSite) {
        this.receiverSite = receiverSite;
    }
}
