package edu.ualberta.med.biobank.common.action.batchoperation.specimen;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.biobank.CommonBundle;
import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.IdResult;
import edu.ualberta.med.biobank.common.action.batchoperation.BatchOpActionUtil;
import edu.ualberta.med.biobank.common.action.batchoperation.BatchOpInputErrorSet;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.exception.BatchOpErrorsException;
import edu.ualberta.med.biobank.i18n.Bundle;
import edu.ualberta.med.biobank.i18n.LString;
import edu.ualberta.med.biobank.i18n.Tr;
import edu.ualberta.med.biobank.model.BatchOperation;
import edu.ualberta.med.biobank.model.BatchOperationSpecimen;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.FileData;
import edu.ualberta.med.biobank.model.OriginInfo;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.PermissionEnum;
import edu.ualberta.med.biobank.model.ProcessingEvent;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.model.util.RowColPos;
import edu.ualberta.med.biobank.util.CompressedReference;

/**
 * This action takes a list of Specimen Batch Operation beans as input, verifies that the data is
 * valid, and if valid saves the data to the database.
 *
 * @author Nelson Loyola
 *
 */
@SuppressWarnings("nls")
public class SpecimenBatchOpAction implements Action<IdResult> {
    private static final long serialVersionUID = 1L;

    private static final Bundle bundle = new CommonBundle();

    private static Logger log = LoggerFactory.getLogger(SpecimenBatchOpAction.class);

    public static final int SIZE_LIMIT = 1000;

    private static final LString SPC_ALREADY_EXISTS_ERROR =
        bundle.tr("specimen already exists").format();

    public static final Tr CSV_CEVENT_ERROR =
        bundle.tr("collection event for patient {0} with visit number \"{1}\" does not exist");

    private static final LString CSV_PARENT_SPC_ERROR =
        bundle.tr("specimen declared a source specimen but parent inventory ID present").format();

    public static final Tr CSV_PARENT_SPC_INV_ID_ERROR =
        bundle.tr("parent inventory id does not exist: {0}");

    public static final LString CSV_ALIQ_SPC_PATIENT_CEVENT_MISSING_ERROR =
        bundle.tr("when parent inventory id is not specified, "
            + "patient number, and visit number are required").format();

    public static final LString CSV_PALLET_POS_ERROR =
        bundle.tr("pallet position defined but not product barcode or label").format();

    private static final LString CSV_PROD_BARCODE_NO_POS_ERROR =
        bundle.tr("pallet product barcode defined but not position").format();

    private static final LString CSV_PALLET_LABEL_NO_CTYPE_ERROR =
        bundle.tr("pallet label defined but not container type").format();

    private static final Tr CSV_WAYBILL_ERROR =
        bundle.tr("waybill \"{0}\" does not exist");

    public static final Tr CSV_SPECIMEN_TYPE_ERROR =
        bundle.tr("specimen type with name \"{0}\" does not exist");

    private static final Tr CSV_CONTAINER_LABEL_ERROR =
        bundle.tr("container with label \"{0}\" does not exist");

    private static final Tr CSV_CONTAINER_BARCODE_ERROR =
        bundle.tr("container with product barcode \"{0}\" does not exist");

    public static final Tr CSV_SPECIMEN_LABEL_ERROR =
        bundle.tr("specimen position \"{0}\" in container with label \"{1}\" is invalid");

    public static final Tr CSV_SPECIMEN_BARCODE_ERROR =
        bundle.tr("specimen position \"{0}\" in container with product barcode \"{1}\" is invalid");

    public static final Tr CSV_PATIENT_NUMBER_INVALID_ERROR =
        bundle.tr("patient number is invalid");

    private static final Tr CSV_PATIENT_MATCH_ERROR =
        bundle.tr("patient number \"{0}\" does not match "
            + "the patient on the source specimen \"{1}\"");

    private static final Tr CSV_CEVENT_MATCH_ERROR =
        bundle.tr("collection event with visit number \"{0}\" "
            + "does match the source specimen's collection event");

    public static final Tr CSV_STUDY_SOURCE_SPC_TYPE_ERROR =
        bundle.tr("specimen type \"{0}\" is invalid for parent specimens in study \"{1}\"");

