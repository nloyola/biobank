package edu.ualberta.med.biobank.test.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import junit.framework.Assert;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;

import edu.ualberta.med.biobank.common.action.container.ContainerGetInfoAction;
import edu.ualberta.med.biobank.common.action.container.ContainerGetInfoAction.ContainerInfo;
import edu.ualberta.med.biobank.common.action.container.ContainerSaveAction;
import edu.ualberta.med.biobank.common.action.containerType.ContainerTypeDeleteAction;
import edu.ualberta.med.biobank.common.action.containerType.ContainerTypeGetInfoAction;
import edu.ualberta.med.biobank.common.action.containerType.ContainerTypeGetInfoAction.ContainerTypeInfo;
import edu.ualberta.med.biobank.common.action.containerType.ContainerTypeSaveAction;
import edu.ualberta.med.biobank.common.util.HibernateUtil;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerLabelingScheme;
import edu.ualberta.med.biobank.model.ContainerPosition;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.action.helper.ContainerTypeHelper;
import edu.ualberta.med.biobank.test.action.helper.SiteHelper;

public class TestContainerType extends TestAction {

    private String name;

    private Integer siteId;

    private ContainerTypeSaveAction containerTypeSaveAction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        name = getMethodNameR();

        siteId = EXECUTOR.exec(SiteHelper.getSaveAction(
            name, name, ActivityStatus.ACTIVE)).getId();

