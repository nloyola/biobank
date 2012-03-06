package edu.ualberta.med.biobank.common.peer;

import edu.ualberta.med.biobank.common.util.TypeReference;
import java.util.Collections;
import edu.ualberta.med.biobank.common.wrappers.Property;
import java.util.List;
import java.util.ArrayList;
import edu.ualberta.med.biobank.model.ReportColumn;
import edu.ualberta.med.biobank.model.Entity;
import edu.ualberta.med.biobank.model.ReportFilter;
import java.util.Collection;
import edu.ualberta.med.biobank.model.Report;

public class ReportPeer {
	public static final Property<Integer, Report> ID = Property.create(
		"id" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Integer>() {}
		, new Property.Accessor<Integer, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Integer get(Report model) {
				return model.getId();
			}
			@Override
			public void set(Report model, Integer value) {
				model.setId(value);
			}
		});

	public static final Property<Boolean, Report> IS_COUNT = Property.create(
		"isCount" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Boolean>() {}
		, new Property.Accessor<Boolean, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Boolean get(Report model) {
				return model.getIsCount();
			}
			@Override
			public void set(Report model, Boolean value) {
				model.setIsCount(value);
			}
		});

	public static final Property<String, Report> DESCRIPTION = Property.create(
		"description" //$NON-NLS-1$
		, Report.class
		, new TypeReference<String>() {}
		, new Property.Accessor<String, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public String get(Report model) {
				return model.getDescription();
			}
			@Override
			public void set(Report model, String value) {
				model.setDescription(value);
			}
		});

	public static final Property<Integer, Report> USER_ID = Property.create(
		"userId" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Integer>() {}
		, new Property.Accessor<Integer, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Integer get(Report model) {
				return model.getUserId();
			}
			@Override
			public void set(Report model, Integer value) {
				model.setUserId(value);
			}
		});

	public static final Property<String, Report> NAME = Property.create(
		"name" //$NON-NLS-1$
		, Report.class
		, new TypeReference<String>() {}
		, new Property.Accessor<String, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public String get(Report model) {
				return model.getName();
			}
			@Override
			public void set(Report model, String value) {
				model.setName(value);
			}
		});

	public static final Property<Boolean, Report> IS_PUBLIC = Property.create(
		"isPublic" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Boolean>() {}
		, new Property.Accessor<Boolean, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Boolean get(Report model) {
				return model.getIsPublic();
			}
			@Override
			public void set(Report model, Boolean value) {
				model.setIsPublic(value);
			}
		});

	public static final Property<Collection<ReportColumn>, Report> REPORT_COLUMN_COLLECTION = Property.create(
		"reportColumnCollection" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Collection<ReportColumn>>() {}
		, new Property.Accessor<Collection<ReportColumn>, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Collection<ReportColumn> get(Report model) {
				return model.getReportColumns();
			}
			@Override
			public void set(Report model, Collection<ReportColumn> value) {
				model.getReportColumns().clear();
				model.getReportColumns().addAll(value);
			}
		});

	public static final Property<Entity, Report> ENTITY = Property.create(
		"entity" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Entity>() {}
		, new Property.Accessor<Entity, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Entity get(Report model) {
				return model.getEntity();
			}
			@Override
			public void set(Report model, Entity value) {
				model.setEntity(value);
			}
		});

	public static final Property<Collection<ReportFilter>, Report> REPORT_FILTER_COLLECTION = Property.create(
		"reportFilterCollection" //$NON-NLS-1$
		, Report.class
		, new TypeReference<Collection<ReportFilter>>() {}
		, new Property.Accessor<Collection<ReportFilter>, Report>() { private static final long serialVersionUID = 1L;
			@Override
			public Collection<ReportFilter> get(Report model) {
				return model.getReportFilters();
			}
			@Override
			public void set(Report model, Collection<ReportFilter> value) {
				model.getReportFilters().clear();
				model.getReportFilters().addAll(value);
			}
		});

   public static final List<Property<?, ? super Report>> PROPERTIES;
   static {
      List<Property<?, ? super Report>> aList = new ArrayList<Property<?, ? super Report>>();
      aList.add(ID);
      aList.add(IS_COUNT);
      aList.add(DESCRIPTION);
      aList.add(USER_ID);
      aList.add(NAME);
      aList.add(IS_PUBLIC);
      aList.add(REPORT_COLUMN_COLLECTION);
      aList.add(ENTITY);
      aList.add(REPORT_FILTER_COLLECTION);
      PROPERTIES = Collections.unmodifiableList(aList);
   };
}