    public static final Tr CSV_STUDY_ALIQUOTED_SPC_TYPE_ERROR =
        bundle.tr("specimen type \"{0}\" is invalid for child specimens in study \"{1}\"");

    public static final Tr CSV_ORIGIN_CENTER_SHORT_NAME_ERROR =
        bundle.tr("invalid origin center short name: {0}");

    public static final Tr CSV_CURRENT_CENTER_SHORT_NAME_ERROR =
        bundle.tr("invalid current center short name: {0}");

    private static final Tr CSV_CONTAINER_SPC_TYPE_ERROR =
        bundle.tr("specimen type \"{0}\" cannot be stored in this container");

    public static final Tr CSV_CONTAINER_POS_OCCUPIED_ERROR =
        bundle.tr("specimen position \"{0}\" in container with label \"{1}\" is occupied");

    public static final Tr CSV_LABEL_POS_OCCUPIED_ERROR =
        bundle.tr("specimen position \"{0}\" in container with label \"{1}\" is occupied");

    private final Integer workingCenterId;

    private Center workingCenterOnServerSide;
    private final CompressedReference<ArrayList<SpecimenBatchOpInputPojo>> compressedList;

    private final FileData fileData;

    private ArrayList<SpecimenBatchOpInputPojo> pojos = null;

    private final Map<String, Specimen> parentSpecimens = new HashMap<String, Specimen>(0);

    private final Map<String, ProcessingEvent> createdProcessingEvents =
        new HashMap<String, ProcessingEvent>(0);

    private final BatchOpInputErrorSet errorSet = new BatchOpInputErrorSet();

    public SpecimenBatchOpAction(
        Center workingCenter,
        Set<SpecimenBatchOpInputPojo> batchOpSpecimens,
        File inputFile)
        throws NoSuchAlgorithmException, IOException {

        if (batchOpSpecimens.isEmpty()) {
            throw new IllegalArgumentException("pojo list is empty");
        }

        if (batchOpSpecimens.size() > SIZE_LIMIT) {
            throw new IllegalArgumentException(
                "Number of rows in file greater than maximum: " + batchOpSpecimens.size());
        }

        this.workingCenterId = workingCenter.getId();
        this.fileData = FileData.fromFile(inputFile);

        compressedList = new CompressedReference<ArrayList<SpecimenBatchOpInputPojo>>(
            new ArrayList<SpecimenBatchOpInputPojo>(batchOpSpecimens));
        log.debug("constructor exit");
    }

