package edu.ualberta.med.biobank.mvp.presenter.impl;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import edu.ualberta.med.biobank.model.Address;
import edu.ualberta.med.biobank.mvp.presenter.impl.AddressEditPresenter.View;
import edu.ualberta.med.biobank.mvp.validation.HasValidation;
import edu.ualberta.med.biobank.mvp.validation.PresenterValidation;
import edu.ualberta.med.biobank.mvp.view.BaseView;

public class AddressEditPresenter extends BasePresenter<View> {
    public interface View extends BaseView {
        HasValue<String> getStreet1();

        HasValue<String> getStreet2();

        HasValue<String> getCity();

        HasValue<String> getProvince();

        HasValue<String> getPostalCode();

        HasValue<String> getPhoneNumber();

        HasValue<String> getFaxNumber();

        HasValue<String> getCountry();
    }

    private final HandlerManager handlerManager = new HandlerManager(this);
    private final PresenterValidation validationManager =
        new PresenterValidation();
    private Address address;

    @Inject
    public AddressEditPresenter(View view, EventBus eventBus) {
        super(view, eventBus);
    }

    public void editAddress(Address address) {
        this.address = address;

        view.getStreet1().setValue(address.getStreet1());
        view.getStreet2().setValue(address.getStreet2());
        view.getCity().setValue(address.getCity());
        view.getProvince().setValue(address.getProvince());
        view.getPostalCode().setValue(address.getPostalCode());
        view.getPhoneNumber().setValue(address.getPhoneNumber());
        view.getFaxNumber().setValue(address.getFaxNumber());
        view.getCountry().setValue(address.getCountry());
    }

    public Address getAddress() {
        address.setStreet1(view.getStreet1().getValue());
        address.setStreet2(view.getStreet2().getValue());
        address.setCity(view.getCity().getValue());
        address.setProvince(view.getProvince().getValue());
        address.setPostalCode(view.getPostalCode().getValue());
        address.setPhoneNumber(view.getPhoneNumber().getValue());
        address.setFaxNumber(view.getFaxNumber().getValue());
        address.setCountry(view.getCountry().getValue());

        return address;
    }

    @Override
    protected void onBind() {
        // validationManager.getValidatedValue(view.getCity()).addValidator(
        // new NotEmptyValidator("city"));
    }

    @Override
    protected void onUnbind() {
        validationManager.unbind();
    }

    public HasValidation getValidation() {
        return validationManager;
    }
}
