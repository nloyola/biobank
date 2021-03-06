package edu.ualberta.med.biobank.common.action.scanprocess.data;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.peer.ContainerPeer;
import edu.ualberta.med.biobank.common.peer.SpecimenPeer;
import edu.ualberta.med.biobank.common.peer.SpecimenPositionPeer;
import edu.ualberta.med.biobank.common.util.NotAProxy;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.Specimen;

public abstract class AbstractProcessPalletInfo implements Serializable,
    NotAProxy {

    private static final long serialVersionUID = 1L;

    protected Integer palletId;

    // FIXME initialized to a new container if new container?
    private Container pallet;

    public AbstractProcessPalletInfo(Integer palletId) {
        this.palletId = palletId;
    }

    public Container getPallet(Session session) {
        if (palletId != null && pallet == null) {
            Criteria criteria = session.createCriteria(Container.class).add(
                Restrictions.idEq(palletId));
            pallet = (Container) criteria.list().get(0);
        }
        return pallet;
    }

    private static final String SPECIMEN_SEARCH_QRY = "select spec from " //$NON-NLS-1$
        + Specimen.class.getName() + " as spec" //$NON-NLS-1$
        + " left join spec." + SpecimenPeer.SPECIMEN_POSITION.getName() //$NON-NLS-1$
        + " as pos " + " where pos." + SpecimenPositionPeer.ROW.getName() //$NON-NLS-1$ //$NON-NLS-2$
        + "= :row" + " and pos." + SpecimenPositionPeer.COL.getName() //$NON-NLS-1$ //$NON-NLS-2$
        + "= :col" + " and pos." + Property //$NON-NLS-1$ //$NON-NLS-2$
            .concatNames(SpecimenPositionPeer.CONTAINER, ContainerPeer.ID)
        + "= :contId"; //$NON-NLS-1$

    public Specimen getSpecimen(Session session,
        Integer row, Integer col) {
        Query qry = session.createQuery(SPECIMEN_SEARCH_QRY);
        qry.setParameter("row", row); //$NON-NLS-1$
        qry.setParameter("col", col); //$NON-NLS-1$
        qry.setParameter("contId", getPallet(session).getId()); //$NON-NLS-1$
        @SuppressWarnings("unchecked")
        List<Specimen> list = qry.list();
        if (list.size() == 1)
            return list.get(0);
        return null;
    }

    public Integer getPalletRowCapacity(ActionContext actionContext) {
        ContainerType type =
            getPallet(actionContext.getSession()).getContainerType();
        if (type.getCapacity() != null)
            return type.getCapacity().getRowCapacity();
        return null;
    }

    public Integer getPalletColCapacity(ActionContext actionContext) {
        ContainerType type =
            getPallet(actionContext.getSession()).getContainerType();
        if (type.getCapacity() != null)
            return type.getCapacity().getColCapacity();
        return null;
    }
}
