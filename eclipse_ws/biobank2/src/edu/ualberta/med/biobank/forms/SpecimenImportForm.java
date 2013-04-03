package edu.ualberta.med.biobank.forms;

import edu.ualberta.med.biobank.batchoperation.IBatchOpPojoReader;
import edu.ualberta.med.biobank.batchoperation.specimen.SpecimenPojoReaderFactory;
import edu.ualberta.med.biobank.common.action.batchoperation.IBatchOpInputPojo;
import edu.ualberta.med.biobank.model.Center;

public class SpecimenImportForm extends ImportForm {

    @SuppressWarnings("nls")
    public static final String ID = "edu.ualberta.med.biobank.forms.SpecimenImportForm";

    @Override
    protected IBatchOpPojoReader<? extends IBatchOpInputPojo> getCsvPojoReader(Center center,
        String csvFilename, String[] csvHeaders) {
        return SpecimenPojoReaderFactory.createPojoReader(center, csvFilename, csvHeaders);
    }

}