package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import edu.ualberta.med.biobank.validator.constraint.Unique;
import edu.ualberta.med.biobank.validator.group.PrePersist;

@Entity
@Table(name = "ROLE")
@Unique(properties = "name", groups = PrePersist.class)
// TODO: check that no Membership uses this role before deleting
public class Role extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private String name;
    private Set<PermissionEnum> permissions = new HashSet<PermissionEnum>(0);

    @NotEmpty(message = "{edu.ualberta.med.biobank.model.Role.name.NotEmpty}")
    @Column(name = "NAME", unique = true, nullable = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ElementCollection(targetClass = PermissionEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "ROLE_PERMISSION",
        joinColumns = @JoinColumn(name = "ID"))
    @Column(name = "PERMISSION_ID", nullable = false)
    @Type(type = "permissionEnum")
    public Set<PermissionEnum> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<PermissionEnum> permissions) {
        this.permissions = permissions;
    }
}
