package edu.ualberta.med.biobank.mvp.view;

import edu.ualberta.med.biobank.mvp.user.ui.HasButton;

public interface IReloadableView extends IView {
    HasButton getReload();
}