        containerTypeSaveAction = ContainerTypeHelper.getSaveAction(
            "FREEZER_3x10", "FR3x10", siteId, true, 3, 10,
            getContainerLabelingSchemes().get("CBSR 2 char alphabetic")
                .getId(), R.nextDouble());
    }

    @Test
    public void saveNew() throws Exception {
        containerTypeSaveAction.setName(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with no name");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        containerTypeSaveAction.setName(name);
        containerTypeSaveAction.setNameShort(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with no name short");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        containerTypeSaveAction.setNameShort(name);
        containerTypeSaveAction.setSiteId(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with no site");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        containerTypeSaveAction.setSiteId(siteId);
        containerTypeSaveAction.setRowCapacity(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with null for row capacity");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        containerTypeSaveAction.setRowCapacity(3);
        containerTypeSaveAction.setColCapacity(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with null for column capacity");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        containerTypeSaveAction.setColCapacity(10);
        containerTypeSaveAction.setChildLabelingSchemeId(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with null for child labeling scheme");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        containerTypeSaveAction
            .setChildLabelingSchemeId(getContainerLabelingSchemes().get(
                "CBSR 2 char alphabetic")
                .getId());
        containerTypeSaveAction.setActivityStatus(null);
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add container type with null for activity status");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        // test success path
        containerTypeSaveAction.setActivityStatus(ActivityStatus.ACTIVE);
        EXECUTOR.exec(containerTypeSaveAction);
    }

    @Test
    public void checkGetAction() throws Exception {
        Integer containerTypeId =
            EXECUTOR.exec(containerTypeSaveAction).getId();
        ContainerTypeInfo topContainerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));

        Assert.assertEquals("FREEZER_3x10",
            topContainerTypeInfo.getContainerType().getName());
        Assert.assertEquals(ActivityStatus.ACTIVE, topContainerTypeInfo
            .getContainerType()
            .getActivityStatus());
        Assert.assertEquals(3, topContainerTypeInfo.getContainerType()
            .getCapacity().getRowCapacity().intValue());
        Assert.assertEquals(10, topContainerTypeInfo.getContainerType()
            .getCapacity().getColCapacity().intValue());
        Assert.assertEquals(0, topContainerTypeInfo.getContainerType()
            .getChildContainerTypes().size());
        Assert.assertEquals(0, topContainerTypeInfo.getContainerType()
            .getComments().size());

        containerTypeSaveAction =
            ContainerTypeHelper.getSaveAction(
                "HOTEL13", "H13", siteId, false, 13, 1,
                getContainerLabelingSchemes().get("2 char numeric").getId(),
                R.nextDouble());
        EXECUTOR.exec(containerTypeSaveAction).getId();
        topContainerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));

    }

    @Test
    public void nameChecks() throws Exception {
        // ensure we can change name on existing container type
        Integer containerTypeId =
            EXECUTOR.exec(containerTypeSaveAction).getId();

        containerTypeSaveAction =
            ContainerTypeHelper.getSaveAction(EXECUTOR
                .exec(new ContainerTypeGetInfoAction(containerTypeId)));
        containerTypeSaveAction.setName("FREEZER_4x12");
        EXECUTOR.exec(containerTypeSaveAction);

        containerTypeSaveAction =
            ContainerTypeHelper.getSaveAction(EXECUTOR
                .exec(new ContainerTypeGetInfoAction(containerTypeId)));
        containerTypeSaveAction.setNameShort("FR4x12");
        EXECUTOR.exec(containerTypeSaveAction);

        // test for duplicate name

        containerTypeSaveAction = ContainerTypeHelper.getSaveAction(
            "FREEZER_4x12", "FR5x10", siteId, true, 3, 10,
            getContainerLabelingSchemes().get("CBSR 2 char alphabetic")
                .getId(), R.nextDouble());
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add a second container type with same name");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }

        // test for duplicate name short
        containerTypeSaveAction.setName("FREEZER_5x10");
        containerTypeSaveAction.setNameShort("FR4x12");
        try {
            EXECUTOR.exec(containerTypeSaveAction);
            Assert
                .fail("should not be allowed to add a second container type with same name");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void specimenTypeCollection() throws Exception {
        // get random sample types
        Set<Integer> ctSpecimenTypeIds = new HashSet<Integer>();
        Set<Integer> set1 = new HashSet<Integer>();
        Set<Integer> set2 = new HashSet<Integer>();
        List<SpecimenType> allSpecimenTypes = getSpecimenTypes();
        Assert.assertTrue(allSpecimenTypes.size() > 20);
        Collections.shuffle(allSpecimenTypes);

        ContainerTypeInfo containerTypeInfo;
        Integer containerTypeId;

        // add specimens types one by one
        for (int i = 0; i < 10; ++i) {
            ctSpecimenTypeIds.add(allSpecimenTypes.get(i).getId());
            set1.add(allSpecimenTypes.get(i).getId());

            containerTypeSaveAction.setSpecimenTypeIds(ctSpecimenTypeIds);
            containerTypeId = EXECUTOR.exec(containerTypeSaveAction).getId();
            containerTypeInfo =
                EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
            Assert.assertEquals(ctSpecimenTypeIds,
                getSpecimenTypeIds(containerTypeInfo));
            containerTypeSaveAction =
                ContainerTypeHelper.getSaveAction(containerTypeInfo);
        }

        // add another 10 specimen types all at once
        for (int i = 0; i < 10; ++i) {
            ctSpecimenTypeIds.add(allSpecimenTypes.get(i + 10).getId());
            set2.add(allSpecimenTypes.get(i + 10).getId());
        }

        containerTypeSaveAction.setSpecimenTypeIds(ctSpecimenTypeIds);
        containerTypeId = EXECUTOR.exec(containerTypeSaveAction).getId();
        containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        Assert.assertEquals(ctSpecimenTypeIds,
            getSpecimenTypeIds(containerTypeInfo));

        // remove set1 one by one
        for (Integer specimenTypeId : set1) {
            ctSpecimenTypeIds.remove(specimenTypeId);
            containerTypeSaveAction.setSpecimenTypeIds(ctSpecimenTypeIds);
            EXECUTOR.exec(containerTypeSaveAction);
            containerTypeInfo =
                EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
            Assert.assertEquals(ctSpecimenTypeIds,
                getSpecimenTypeIds(containerTypeInfo));
        }

        // remove set2 all at once
        ctSpecimenTypeIds.removeAll(set2);
        containerTypeSaveAction.setSpecimenTypeIds(ctSpecimenTypeIds);
        EXECUTOR.exec(containerTypeSaveAction);
        containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        Assert.assertEquals(0, containerTypeInfo.getContainerType()
            .getSpecimenTypes().size());
    }

    private Set<Integer> getSpecimenTypeIds(ContainerTypeInfo containerTypeInfo) {
        HashSet<Integer> result = new HashSet<Integer>();
        for (SpecimenType specimenType : containerTypeInfo.getContainerType()
            .getSpecimenTypes()) {
            result.add(specimenType.getId());
        }
        return result;
    }

    @Test
    public void childContainerTypeCollection() throws Exception {
        // get random sample types
        Set<Integer> ctContainerTypeIds = new HashSet<Integer>();
        Set<Integer> set1 = new HashSet<Integer>();
        Set<Integer> set2 = new HashSet<Integer>();
        List<ContainerType> allContainerTypes = createChildContainerTypes();
        Assert.assertEquals(4, allContainerTypes.size());
        // Collections.shuffle(allContainerTypes);

        ContainerTypeInfo containerTypeInfo;
        Integer containerTypeId;

        // add container types one by one
        for (int i = 0; i < 2; ++i) {
            ctContainerTypeIds.add(allContainerTypes.get(i).getId());
            set1.add(allContainerTypes.get(i).getId());

            containerTypeSaveAction
                .setChildContainerTypeIds(ctContainerTypeIds);
            containerTypeId = EXECUTOR.exec(containerTypeSaveAction).getId();
            containerTypeInfo =
                EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
            Assert.assertEquals(ctContainerTypeIds,
                getChildContainerTypeIds(containerTypeInfo));
            containerTypeSaveAction =
                ContainerTypeHelper.getSaveAction(containerTypeInfo);
        }

        // add another 2 container types all at once
        for (int i = 0; i < 2; ++i) {
            ctContainerTypeIds.add(allContainerTypes.get(i + 2).getId());
            set2.add(allContainerTypes.get(i + 2).getId());
        }

        containerTypeSaveAction.setChildContainerTypeIds(ctContainerTypeIds);
        containerTypeId = EXECUTOR.exec(containerTypeSaveAction).getId();
        containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        Assert.assertEquals(ctContainerTypeIds,
            getChildContainerTypeIds(containerTypeInfo));

        // remove set1 one by one
        for (Integer specimenTypeId : set1) {
            ctContainerTypeIds.remove(specimenTypeId);
            containerTypeSaveAction
                .setChildContainerTypeIds(ctContainerTypeIds);
            EXECUTOR.exec(containerTypeSaveAction);
            containerTypeInfo =
                EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
            Assert.assertEquals(ctContainerTypeIds,
                getChildContainerTypeIds(containerTypeInfo));
        }

        // remove set2 all at once
        ctContainerTypeIds.removeAll(set2);
        containerTypeSaveAction.setChildContainerTypeIds(ctContainerTypeIds);
        EXECUTOR.exec(containerTypeSaveAction);
        containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        Assert.assertEquals(0, containerTypeInfo.getContainerType()
            .getChildContainerTypes().size());
    }

    private Set<Integer> getChildContainerTypeIds(
        ContainerTypeInfo containerTypeInfo) {
        HashSet<Integer> result = new HashSet<Integer>();
        for (ContainerType childContainerType : containerTypeInfo
            .getContainerType()
            .getChildContainerTypes()) {
            result.add(childContainerType.getId());
        }
        return result;
    }

    /*
     * Creates 4 child containers for the top level container
     */
    private List<ContainerType> createChildContainerTypes() {
        List<ContainerType> result = new ArrayList<ContainerType>();

        ContainerTypeSaveAction hotelCtSaveAction =
            ContainerTypeHelper.getSaveAction(
                "HOTEL12", "H12", siteId, false, 12, 1,
                getContainerLabelingSchemes().get("2 char numeric").getId(),
                R.nextDouble());
        Integer ctId = EXECUTOR.exec(hotelCtSaveAction).getId();
        ContainerTypeInfo containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(ctId));
        result.add(containerTypeInfo.getContainerType());

        hotelCtSaveAction =
            ContainerTypeHelper.getSaveAction(
                "HOTEL13", "H13", siteId, false, 13, 1,
                getContainerLabelingSchemes().get("2 char numeric").getId(),
                R.nextDouble());
        ctId = EXECUTOR.exec(hotelCtSaveAction).getId();
        containerTypeInfo = EXECUTOR.exec(new ContainerTypeGetInfoAction(ctId));
        result.add(containerTypeInfo.getContainerType());

        hotelCtSaveAction =
            ContainerTypeHelper.getSaveAction(
                "HOTEL18", "H18", siteId, false, 18, 1,
                getContainerLabelingSchemes().get("2 char numeric").getId(),
                R.nextDouble());
        ctId = EXECUTOR.exec(hotelCtSaveAction).getId();
        containerTypeInfo = EXECUTOR.exec(new ContainerTypeGetInfoAction(ctId));
        result.add(containerTypeInfo.getContainerType());

        hotelCtSaveAction =
            ContainerTypeHelper.getSaveAction(
                "HOTEL19", "H19", siteId, false, 19, 1,
                getContainerLabelingSchemes().get("2 char numeric").getId(),
                R.nextDouble());
        ctId = EXECUTOR.exec(hotelCtSaveAction).getId();
        containerTypeInfo = EXECUTOR.exec(new ContainerTypeGetInfoAction(ctId));
        result.add(containerTypeInfo.getContainerType());

        return result;
    }

    @Test
    public void comments() {
        // save with no comments
        Integer containerTypeId =
            EXECUTOR.exec(containerTypeSaveAction).getId();
        ContainerTypeInfo containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        Assert.assertEquals(0, containerTypeInfo.getContainerType()
            .getComments().size());

        containerTypeInfo = addComment(containerTypeId);
        Assert.assertEquals(1, containerTypeInfo.getContainerType()
            .getComments().size());

        containerTypeInfo = addComment(containerTypeId);
        Assert.assertEquals(2, containerTypeInfo.getContainerType()
            .getComments().size());

        // TODO: check full name on each comment's user
        // for (Comment comment :
        // containerTypeInfo.containerType.getCommentCollection()) {
        //
        // }

    }

    private Container createTypeWithContainer() {
        Integer containerTypeId =
            EXECUTOR.exec(containerTypeSaveAction).getId();

        ContainerSaveAction containerSaveAction = new ContainerSaveAction();
        containerSaveAction.setActivityStatus(ActivityStatus.ACTIVE);
        containerSaveAction.setBarcode(Utils.getRandomString(5, 10));
        containerSaveAction.setLabel("01");
        containerSaveAction.setSiteId(siteId);
        containerSaveAction.setTypeId(containerTypeId);
        Integer containerId = EXECUTOR.exec(containerSaveAction).getId();
        ContainerInfo containerInfo =
            EXECUTOR.exec(new ContainerGetInfoAction(containerId));

        return containerInfo.container;
    }

    @Test
    public void removeUsedChildContainerType() {
        Transaction tx = session.beginTransaction();

        String methodNameR = getMethodNameR();

        Site site = new Site();

        site.setActivityStatus(ActivityStatus.ACTIVE);
        site.getAddress().setCity(methodNameR);
        site.setName(methodNameR);
        site.setNameShort(methodNameR);

        session.save(site);

        ContainerLabelingScheme sbsScheme = (ContainerLabelingScheme) session
            .createCriteria(ContainerLabelingScheme.class)
            .add(Restrictions.idEq(1))
            .uniqueResult();

        ContainerType ct1 = new ContainerType();
        ct1.setName(methodNameR + "_1");
        ct1.setNameShort(methodNameR + "_1");
        ct1.setActivityStatus(ActivityStatus.ACTIVE);
        ct1.setChildLabelingScheme(sbsScheme);
        ct1.setSite(site);
        ct1.setTopLevel(true);
        ct1.getCapacity().setRowCapacity(5);
        ct1.getCapacity().setColCapacity(5);

        ContainerType ct2 = new ContainerType();
        ct2.setName(methodNameR + "_2");
        ct2.setNameShort(methodNameR + "_2");
        ct2.setActivityStatus(ActivityStatus.ACTIVE);
        ct2.setChildLabelingScheme(sbsScheme);
        ct2.setSite(site);
        ct2.getCapacity().setRowCapacity(5);
        ct2.getCapacity().setColCapacity(5);

        ct1.getChildContainerTypes().add(ct2);

        session.save(ct2);
        session.save(ct1);

        session.flush();

        Container c1 = new Container();
        c1.setLabel(methodNameR + "_1");
        c1.setActivityStatus(ActivityStatus.ACTIVE);
        c1.setContainerType(ct1);
        c1.setSite(site);

        Container c2 = new Container();
        c2.setLabel(methodNameR + "_2");
        c2.setActivityStatus(ActivityStatus.ACTIVE);
        c2.setContainerType(ct2);
        c2.setSite(site);

        ContainerPosition cp = new ContainerPosition();

        cp.setParentContainer(c1);
        cp.setRow(0);
        cp.setCol(0);
        cp.setContainer(c2);
        c2.setPosition(cp);

        session.save(c1);
        session.save(c2);

        try {
            ct1.getChildContainerTypes().clear();
            session.update(ct1);
            tx.commit();

            Assert.fail("cannot remove child container types in use");
        } catch (ConstraintViolationException e) {
        }
    }

    @Test
    public void removeUsedSpecimenType() {
    }

    @Test
    public void changeCapacity() {
        // capacity cannot be changed after on a container type once containers
        // of this type have been created

    }

    @Test
    public void changeTopLevel() {
        // top level cannot be changed after on a container type once containers
        // of this type have been created

    }

    @Test
    public void changeLabellingScheme() {
        // labeling scheme cannot be changed after on a container type once
        // containers of this type have been created

    }

    private ContainerTypeInfo addComment(Integer containerTypeId) {
        ContainerTypeSaveAction containerTypeSaveAction =
            ContainerTypeHelper.getSaveAction(
                EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId)));
        containerTypeSaveAction
            .setCommentMessage(Utils.getRandomString(20, 30));
        EXECUTOR.exec(containerTypeSaveAction).getId();
        return EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
    }

    @Test
    public void delete() {
        // save with no comments
        Integer containerTypeId =
            EXECUTOR.exec(containerTypeSaveAction).getId();
        ContainerTypeInfo containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        EXECUTOR.exec(new ContainerTypeDeleteAction(containerTypeInfo
            .getContainerType()));

        // hql query for container type should return empty
        Query q =
            session.createQuery("SELECT COUNT(*) FROM "
                + ContainerType.class.getName() + " WHERE id=?");
        q.setParameter(0, containerTypeId);
        Long result = HibernateUtil.getCountFromQuery(q);

        Assert.assertTrue(result.equals(0L));
    }

    @Test
    public void deleteWithParent() {
        // create a top container type with children
        Set<Integer> childContainerTypeIds = new HashSet<Integer>();
        for (ContainerType childContainerType : createChildContainerTypes()) {
            childContainerTypeIds.add(childContainerType.getId());
        }

        containerTypeSaveAction.setChildContainerTypeIds(childContainerTypeIds);
        Integer parentCtId = EXECUTOR.exec(containerTypeSaveAction).getId();
        ContainerTypeInfo containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(parentCtId));
        Integer childCtId =
            containerTypeInfo.getContainerType()
                .getChildContainerTypes()
                .iterator().next().getId();

        ContainerTypeInfo childCtInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(childCtId));
        try {
            EXECUTOR.exec(new ContainerTypeDeleteAction(childCtInfo
                .getContainerType()));
            Assert
                .fail(
                "should not be allowed to delete a child container type and linked to a parent type");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void deleteWithChildren() {
        // create a top container type with children
        Set<Integer> childContainerTypeIds = new HashSet<Integer>();
        for (ContainerType childContainerType : createChildContainerTypes()) {
            childContainerTypeIds.add(childContainerType.getId());
        }

        containerTypeSaveAction.setChildContainerTypeIds(childContainerTypeIds);
        Integer parentCtId = EXECUTOR.exec(containerTypeSaveAction).getId();
        EXECUTOR.exec(new ContainerTypeGetInfoAction(parentCtId));

        // FIXME: should this test pass or fail?

        // delete parent type
        ContainerTypeInfo parentCtInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(parentCtId));
        try {
            EXECUTOR.exec(new ContainerTypeDeleteAction(parentCtInfo
                .getContainerType()));
            Assert
                .fail(
                "should not be allowed to delete a parent container type and "
                    + "linked to children types");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void deleteWithContainer() {
        Container container = createTypeWithContainer();
        Integer containerTypeId = container.getContainerType().getId();

        ContainerTypeInfo containerTypeInfo =
            EXECUTOR.exec(new ContainerTypeGetInfoAction(containerTypeId));
        try {
            EXECUTOR.exec(new ContainerTypeDeleteAction(containerTypeInfo
                .getContainerType()));
            Assert
                .fail(
                "should not be allowed to delete a container type in use by a container");
        } catch (ConstraintViolationException e) {
            Assert.assertTrue(true);
        }
    }
}