    private void decompressData() {
        if (compressedList == null) {
            throw new IllegalStateException("compressed list is null");
        }

        try {
            pojos = compressedList.get();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        log.debug("isAllowed: start");
        if (compressedList == null) {
            throw new IllegalStateException("compressed list is null");
        }

        decompressData();

        User user = context.getUser();
        workingCenterOnServerSide = context.load(Center.class, workingCenterId);

        return PermissionEnum.BATCH_OPERATIONS.isAllowed(user,
            workingCenterOnServerSide)
            && hasPermissionOnStudies(user, getStudies(context));

    }

    private boolean hasPermissionOnStudies(User user, Set<Study> studies) {
        for (Study study : studies) {
            if (!PermissionEnum.BATCH_OPERATIONS.isAllowed(user, study)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Returns a list of studies that existing specimens and patients in the pojo data belong to.
     */
    private Set<Study> getStudies(ActionContext context) {
        Set<Specimen> existingSpecimens = new HashSet<Specimen>();
        Set<Patient> existingPatients = new HashSet<Patient>();

        for (SpecimenBatchOpInputPojo pojo : pojos) {
            String parentInvId = pojo.getParentInventoryId();
            if ((parentInvId == null) || parentInvId.isEmpty()) continue;
            Specimen specimen = BatchOpActionUtil.getSpecimen(context.getSession(), parentInvId);
            if (specimen != null) {
                existingSpecimens.add(specimen);
            }
        }

        for (SpecimenBatchOpInputPojo pojo : pojos) {
            String pnumber = pojo.getPatientNumber();
            if ((pnumber == null) || pnumber.isEmpty()) continue;
            Patient patient = BatchOpActionUtil.getPatient(context.getSession(), pnumber);
            if (patient != null) {
                existingPatients.add(patient);
            }
        }

        // get all collection events
        for (Specimen specimen : existingSpecimens) {
            specimen.getCollectionEvent();
        }

        // get all patients from specimens
        for (Specimen specimen : existingSpecimens) {
            specimen.getCollectionEvent().getPatient();
        }

        Set<Study> studies = new HashSet<Study>();
        for (Specimen specimen : existingSpecimens) {
            studies.add(specimen.getCollectionEvent().getPatient().getStudy());
        }
        for (Patient patient : existingPatients) {
            studies.add(patient.getStudy());
        }
        return studies;
    }

    @Override
    public IdResult run(ActionContext context) throws ActionException {
        log.debug("SpecimenBatchOpAction:run");

        if (fileData == null) {
            throw new IllegalStateException("file data is null");
        }

        if (pojos == null) {
            throw new IllegalStateException("pojos were not decompressed");
        }

        if (pojos.isEmpty()) {
            throw new IllegalStateException("pojo list is empty");
        }

        for (SpecimenBatchOpInputPojo pojo : pojos) {
            validatePojo(pojo);
        }

        if (!errorSet.isEmpty()) {
            throw new BatchOpErrorsException(errorSet.getErrors());
        }

        // for improved performance, model objects of the same type are loaded sequentially
        // getModelObjects(context, pojos);

        Map<String, SpecimenBatchOpInputPojo> pojoMap = new HashMap<String, SpecimenBatchOpInputPojo>(0);

        Map<String, SpecimenBatchOpBuilder> pojoDataMap = new HashMap<String, SpecimenBatchOpBuilder>(0);

        Set<SpecimenBatchOpBuilder> aliquotSpcPojoData = new HashSet<SpecimenBatchOpBuilder>();

        log.debug("SpecimenBatchOpAction: getting DB info");
        for (SpecimenBatchOpInputPojo pojo : pojos) {
            String inventoryId = pojo.getInventoryId();
            if (pojoMap.containsKey(inventoryId)) {
                throw new IllegalStateException("inventory ID found more than once: " + inventoryId);
            }
            pojoMap.put(inventoryId, pojo);
        }

        for (SpecimenBatchOpInputPojo pojo : pojos) {
            SpecimenBatchOpBuilder pojoData = getDbInfo(context, pojo,
                pojoMap.get(pojo.getParentInventoryId()));

            if (pojoData != null) {
                if (pojoData.isAliquotedSpecimen()) {
                    aliquotSpcPojoData.add(pojoData);
                }
                pojoDataMap.put(pojo.getInventoryId(), pojoData);
            }
        }

        // assign the parent specimen for child specimens
        for (SpecimenBatchOpBuilder pojoData : aliquotSpcPojoData) {
            SpecimenBatchOpBuilder parentPojoData =
                pojoDataMap.get(pojoData.getParentInventoryId());

            if (parentPojoData != null) {
                pojoData.setParentPojoData(parentPojoData);
            }
        }

        for (SpecimenBatchOpBuilder pojoData : pojoDataMap.values()) {
            boolean valid = pojoData.validate();
            if (!valid) {
                errorSet.addAll(pojoData.getErrorList());
            }

            if (pojoData.getParentInventoryId() != null) {
                SpecimenBatchOpInputPojo pojo = pojoData.getPojo();

                // ensure that aliquoted specimens with parent specimens already
                // in the database have a collection event
                if ((pojoData.getParentSpecimen() != null)
                    && (pojoData.getParentSpecimen().getCollectionEvent() == null)) {
                    errorSet.addError(pojo.getLineNumber(),
                        CSV_CEVENT_ERROR.format(pojo.getPatientNumber(), pojo.getVisitNumber()));
                }
            }
        }

        if (!errorSet.isEmpty()) {
            throw new BatchOpErrorsException(errorSet.getErrors());
        }

        BatchOperation batchOp = BatchOpActionUtil.createBatchOperation(
            context.getSession(), context.getUser(), fileData);

        // add all source specimens first
        log.debug("SpecimenBatchOpAction: adding source specimens");
        for (SpecimenBatchOpBuilder info : pojoDataMap.values()) {
            if (!info.getPojo().getSourceSpecimen()) continue;

            Specimen spc = addSpecimen(context, batchOp, info);
            parentSpecimens.put(spc.getInventoryId(), spc);

            if (info.getPevent() == null) {
                ProcessingEvent pevent = createProcessignEventIfRequired(context, info);
                if (pevent != null) {
                    createdProcessingEvents.put(pevent.getWorksheet(), pevent);

                    spc.setProcessingEvent(pevent);
                    pevent.getSpecimens().add(spc);
                }
            }

            // TODO: set activity status to closed?
        }

        // now add aliquoted specimens
        log.debug("SpecimenBatchOpAction: adding aliquot specimens");
        for (SpecimenBatchOpBuilder info : aliquotSpcPojoData) {
            if ((info.getParentInventoryId() != null)
                && (info.getParentSpecimen() == null)) {
                Specimen parentSpc =
                    parentSpecimens.get(info.getParentInventoryId());
                if (parentSpc == null) {
                    errorSet.addError(info.getPojo().getLineNumber(),
                        CSV_PARENT_SPC_INV_ID_ERROR.format(info.getPojo().getParentInventoryId()));
                } else {
                    info.setParentSpecimen(parentSpc);
                }
            }

            if (errorSet.isEmpty()) {
                createProcessignEventIfRequired(context, info);
                addSpecimen(context, batchOp, info);
            }
        }

        if (!errorSet.isEmpty()) {
            throw new BatchOpErrorsException(errorSet.getErrors());
        }

        log.debug("SpecimenBatchOpAction:end");
        return new IdResult(batchOp.getId());
    }

    private void validatePojo(SpecimenBatchOpInputPojo pojo) {
        if (pojo.getSourceSpecimen()) {
            if ((pojo.getParentInventoryId() != null)) {
                errorSet.addError(pojo.getLineNumber(), CSV_PARENT_SPC_ERROR);
            }

            checkForPatientAndCollectionEvent(pojo);
        } else {
            // this is an aliquoted specimen

            if (pojo.getParentInventoryId() == null) {
                checkForPatientAndCollectionEvent(pojo);
            }
        }

        String productBarcode = pojo.getPalletProductBarcode();
        String label = pojo.getPalletLabel();
        String position = pojo.getPalletPosition();
        String rootContainerType = pojo.getRootContainerType();

        boolean hasLabel = (label != null) && !label.isEmpty();
        boolean hasProductBarcode = (productBarcode != null) && !productBarcode.isEmpty();
        boolean hasPosition = (position != null) && !position.isEmpty();
        boolean hasRootContainerType = (rootContainerType != null) && !rootContainerType.isEmpty();

        if (hasPosition && !hasProductBarcode && !hasLabel) {
            errorSet.addError(pojo.getLineNumber(), CSV_PALLET_POS_ERROR);
        }

        if (hasProductBarcode && !hasLabel && !hasPosition) {
            errorSet.addError(pojo.getLineNumber(), CSV_PROD_BARCODE_NO_POS_ERROR);
        }

        if (hasLabel && !hasProductBarcode && !hasPosition) {
            errorSet.addError(pojo.getLineNumber(), CSV_PALLET_POS_ERROR);
        }

        if (hasLabel && hasPosition && !hasRootContainerType) {
            errorSet.addError(pojo.getLineNumber(), CSV_PALLET_LABEL_NO_CTYPE_ERROR);
        }

    }

    private void checkForPatientAndCollectionEvent(
        SpecimenBatchOpInputPojo csvPojo) {

        if (!csvPojo.hasPatientAndCollectionEvent()) {
            // no parent inventory id and does not have patient number and visit
            // number
            errorSet.addError(csvPojo.getLineNumber(),
                CSV_ALIQ_SPC_PATIENT_CEVENT_MISSING_ERROR);
        }
    }

    // get referenced items that exist in the database
    private SpecimenBatchOpBuilder getDbInfo(ActionContext context,
        SpecimenBatchOpInputPojo inputPojo,
        SpecimenBatchOpInputPojo parentInputPojo) {
        Specimen spc = BatchOpActionUtil.getSpecimen(context.getSession(), inputPojo.getInventoryId());
        if (spc != null) {
            errorSet.addError(inputPojo.getLineNumber(),
                SPC_ALREADY_EXISTS_ERROR);
            return null;
        }

        SpecimenBatchOpBuilder pojoData = new SpecimenBatchOpBuilder(inputPojo, parentInputPojo);
        pojoData.setUser(context.getUser());

        Specimen parentSpecimen = null;

        Patient patient = null;

        if (inputPojo.getParentInventoryId() != null) {
            parentSpecimen = BatchOpActionUtil.getSpecimen(context.getSession(),
                inputPojo.getParentInventoryId());

            if (parentSpecimen != null) {
                pojoData.setParentSpecimen(parentSpecimen);
                patient = parentSpecimen.getCollectionEvent().getPatient();
            }
        }

        SpecimenType spcType = BatchOpActionUtil.getSpecimenType(context.getSession(),
            inputPojo.getSpecimenType());
        if (spcType == null) {
            errorSet.addError(inputPojo.getLineNumber(),
                CSV_SPECIMEN_TYPE_ERROR.format(inputPojo.getSpecimenType()));
        } else {
            pojoData.setSpecimenType(spcType);
        }

        if (pojoData.isSourceSpecimen()) {
            patient = BatchOpActionUtil.getPatient(context.getSession(), inputPojo.getPatientNumber());
            if (patient == null) {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_PATIENT_NUMBER_INVALID_ERROR.format());
                return null;
            }
            pojoData.setPatient(patient);

            log.debug("retrieving patient for specimen: invId={} pnumber={}",
                inputPojo.getInventoryId(), inputPojo.getPatientNumber());

            Set<SpecimenType> siteSourceSpecimenTypes =
                BatchOpActionUtil.getSiteSourceSpecimenTypes(patient.getStudy());

            if ((spcType != null) && !siteSourceSpecimenTypes.contains(spcType)) {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_STUDY_SOURCE_SPC_TYPE_ERROR.format(
                        spcType.getName(), patient.getStudy().getNameShort()));
                return null;
            }
        } else {
            // ensure child specimen type is allowed for study
            if (inputPojo.getPatientNumber() != null) {
                patient = BatchOpActionUtil.getPatient(
                    context.getSession(), inputPojo.getPatientNumber());
            } else if (parentSpecimen != null) {
                patient = parentSpecimen.getCollectionEvent().getPatient();
            } else if (parentInputPojo != null) {
                patient = BatchOpActionUtil.getPatient(
                    context.getSession(), parentInputPojo.getPatientNumber());
            }

            if (patient != null) {
                Set<SpecimenType> siteAliquotedSpecimenTypes =
                    BatchOpActionUtil.getSiteAliquotedSpecimenTypes(patient.getStudy());

                if ((spcType != null) && !siteAliquotedSpecimenTypes.contains(spcType)) {
                    errorSet.addError(inputPojo.getLineNumber(),
                        CSV_STUDY_ALIQUOTED_SPC_TYPE_ERROR.format(
                            spcType.getName(), patient.getStudy().getNameShort()));
                    return null;
                }
            }
        }

        CollectionEvent cevent = getAndVerifyCollectionEvent(context, inputPojo, parentSpecimen);

        if (cevent == null) {
            // only aliquoted specimens with no parent require a collection
            // event
            if (pojoData.isAliquotedSpecimen()) {
                if (pojoData.getParentInventoryId() == null) {
                    errorSet.addError(
                        inputPojo.getLineNumber(),
                        CSV_CEVENT_ERROR.format(inputPojo.getPatientNumber(),
                            inputPojo.getVisitNumber()));
                }
            }
        } else {
            pojoData.setCevent(cevent);
            pojoData.setPatient(cevent.getPatient());
        }

        if (inputPojo.getWaybill() != null) {
            OriginInfo originInfo =
                BatchOpActionUtil.getOriginInfo(context.getSession(), inputPojo.getWaybill());
            if (originInfo == null) {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_WAYBILL_ERROR.format(inputPojo.getWaybill()));
            } else {
                pojoData.setOriginInfo(originInfo);
            }
        }

        if (inputPojo.getWorksheet() != null) {
            ProcessingEvent pevent = BatchOpActionUtil.getProcessingEvent(
                context.getSession(), inputPojo.getWorksheet());
            if (pevent != null) {
                pojoData.setPevent(pevent);
                log.trace("found processing event: invId={} worksheet={}",
                    inputPojo.getInventoryId(), inputPojo.getWorksheet());
            }
        }

        if (inputPojo.getOriginCenter() != null) {
            Center center = BatchOpActionUtil.getCenter(context.getSession(), inputPojo.getOriginCenter());
            if (center != null) {
                pojoData.setOriginCenter(center);
                log.trace("found origin center: center={}", inputPojo.getOriginCenter());
            } else {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_ORIGIN_CENTER_SHORT_NAME_ERROR.format(inputPojo.getOriginCenter()));
                return null;
            }
        }

        if (inputPojo.getCurrentCenter() != null) {
            Center center = BatchOpActionUtil.getCenter(context.getSession(), inputPojo.getCurrentCenter());
            if (center != null) {
                pojoData.setCurrentCenter(center);
                log.trace("found current center: center={}", inputPojo.getCurrentCenter());
            } else {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_CURRENT_CENTER_SHORT_NAME_ERROR.format(inputPojo.getCurrentCenter()));
                return null;
            }
        }

