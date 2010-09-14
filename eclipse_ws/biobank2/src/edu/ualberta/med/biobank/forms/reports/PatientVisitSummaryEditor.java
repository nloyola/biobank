package edu.ualberta.med.biobank.forms.reports;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.widgets.DateTimeWidget;

public class PatientVisitSummaryEditor extends ReportsEditor {

    protected DateTimeWidget start;
    protected DateTimeWidget end;

    public static String ID = "edu.ualberta.med.biobank.editors.PatientVisitSummaryEditor";

    @Override
    protected int[] getColumnWidths() {
        return new int[] { 100, 100, 100, 100, 100, 100, 100, 100, 100 };
    }

    @Override
    protected void createOptionSection(Composite parent) {
        start = widgetCreator.createDateTimeWidget(parent,
            "Start Date (Linked)", null, null, null, SWT.DATE);
        end = widgetCreator.createDateTimeWidget(parent, "End Date (Linked)",
            null, null, null, SWT.DATE);
    }

    @Override
    protected List<Object> getParams() {
        List<Object> params = new ArrayList<Object>();
        params.add(ReportsEditor.processDate(start.getDate(), true));
        params.add(ReportsEditor.processDate(end.getDate(), false));
        return params;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[] { "Study", "Clinic", "1 Visit", "2 Visit",
            "3 Visit", "4 Visit", "5+ Visits", "Total Visits", "Total Patients" };
    }

    @Override
    protected List<String> getParamNames() {
        List<String> param = new ArrayList<String>();
        param.add("Start Date (Linked)");
        param.add("End Date (Linked)");
        return param;
    }

}