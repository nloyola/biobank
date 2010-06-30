package edu.ualberta.med.biobank.test.wrappers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Assert;
import org.junit.Test;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.WrapperException;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.test.TestDatabase;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.internal.SiteHelper;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class TestModelWrapper extends TestDatabase {

    class TestWrapper extends ModelWrapper<Object> {

        public TestWrapper(WritableApplicationService appService) {
            super(appService);
        }

        @Override
        protected void deleteChecks() throws Exception {
        }

        @Override
        protected String[] getPropertyChangeNames() {
            return null;
        }

        @Override
        public Class<Object> getWrappedClass() {
            return null;
        }

        @Override
        protected void persistChecks() throws BiobankCheckException,
            ApplicationException, WrapperException {
        }

        @Override
        public int compareTo(ModelWrapper<Object> o) {
            return 0;
        }

    }

    class TestSiteWrapper extends ModelWrapper<Site> {

        public TestSiteWrapper(WritableApplicationService appService) {
            super(appService);
        }

        @Override
        protected void deleteChecks() throws Exception {
        }

        @Override
        protected String[] getPropertyChangeNames() {
            return null;
        }

        @Override
        public Class<Site> getWrappedClass() {
            return Site.class;
        }

        @Override
        protected void persistChecks() throws BiobankCheckException,
            ApplicationException, WrapperException {
        }

        @Override
        public int compareTo(ModelWrapper<Site> o) {
            return 0;
        }

    }

    @Test
    public void testConstructor() throws Exception {
        try {
            new TestWrapper(appService);
            Assert.fail("should not be allowed to create this wrapper");
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetWrappedObject() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);
        wrapper.setWrappedObject(new Site());
        Site site = wrapper.getWrappedObject();
        Assert.assertTrue(site instanceof Object);
    }

    @Test
    public void testPropertyChangeListener() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
            }
        };

        try {
            wrapper.addPropertyChangeListener("propertyX", listener);
            Assert
                .fail("should not be allowed to create listener for non exisiting property");
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
        }

        SiteWrapper site = SiteHelper.addSite("testPropertyChangeListener");
        site.addPropertyChangeListener("name", listener);
        site.removePropertyChangeListener(listener);
    }

    @Test
    public void testGetId() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);
        Assert.assertNull(wrapper.getId());
    }

    @Test
    public void testGetAppService() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);
        Assert.assertTrue(appService == wrapper.getAppService());
    }

    @Test
    public void testFirePropertyChanges() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);
        SiteWrapper site = SiteHelper.addSite("testFirePropertyChanges");
        Site rawSite = new Site();
        rawSite.setId(site.getId());
        wrapper.setWrappedObject(rawSite);

        try {
            wrapper.reload();
            Assert.fail("should fail since there are no properties");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDelete() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);

        try {
            wrapper.delete();
            Assert
                .fail("should fail since there is no such object in database");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLoadAttributes() throws Exception {
        String name = "testLoadAttributes";
        SiteWrapper wrapper = SiteHelper.addSite(name);
        wrapper.loadAttributes();
    }

    @Test
    public void testEquals() throws Exception {
        TestSiteWrapper wrapper = new TestSiteWrapper(appService);
        SiteWrapper site = SiteHelper.addSite("testEquals");
        Assert.assertFalse(site.equals(null));
        Assert.assertFalse(site.equals(wrapper));

        SiteWrapper site2 = SiteHelper.addSite("testEquals_2");
        Assert.assertFalse(site.equals(site2));

        // call to equals should handle null IDs
        TestSiteWrapper wrapper2 = new TestSiteWrapper(appService);
        Assert.assertTrue(wrapper.equals(wrapper2));
    }

    @Test
    public void testPersist() throws Exception {
        // test insert and update into database
        SiteWrapper site = SiteHelper.addSite("testEquals");
        site.setName(Utils.getRandomString(10, 15));
        site.persist();
    }

    @Test
    public void testReset() throws Exception {
        SiteWrapper wrapper = new SiteWrapper(appService);
        wrapper.reset();

        SiteWrapper site = SiteHelper.addSite("testEquals");
        site.setName(Utils.getRandomString(10, 15));
        site.reset();
    }

    @Test
    public void testGetAllObjects() throws Exception {

    }
}
