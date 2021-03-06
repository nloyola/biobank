package edu.ualberta.med.biobank.common.action.reports;

import java.lang.reflect.Constructor;

import edu.ualberta.med.biobank.CommonBundle;
import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.ProxiedListResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.reports.ReportsPermission;
import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.i18n.Bundle;
import edu.ualberta.med.biobank.i18n.LString;
import edu.ualberta.med.biobank.i18n.LocalizedException;
import edu.ualberta.med.biobank.server.reports.AbstractReport;

public class ReportAction implements Action<ProxiedListResult<Object>> {
    private static final long serialVersionUID = 1L;
    private static final Bundle bundle = new CommonBundle();

    @SuppressWarnings("nls")
    public static final LString UNKNOWN_PROBLEM_ERRMSG =
        bundle.tr("Unable to run report for unknown reason").format();

    BiobankReport report;

    public ReportAction(BiobankReport report) {
        this.report = report;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new ReportsPermission().isAllowed(context);
    }

    @SuppressWarnings("nls")
    @Override
    public ProxiedListResult<Object> run(ActionContext context)
        throws ActionException {
        try {
            Class<?> cls =
                Class.forName("edu.ualberta.med.biobank.server.reports."
                    + report.getClassName());
            Class<?> partypes[] = new Class[] { BiobankReport.class };
            Constructor<?> constructor = cls.getConstructor(partypes);
            AbstractReport runReport =
                (AbstractReport) constructor.newInstance(report);
            return new ProxiedListResult<Object>(
                runReport.generate(context
                    .getAppService()));
        } catch (Exception e) {
            throw new LocalizedException(UNKNOWN_PROBLEM_ERRMSG, e);
        }
    }
}