        // only get container information if defined for this row

        if (pojoData.hasPosition()) {
            String position = inputPojo.getPalletPosition();
            String label = inputPojo.getPalletLabel();
            String barcode = inputPojo.getPalletProductBarcode();
            boolean hasLabel = (label != null) && !label.isEmpty();
            Container container;

            if (hasLabel) {
                container = BatchOpActionUtil.getContainer(context.getSession(), label);

                if (container == null) {
                    errorSet.addError(inputPojo.getLineNumber(),
                        CSV_CONTAINER_LABEL_ERROR.format(label));
                    return null;
                }
            } else {
                container = BatchOpActionUtil.getContainerByBarcode(context.getSession(), barcode);

                if (container == null) {
                    errorSet.addError(inputPojo.getLineNumber(), CSV_CONTAINER_BARCODE_ERROR.format(barcode));
                    return null;
                }
            }

            pojoData.setContainer(container);

            if (!container.getContainerType().getSpecimenTypes().contains(spcType)) {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_CONTAINER_SPC_TYPE_ERROR.format(spcType.getName()));
                return null;
            }

            try {
                RowColPos pos = container.getPositionFromLabelingScheme(position);

                // is container position empty?
                if (!container.isPositionFree(pos)) {
                    if (hasLabel) {
                        errorSet.addError(inputPojo.getLineNumber(),
                            CSV_LABEL_POS_OCCUPIED_ERROR.format(position, label));
                    } else {
                        errorSet.addError(inputPojo.getLineNumber(),
                            CSV_CONTAINER_POS_OCCUPIED_ERROR.format(position, barcode));
                    }
                    return null;
                }

                pojoData.setSpecimenPos(pos);
            } catch (Exception e) {
                if (hasLabel) {
                    errorSet.addError(inputPojo.getLineNumber(),
                        CSV_SPECIMEN_LABEL_ERROR.format(position, label));
                } else {
                    errorSet.addError(inputPojo.getLineNumber(),
                        CSV_SPECIMEN_BARCODE_ERROR.format(position, barcode));
                }
                return null;
            }

        }

        return pojoData;
    }

    private Specimen addSpecimen(ActionContext context,
        BatchOperation batchOp, SpecimenBatchOpBuilder pojoData) {
        if (context == null) {
            throw new NullPointerException("context is null");
        }

        if (workingCenterOnServerSide == null) {
            // workingCenterOnServerSide is assigned when isAllowed() is called
            throw new IllegalStateException("workingCenterOnServerSide is null");
        }

        Center originCenter = (pojoData.getOriginCenter() != null)
            ? pojoData.getOriginCenter() : workingCenterOnServerSide;

        OriginInfo originInfo = pojoData.getOriginInfo();
        if (originInfo == null) {
            originInfo = pojoData.createNewOriginInfo(originCenter);
        }

        CollectionEvent cevent = pojoData.getCevent();
        if (cevent == null) {
            // if this is a source specimen then see if the patient has the
            // collection event
            if (pojoData.getPojo().getSourceSpecimen()) {
                cevent = BatchOpActionUtil.getCollectionEvent(context.getSession(),
                    pojoData.getPojo().getPatientNumber(),
                    pojoData.getPojo().getVisitNumber());

                log.debug(
                    "collection event found: pt={} v#={} invId={}",
                    new Object[] {
                        pojoData.getPojo().getPatientNumber(),
                        pojoData.getPojo().getVisitNumber(),
                        pojoData.getPojo().getInventoryId()
                    });
            } else if (pojoData.getParentSpecimen() != null) {
                // if this is an aliquoted specimen, then get the collection
                // event from the source specimen
                cevent = pojoData.getParentSpecimen().getCollectionEvent();
            }

            // if still not found create one
            if (cevent == null) {
                cevent = pojoData.createNewCollectionEvent();
                context.getSession().saveOrUpdate(cevent);
            }

            pojoData.setCevent(cevent);
            pojoData.setPatient(cevent.getPatient());
        }

        Specimen spc = pojoData.createNewSpecimen();

        // check if this specimen has a comment and if so save it to DB
        if (!spc.getComments().isEmpty()) {
            Comment comment = spc.getComments().iterator().next();
            context.getSession().save(comment);
        }

        context.getSession().save(spc.getOriginInfo());
        context.getSession().save(spc);

        BatchOperationSpecimen batchOpSpc = new BatchOperationSpecimen();
        batchOpSpc.setBatch(batchOp);
        batchOpSpc.setSpecimen(spc);
        context.getSession().save(batchOpSpc);

        return spc;
    }

    // find the collection event for this specimen
    private CollectionEvent getAndVerifyCollectionEvent(ActionContext context,
        SpecimenBatchOpInputPojo inputPojo, Specimen parentSpecimen) {
        CollectionEvent cevent = null;

        if (inputPojo.getParentInventoryId() == null) {
            if (inputPojo.getPatientNumber() == null) {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_PATIENT_NUMBER_INVALID_ERROR.format());
                return null;
            }

            cevent = BatchOpActionUtil.getCollectionEvent(context.getSession(),
                inputPojo.getPatientNumber(), inputPojo.getVisitNumber());
            return cevent;
        }

        if (parentSpecimen != null) {
            cevent = parentSpecimen.getCollectionEvent();

            // if patient number and visit number present in the pojo
            // ensure they match with the cevent and patient
            if ((inputPojo.getPatientNumber() != null) && !inputPojo.getPatientNumber().isEmpty()
                && !cevent.getPatient().getPnumber().equals(inputPojo.getPatientNumber())) {
                errorSet.addError(inputPojo.getLineNumber(), CSV_PATIENT_MATCH_ERROR.format(
                    inputPojo.getPatientNumber(), cevent.getPatient().getPnumber()));
            }

            if ((inputPojo.getVisitNumber() != null)
                && !cevent.getVisitNumber().equals(inputPojo.getVisitNumber())) {
                errorSet.addError(inputPojo.getLineNumber(),
                    CSV_CEVENT_MATCH_ERROR.format(
                        inputPojo.getVisitNumber()));
            }
        }

        // if parentSpecimen is null, then it comes from the CSV file
        return cevent;
    }

    private ProcessingEvent createProcessignEventIfRequired(ActionContext context,
        SpecimenBatchOpBuilder pojoData) {
        ProcessingEvent pevent = null;

        // add the processing event for this source specimen
        if (pojoData.getPojo().getWorksheet() != null) {
            pevent = createdProcessingEvents.get(pojoData.getPojo().getWorksheet());

            if (pojoData.getPevent() == null) {
                if (pevent != null) {
                    pojoData.setPevent(pevent);
                    log.debug("createProcessignEventIfRequired: processing event created previously");
                } else {
                    pevent = pojoData.createNewProcessingEvent();
                    context.getSession().saveOrUpdate(pevent);
                    log.debug("createProcessignEventIfRequired: created new processing event");

                }
            }
        }
        return pevent;
    }

}
