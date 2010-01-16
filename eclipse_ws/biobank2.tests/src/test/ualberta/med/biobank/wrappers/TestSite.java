package test.ualberta.med.biobank.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import test.ualberta.med.biobank.TestDatabase;
import test.ualberta.med.biobank.Utils;
import test.ualberta.med.biobank.internal.ClinicHelper;
import test.ualberta.med.biobank.internal.ContainerHelper;
import test.ualberta.med.biobank.internal.ContainerTypeHelper;
import test.ualberta.med.biobank.internal.DbHelper;
import test.ualberta.med.biobank.internal.SampleTypeHelper;
import test.ualberta.med.biobank.internal.SiteHelper;
import test.ualberta.med.biobank.internal.StudyHelper;
import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.common.wrappers.internal.PvAttrTypeWrapper;
import edu.ualberta.med.biobank.model.SampleType;
import edu.ualberta.med.biobank.model.Site;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class TestSite extends TestDatabase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGettersAndSetters() throws Exception {
        SiteWrapper site = SiteHelper.addSite("testGettersAndSetters"
            + r.nextInt());
        testGettersAndSetters(site);
    }

    @Test
    public void testGetStudyCollection() throws Exception {
        String name = "testGetStudyCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int studiesNber = r.nextInt(15) + 1;
        StudyHelper.addStudies(site, name, studiesNber);

        List<StudyWrapper> studies = site.getStudyCollection();
        int sizeFound = studies.size();

        Assert.assertEquals(studiesNber, sizeFound);
    }

    @Test
    public void testGetStudyCollectionBoolean() throws Exception {
        String name = "testGetStudyCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        StudyHelper.addStudies(site, name, r.nextInt(15) + 5);

        List<StudyWrapper> studiesSorted = site.getStudyCollection(true);
        if (studiesSorted.size() > 1) {
            for (int i = 0; i < studiesSorted.size() - 1; i++) {
                StudyWrapper study1 = studiesSorted.get(i);
                StudyWrapper study2 = studiesSorted.get(i + 1);
                Assert.assertTrue(study1.compareTo(study2) <= 0);
            }
        }
    }

    @Test
    public void testSetStudyCollectionAdd() throws Exception {
        String name = "testSetStudyCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int studiesNber = r.nextInt(15) + 1;
        StudyHelper.addStudies(site, name, studiesNber);

        List<StudyWrapper> studies = site.getStudyCollection();
        StudyWrapper study = StudyHelper.newStudy(site, name + "newStudy");
        studies.add(study);
        site.setStudyCollection(studies);
        site.persist();

        site.reload();
        // one study added
        Assert.assertEquals(studiesNber + 1, site.getStudyCollection().size());
    }

    @Test
    public void testSetStudyCollectionRemove() throws Exception {
        String name = "testSetStudyCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int studiesNber = r.nextInt(15) + 1;
        StudyHelper.addStudies(site, name, studiesNber);

        List<StudyWrapper> studies = site.getStudyCollection();
        StudyWrapper study = DbHelper.chooseRandomlyInList(studies);
        int idStudy = study.getId();
        studies.remove(study);
        site.setStudyCollection(studies);

        try {
            site.persist();
            Assert.fail("a study is missing and is not deleted");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        study.delete();
        site.persist();

        site.reload();
        // one study removed
        Assert.assertEquals(studiesNber - 1, site.getStudyCollection().size());

        // study should not be anymore in the study collection (removed the
        // good one)
        for (StudyWrapper s : site.getStudyCollection()) {
            Assert.assertFalse(s.getId().equals(idStudy));
        }
    }

    @Test
    public void testGetClinicCollection() throws Exception {
        String name = "testGetClinicCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int clinicsNber = r.nextInt(15) + 1;
        ClinicHelper.addClinics(site, name, clinicsNber);

        List<ClinicWrapper> clinics = site.getClinicCollection();
        int sizeFound = clinics.size();

        Assert.assertEquals(clinicsNber, sizeFound);
    }

    @Test
    public void testGetClinicCollectionBoolean() throws Exception {
        String name = "testGetClinicCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = r.nextInt(15) + 5;
        ClinicHelper.addClinics(site, name, nber);

        List<ClinicWrapper> clinics = site.getClinicCollection(true);
        if (clinics.size() > 1) {
            for (int i = 0; i < clinics.size() - 1; i++) {
                ClinicWrapper clinic1 = clinics.get(i);
                ClinicWrapper clinic2 = clinics.get(i + 1);
                Assert.assertTrue(clinic1.compareTo(clinic2) <= 0);
            }
        }
    }

    @Test
    public void testSetClinicCollectionAdd() throws Exception {
        String name = "testSetClinicCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = r.nextInt(15) + 1;
        ClinicHelper.addClinics(site, name, nber);

        List<ClinicWrapper> clinics = site.getClinicCollection();
        ClinicWrapper clinic = ClinicHelper.newClinic(site, name + "newClinic");
        clinics.add(clinic);
        site.setClinicCollection(clinics);
        site.persist();

        site.reload();
        // one clinic added
        Assert.assertEquals(nber + 1, site.getClinicCollection().size());
    }

    @Test
    public void testSetClinicCollectionRemove() throws Exception {
        String name = "testSetClinicCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = r.nextInt(15) + 1;
        ClinicHelper.addClinics(site, name, nber);

        List<ClinicWrapper> clinics = site.getClinicCollection();
        ClinicWrapper clinic = DbHelper.chooseRandomlyInList(clinics);
        int idClinic = clinic.getId();
        clinics.remove(clinic);
        site.setClinicCollection(clinics);

        try {
            site.persist();
            Assert.fail("a clinic is missing and is not deleted");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        clinic.delete();
        site.persist();
        site.reload();
        // one clinic removed
        Assert.assertEquals(nber - 1, site.getClinicCollection().size());

        // clinic should not be anymore in the clinic collection (removed
        // the good one)
        for (ClinicWrapper c : site.getClinicCollection()) {
            Assert.assertFalse(c.getId().equals(idClinic));
        }
    }

    @Test
    public void testGetContainerTypeCollection() throws Exception {
        String name = "testGetContainerTypeCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = r.nextInt(15) + 1;
        ContainerTypeHelper.addContainerTypesRandom(site, name, nber);

        List<ContainerTypeWrapper> types = site.getContainerTypeCollection();
        int sizeFound = types.size();

        Assert.assertEquals(nber, sizeFound);
    }

    @Test
    public void testGetContainerTypeCollectionBoolean() throws Exception {
        String name = "testGetContainerTypeCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ContainerTypeHelper.addContainerTypesRandom(site, name,
            r.nextInt(15) + 5);

        List<ContainerTypeWrapper> types = site
            .getContainerTypeCollection(true);
        if (types.size() > 1) {
            for (int i = 0; i < types.size() - 1; i++) {
                ContainerTypeWrapper type1 = types.get(i);
                ContainerTypeWrapper type2 = types.get(i + 1);
                Assert.assertTrue(type1.compareTo(type2) <= 0);
            }
        }
    }

    @Test
    public void testSetContainerTypeCollectionAdd() throws Exception {
        String name = "testSetContainerTypeCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = r.nextInt(15) + 1;
        ContainerTypeHelper.addContainerTypesRandom(site, name, nber);

        List<ContainerTypeWrapper> types = site.getContainerTypeCollection();
        ContainerTypeWrapper type = ContainerTypeHelper.newContainerType(site,
            name + "newType", name, null, 5, 4, false);
        types.add(type);
        site.setContainerTypeCollection(types);
        site.persist();

        site.reload();
        // one type added
        Assert.assertEquals(nber + 1, site.getContainerTypeCollection().size());
    }

    @Test
    public void testSetContainerTypeCollectionRemove() throws Exception {
        String name = "testSetContainerTypeCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = r.nextInt(15) + 1;
        ContainerTypeHelper.addContainerTypesRandom(site, name, nber);

        List<ContainerTypeWrapper> types = site.getContainerTypeCollection();
        ContainerTypeWrapper type = DbHelper.chooseRandomlyInList(types);
        int idType = type.getId();
        types.remove(type);
        site.setContainerTypeCollection(types);

        try {
            site.persist();
            Assert.fail("a containertype is missing and is not deleted");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        type.delete();
        site.persist();

        site.reload();
        // one type removed
        Assert.assertEquals(nber - 1, site.getContainerTypeCollection().size());

        // type should not be anymore in the type collection (removed
        // the good one)
        for (ContainerTypeWrapper t : site.getContainerTypeCollection()) {
            Assert.assertFalse(t.getId().equals(idType));
        }
    }

    @Test
    public void testGetContainerCollection() throws Exception {
        String name = "testGetContainerCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int totalContainers = ContainerHelper.addTopContainersWithChildren(
            site, name, r.nextInt(3) + 1);

        List<ContainerWrapper> containers = site.getContainerCollection();
        int sizeFound = containers.size();

        Assert.assertEquals(totalContainers, sizeFound);
    }

    @Test
    public void testSetContainerCollectionAdd() throws Exception {
        String name = "testSetContainerCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int totalContainers = ContainerHelper.addTopContainersWithChildren(
            site, name, r.nextInt(3) + 1);

        List<ContainerWrapper> containers = site.getContainerCollection();
        ContainerTypeWrapper type = ContainerTypeHelper.addContainerTypeRandom(
            site, name);
        ContainerWrapper container = ContainerHelper.newContainer(null, name
            + "newContainer", null, site, type);
        containers.add(container);
        site.setContainerCollection(containers);
        site.persist();

        site.reload();
        // one container added
        Assert.assertEquals(totalContainers + 1, site.getContainerCollection()
            .size());
    }

    @Test
    public void testSetContainerCollectionRemove() throws Exception {
        String name = "testSetContainerCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int totalNber = ContainerHelper.addTopContainersWithChildren(site,
            name, r.nextInt(3) + 1);

        List<ContainerWrapper> containers = site.getContainerCollection();
        List<ContainerWrapper> containersToChoose = new ArrayList<ContainerWrapper>(
            containers);
        containersToChoose.removeAll(site.getTopContainerCollection());
        ContainerWrapper container = DbHelper
            .chooseRandomlyInList(containersToChoose);
        int idContainer = container.getId();
        containers.remove(container);
        site.setContainerCollection(containers);

        try {
            site.persist();
            Assert.fail("a container is missing and is not deleted");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        container.delete();
        site.persist();

        site.reload();
        // one container removed
        Assert
            .assertEquals(totalNber - 1, site.getContainerCollection().size());

        // container should not be anymore in the container collection
        // (removed the good one)
        for (ContainerWrapper c : site.getContainerCollection()) {
            Assert.assertFalse(c.getId().equals(idContainer));
        }
    }

    @Test
    public void testGetSampleTypeCollection() throws Exception {
        String name = "testGetSampleTypeCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = SampleTypeHelper.addSampleTypes(site, name);

        List<SampleTypeWrapper> types = site.getSampleTypeCollection();
        int sizeFound = types.size();

        Assert.assertEquals(nber, sizeFound);
    }

    @Test
    public void testGetSampleTypeCollectionBoolean() throws Exception {
        String name = "testGetSampleTypeCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        SampleTypeHelper.addSampleTypes(site, name);

        List<SampleTypeWrapper> types = site.getSampleTypeCollection(true);
        if (types.size() > 1) {
            for (int i = 0; i < types.size() - 1; i++) {
                SampleTypeWrapper type1 = types.get(i);
                SampleTypeWrapper type2 = types.get(i + 1);
                Assert.assertTrue(type1.compareTo(type2) <= 0);
            }
        }
    }

    @Test
    public void testSetSampleTypeCollectionAdd() throws Exception {
        String name = "testSetSampleTypeCollectionAdd" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = SampleTypeHelper.addSampleTypes(site, name);

        List<SampleTypeWrapper> types = site.getSampleTypeCollection();
        SampleTypeWrapper type = SampleTypeHelper.newSampleType(site, name);
        types.add(type);
        site.setSampleTypeCollection(types);
        site.persist();

        site.reload();
        // one container added
        Assert.assertEquals(nber + 1, site.getSampleTypeCollection().size());
    }

    @Test
    public void testSetSampleTypeCollectionRemove() throws Exception {
        String name = "testSetSampleTypeCollectionRemove" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = SampleTypeHelper.addSampleTypes(site, name);

        List<SampleTypeWrapper> types = site.getSampleTypeCollection();
        SampleTypeWrapper type = DbHelper.chooseRandomlyInList(types);
        int idContainer = type.getId();
        types.remove(type);
        site.setSampleTypeCollection(types);
        // don't need to delete the type, thanks to method
        // deleteSampleTypeDifference of persistDependencies
        SampleTypeHelper.removeFromCreated(type);
        site.persist();

        site.reload();
        // one type removed
        Assert.assertEquals(nber - 1, site.getSampleTypeCollection().size());

        // type should not be anymore in the type collection
        // (removed the good one)
        for (SampleTypeWrapper t : site.getSampleTypeCollection()) {
            Assert.assertFalse(t.getId().equals(idContainer));
        }
    }

    @Test
    public void testGetAllSampleTypeCollection() throws Exception {
        String name = "testGetAllSampleTypeCollection" + r.nextInt();
        List<SampleType> types = appService.search(SampleType.class,
            new SampleType());
        SiteWrapper site = SiteHelper.addSite(name);
        int nber = SampleTypeHelper.addSampleTypes(site, name);

        site.reload();
        List<SampleTypeWrapper> all = site.getAllSampleTypeCollection();
        Assert.assertEquals(nber + types.size(), all.size());
    }

    @Test
    public void testGetAllSampleTypeCollectionBoolean() throws Exception {
        String name = "testGetAllSampleTypeCollectionBoolean" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        SampleTypeHelper.addSampleTypes(site, name);

        List<SampleTypeWrapper> types = site.getAllSampleTypeCollection(true);
        if (types.size() > 1) {
            for (int i = 0; i < types.size() - 1; i++) {
                SampleTypeWrapper type1 = types.get(i);
                SampleTypeWrapper type2 = types.get(i + 1);
                Assert.assertTrue(type1.compareTo(type2) <= 0);
            }
        }
    }

    @Test
    public void testPersist() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        SiteHelper.addSite("testPersist" + r.nextInt());
        int newTotal = SiteWrapper.getSites(appService).size();
        Assert.assertEquals(oldTotal + 1, newTotal);
    }

    @Test
    public void testPersistFailNoAddress() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        String name = "testPersistFailNoAddress" + r.nextInt();
        SiteWrapper site = new SiteWrapper(appService);
        site.setName(name);

        try {
            site.persist();
            Assert.fail("Should not insert the site : no address");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        site.setCity("Vesoul");
        SiteHelper.createdSites.add(site);
        site.persist();
        int newTotal = SiteWrapper.getSites(appService).size();
        Assert.assertEquals(oldTotal + 1, newTotal);
    }

    @Test
    public void testPersistFailNameUnique() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        String name = "testPersistFailNameUnique" + r.nextInt();
        SiteWrapper site = SiteHelper.newSite(name);
        site = SiteHelper.addSite(name);
        site.persist();

        SiteWrapper site2 = SiteHelper.newSite(name);
        try {
            site2.persist();
            Assert
                .fail("Should not insert the site : same name already in database");
        } catch (BiobankCheckException bce) {
            Assert.assertTrue(true);
        }

        site.setName("Other Name");
        site.persist();
        int newTotal = SiteWrapper.getSites(appService).size();
        Assert.assertEquals(oldTotal + 1, newTotal);
    }

    @Test
    public void testDelete() throws Exception {
        SiteWrapper site = SiteHelper
            .addSite("testDelete" + r.nextInt(), false);

        // object is in database
        Site siteInDB = ModelUtils.getObjectWithId(appService, Site.class, site
            .getId());
        Assert.assertNotNull(siteInDB);

        site.delete();

        siteInDB = ModelUtils.getObjectWithId(appService, Site.class, site
            .getId());
        // object is not anymore in database
        Assert.assertNull(siteInDB);
    }

    @Test
    public void testDeleteFailNoMoreStudy() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        String name = "testDeleteFailNoMoreStudy" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name, false);

        StudyWrapper study = StudyHelper.addStudy(site, name);
        site.reload();

        try {
            site.delete();
            Assert.fail("Should not delete the site : a study is still there");
        } catch (BiobankCheckException bce) {
            Assert.assertEquals(oldTotal + 1, SiteWrapper.getSites(appService)
                .size());
        }
        study.delete();
        site.reload();
        site.delete();
        Assert.assertEquals(oldTotal, SiteWrapper.getSites(appService).size());
    }

    @Test
    public void testDeleteFailNoMoreClinic() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        String name = "testDeleteFailNoMoreClinic" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name, false);

        ClinicWrapper clinic = ClinicHelper.addClinic(site, name);
        site.reload();

        try {
            site.delete();
            Assert.fail("Should not delete the site : a clinic is still there");
        } catch (BiobankCheckException bce) {
            Assert.assertEquals(oldTotal + 1, SiteWrapper.getSites(appService)
                .size());
        }
        clinic.delete();
        site.reload();
        site.delete();
        Assert.assertEquals(oldTotal, SiteWrapper.getSites(appService).size());
    }

    @Test
    public void testDeleteFailNoMoreContainerType() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        String name = "testDeleteFailNoMoreContainerType" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name, false);

        ContainerTypeWrapper type = ContainerTypeHelper.addContainerType(site,
            name, name, 1, 2, 3, false);
        site.reload();

        try {
            site.delete();
            Assert
                .fail("Should not delete the site : a container type is still there");
        } catch (BiobankCheckException bce) {
            Assert.assertEquals(oldTotal + 1, SiteWrapper.getSites(appService)
                .size());
        }
        type.delete();
        site.reload();
        site.delete();
        Assert.assertEquals(oldTotal, SiteWrapper.getSites(appService).size());
    }

    @Test
    public void testDeleteFailNoMoreContainer() throws Exception {
        int oldTotal = SiteWrapper.getSites(appService).size();
        String name = "testDeleteFailNoMoreContainer" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name, false);

        ContainerWrapper container = ContainerHelper.addContainerRandom(site,
            name, null);
        site.reload();

        try {
            site.delete();
            Assert
                .fail("Should not delete the site : a container and a container type is still there");
        } catch (BiobankCheckException bce) {
            Assert.assertEquals(oldTotal + 1, SiteWrapper.getSites(appService)
                .size());
        }
        ContainerTypeWrapper type = container.getContainerType();
        container.delete();
        type.delete();
        site.reload();
        site.delete();
        Assert.assertEquals(oldTotal, SiteWrapper.getSites(appService).size());
    }

    @Test
    public void testResetAlreadyInDatabase() throws Exception {
        SiteWrapper site = SiteHelper.addSite("testResetAlreadyInDatabase"
            + r.nextInt());
        site.reload();
        String oldName = site.getName();
        site.setName("toto");
        site.reset();
        Assert.assertEquals(oldName, site.getName());
    }

    @Test
    public void testResetNew() throws Exception {
        SiteWrapper newSite = new SiteWrapper(appService);
        newSite.setName("titi");
        newSite.reset();
        Assert.assertEquals(null, newSite.getName());
    }

    @Test
    public void testGetSetSitePvAttr() throws Exception {
        String name = "testGetSetSitePvAttr" + r.nextInt();

        SiteWrapper site = SiteHelper.addSite(name);

        SiteWrapper site2 = SiteHelper.addSite(name + "_secondSite");
        List<String> types = SiteWrapper.getPvAttrTypeNames(appService);
        if (types.size() == 0) {
            Assert.fail("Can't test without PvAttrTypes");
        }

        String type = types.get(r.nextInt(types.size()));
        site2.setSitePvAttr(name, type);
        site2.persist();

        site2.reload();
        Assert.assertTrue(Arrays.asList(site2.getSitePvAttrLabels()).contains(
            name));
        Assert.assertEquals(type, site2.getSitePvAttrTypeName(name));
        Assert.assertFalse(Arrays.asList(site.getSitePvAttrLabels()).contains(
            name));

        // set non existing type, expect exception
        try {
            site.setSitePvAttr(Utils.getRandomString(10, 15), Utils
                .getRandomString(10, 15));
            Assert.fail("call should generate an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        // delete non existing label, expect exception
        try {
            site.deleteSitePvAttr(Utils.getRandomString(10, 15));
            Assert.fail("call should generate an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testRemoveSitePvAttr() throws Exception {
        String name = "testRemoveSitePvAttr" + r.nextInt();

        SiteWrapper site = SiteHelper.addSite(name);

        int sizeOrig = site.getSitePvAttrLabels().length;
        List<String> types = SiteWrapper.getPvAttrTypeNames(appService);
        if (types.size() < 2) {
            Assert.fail("Can't test without PvAttrTypes");
        }
        site.setSitePvAttr(name, types.get(0));
        site.setSitePvAttr(name + "_2", types.get(1));
        site.persist();

        site.reload();
        Assert.assertEquals(sizeOrig + 2, site.getSitePvAttrLabels().length);
        site.deleteSitePvAttr(name);
        Assert.assertEquals(sizeOrig + 1, site.getSitePvAttrLabels().length);
        site.persist();

        site.reload();
        Assert.assertEquals(sizeOrig + 1, site.getSitePvAttrLabels().length);
    }

    @Test
    public void testGetSitePvAttrType() throws Exception {
        String name = "testGetSitePvAttrType" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);

        List<PvAttrTypeWrapper> types = PvAttrTypeWrapper
            .getAllWrappers(appService);
        if (types.size() == 0) {
            Assert.fail("Can't test without PvAttrTypes");
        }
        PvAttrTypeWrapper type = types.get(0);
        String label = "toto";
        site.setSitePvAttr(label, type.getName());

        // my guess would be that the sitePvAttr map inside the site should
        // be updated in the setSitePvAttr method
        Assert.assertEquals(type.getId(), site.getSitePvAttrType(label));

        site.persist();
        site.reload();
        Assert.assertEquals(type.getId(), site.getSitePvAttrType(label));

        // get type for non existing label, expect exception
        try {
            site.getSitePvAttrType(Utils.getRandomString(10, 15));
            Assert.fail("call should generate an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetSitePvAttrTypeName() throws Exception {
        String name = "testGetSitePvAttrTypeName" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);

        List<PvAttrTypeWrapper> types = PvAttrTypeWrapper
            .getAllWrappers(appService);
        if (types.size() == 0) {
            Assert.fail("Can't test without PvAttrTypes");
        }
        PvAttrTypeWrapper type = types.get(0);
        String label = "toto";
        site.setSitePvAttr(label, type.getName());

        Assert.assertEquals(type.getName(), site.getSitePvAttrTypeName(label));

        site.persist();
        site.reload();
        Assert.assertEquals(type.getName(), site.getSitePvAttrTypeName(label));
    }

    @Test
    public void testSetSitePvAttrTypeNames() throws Exception {
        String name = "testGetSitePvAttrTypeNames" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);

        List<String> types = SiteWrapper.getPvAttrTypeNames(appService);
        Assert.assertTrue("No PvAttrTypes", types.size() > 0);
        int count = 1;
        for (String type : types) {
            site.setSitePvAttr(name + count, type);
            ++count;
        }
    }

    @Test
    public void testGetTopContainerCollection() throws Exception {
        String name = "testGetTopContainerCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        int topNber = r.nextInt(8) + 1;
        ContainerHelper.addTopContainersWithChildren(site, name, topNber);

        List<ContainerWrapper> containers = site.getTopContainerCollection();
        int sizeFound = containers.size();

        Assert.assertEquals(topNber, sizeFound);
    }

    @Test
    public void testGetTopContainerCollectionBoolean() throws Exception {
        String name = "testGetTopContainerCollection" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite(name);
        ContainerHelper.addTopContainersWithChildren(site, name,
            r.nextInt(8) + 5);

        List<ContainerWrapper> containers = site
            .getTopContainerCollection(true);
        if (containers.size() > 1) {
            for (int i = 0; i < containers.size() - 1; i++) {
                ContainerWrapper container1 = containers.get(i);
                ContainerWrapper containter2 = containers.get(i + 1);
                Assert.assertTrue(container1.compareTo(containter2) <= 0);
            }
        }
    }

    @Test
    public void testGetSites() throws Exception {
        List<Site> sitesDBBefore = appService.search(Site.class, new Site());
        int nberSite = r.nextInt(15) + 1;
        SiteHelper.addSites("testGetSites" + r.nextInt(), nberSite);

        List<SiteWrapper> siteWrappers = SiteWrapper.getSites(appService);
        List<Site> sitesDB = appService.search(Site.class, new Site());
        int nberAddedInDB = sitesDB.size() - sitesDBBefore.size();
        Assert.assertEquals(nberSite, nberAddedInDB);
        Assert.assertEquals(siteWrappers.size(), siteWrappers.size());

        Site site = DbHelper.chooseRandomlyInList(sitesDB);
        siteWrappers = SiteWrapper.getSites(appService, site.getId());
        Assert.assertEquals(1, siteWrappers.size());

        HQLCriteria criteria = new HQLCriteria("select max(id) from "
            + Site.class.getName());
        List<Integer> max = appService.query(criteria);
        siteWrappers = SiteWrapper.getSites(appService, max.get(0) + 1000);
        Assert.assertEquals(0, siteWrappers.size());
    }

    @Test
    public void testCompareTo() throws Exception {
        String name = "testCompareTo" + r.nextInt();
        SiteWrapper site = SiteHelper.addSite("QWERTY" + name);
        SiteWrapper site2 = SiteHelper.addSite("ASDFG" + name);

        Assert.assertTrue(site.compareTo(site2) > 0);
        Assert.assertTrue(site2.compareTo(site) < 0);
    }

}
