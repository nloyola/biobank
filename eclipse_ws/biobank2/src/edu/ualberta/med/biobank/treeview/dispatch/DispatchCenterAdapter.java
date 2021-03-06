package edu.ualberta.med.biobank.treeview.dispatch;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.forms.SiteViewForm;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class DispatchCenterAdapter extends AdapterBase {

    private final OutgoingNode out;
    private final IncomingNode inc;

    public DispatchCenterAdapter(AdapterBase parent, CenterWrapper<?> center) {
        super(parent, center);
        out = new OutgoingNode(this, 0);
        out.setParent(this);
        this.addChild(out);

        inc = new IncomingNode(this, 1);
        inc.setParent(this);
        this.addChild(inc);
    }

    @SuppressWarnings("nls")
    @Override
    protected String getLabelInternal() {
        CenterWrapper<?> site = (CenterWrapper<?>) getModelObject();
        Assert.isNotNull(site, "site is null");
        return site.getNameShort();
    }

    @Override
    public String getTooltipTextInternal() {
        return getTooltipText(Site.NAME.singular().toString());
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
    }

    @Override
    protected String getConfirmDeleteMessage() {
        return null;
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        return searchChildren(searchedClass, objectId);
    }

    @Override
    protected AbstractAdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AbstractAdapterBase createChildNode(Object child) {
        return null;
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public String getViewFormId() {
        return SiteViewForm.ID;
    }

    @Override
    public void rebuild() {
        for (AbstractAdapterBase adaper : getChildren()) {
            adaper.rebuild();
        }
    }

    @Override
    public void performDoubleClick() {

    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        if (o instanceof DispatchCenterAdapter)
            return internalCompareTo(o);
        return 0;
    }
}
