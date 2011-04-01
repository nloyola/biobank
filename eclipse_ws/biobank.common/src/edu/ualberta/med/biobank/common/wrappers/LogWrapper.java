package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ualberta.med.biobank.common.util.AbstractRowPostProcess;
import edu.ualberta.med.biobank.common.util.PostProcessListProxy;
import edu.ualberta.med.biobank.common.wrappers.base.LogBaseWrapper;
import edu.ualberta.med.biobank.model.Log;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class LogWrapper extends LogBaseWrapper {

    public LogWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public LogWrapper(WritableApplicationService appService, Log log) {
        super(appService, log);
    }

    public static List<LogWrapper> getLogs(
        final WritableApplicationService appService, String center,
        String username, Date startDate, Date endDate, String action,
        String patientNumber, String inventoryId, String locationLabel,
        String details, String type) throws Exception {
        StringBuffer parametersString = new StringBuffer();
        List<Object> parametersArgs = new ArrayList<Object>();
        addParam(parametersString, parametersArgs, "username", username);

        StringBuffer datePart = new StringBuffer();
        if ((startDate != null) && (endDate != null)) {
            datePart.append(" createdAt >= ? and createdAt <= ?");
            parametersArgs.add(startDate);
            parametersArgs.add(endDate);
        } else if (startDate != null) {
            datePart.append(" createdAt >= ?");
            parametersArgs.add(startDate);
        } else if (endDate != null) {
            datePart.append(" createdAt <= ?");
            parametersArgs.add(endDate);
        }

        if (datePart.length() > 0) {
            if (parametersString.length() > 0) {
                parametersString.append(" and");
            }
            parametersString.append(" " + datePart);
        }

        addParam(parametersString, parametersArgs, "center", center);
        addParam(parametersString, parametersArgs, "action", action);
        addParam(parametersString, parametersArgs, "patientNumber",
            patientNumber);
        addParam(parametersString, parametersArgs, "inventoryId", inventoryId);
        addLocationLabelParam(parametersString, parametersArgs, locationLabel);
        addParam(parametersString, parametersArgs, "details", details, false);
        addParam(parametersString, parametersArgs, "type", type);
        StringBuilder qry = new StringBuilder("from ").append(Log.class
            .getName());
        if (parametersString.length() > 0) {
            qry.append(" where").append(parametersString.toString());
        }
        List<LogWrapper> logs = new PostProcessListProxy(appService,
            new HQLCriteria(qry.toString(), parametersArgs),
            new AbstractRowPostProcess() {
                private static final long serialVersionUID = 1L;

                @Override
                public Object rowPostProcess(Object element) {
                    return new LogWrapper(appService, (Log) element);
                }
            });

        return logs;
    }

    private static void addParam(StringBuffer sb, List<Object> parameters,
        String property, Object value) {
        addParam(sb, parameters, property, value, true);
    }

    private static void addParam(StringBuffer sb, List<Object> parameters,
        String property, Object value, boolean strict) {
        if (value != null) {
            if (sb.length() > 0) {
                sb.append(" and");
            }
            sb.append(" ").append(property);
            if (strict) {
                sb.append("=?");
                parameters.add(value);
            } else {
                sb.append(" like ?");
                parameters.add("%" + value + "%");
            }
        }
    }

    private static void addLocationLabelParam(StringBuffer sb,
        List<Object> parameters, Object value) {
        if (value != null) {
            if (sb.length() > 0) {
                sb.append(" and");
            }
            sb.append(" ").append("locationLabel").append(" like ?");
            parameters.add(value + " (%");
        }
    }

    public static final String POSSIBLE_CENTERS_QRY = "select distinct(center) from "
        + Log.class.getName() + " where center !=''";

    public static List<String> getPossibleCenters(
        WritableApplicationService appService) throws ApplicationException {
        return appService.query(new HQLCriteria(POSSIBLE_CENTERS_QRY));
    }

    public static final String POSSIBLE_USER_NAMES_QRY = "select distinct(username) from "
        + Log.class.getName();

    public static List<String> getPossibleUsernames(
        WritableApplicationService appService) throws ApplicationException {
        return appService.query(new HQLCriteria(POSSIBLE_USER_NAMES_QRY));
    }

    public static final String POSSIBLE_ACTIONS_QRY = "select distinct(action) from "
        + Log.class.getName();

    public static List<String> getPossibleActions(
        WritableApplicationService appService) throws ApplicationException {
        return appService.query(new HQLCriteria(POSSIBLE_ACTIONS_QRY));
    }

    public static final String POSSIBLE_TYPES_QRY = "select distinct(type) from "
        + Log.class.getName() + " where type !=''";

    public static List<String> getPossibleTypes(
        WritableApplicationService appService) throws ApplicationException {
        return appService.query(new HQLCriteria(POSSIBLE_TYPES_QRY));
    }

    @Override
    public String toString() {
        return getCreatedAt() + " -- action: " + getAction() + " / type: "
            + getType() + " / patientNumber: " + getPatientNumber()
            + " / inventoryId: " + getInventoryId() + " / locationLabel: "
            + getLocationLabel() + " / details: " + getDetails();
    }
}
