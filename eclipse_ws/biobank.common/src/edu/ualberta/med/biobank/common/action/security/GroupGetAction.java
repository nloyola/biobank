package edu.ualberta.med.biobank.common.action.security;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.security.UserManagerPermission;
import edu.ualberta.med.biobank.model.Group;

public class GroupGetAction implements Action<GroupGetOutput> {
    private static final long serialVersionUID = 1L;
    private static final Permission PERMISSION = new UserManagerPermission();

    private final GroupGetInput input;

    public GroupGetAction(GroupGetInput input) {
        this.input = input;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return PERMISSION.isAllowed(context);
    }

    @Override
    public GroupGetOutput run(ActionContext context) throws ActionException {
        Criteria c = context.getSession()
            .createCriteria(Group.class, "g")
            .createAlias("g.memberships", "m", Criteria.LEFT_JOIN)
            .createAlias("g.users", "u", Criteria.LEFT_JOIN)
            .createAlias("m.center", "c", Criteria.LEFT_JOIN)
            .createAlias("m.study", "s", Criteria.LEFT_JOIN)
            .createAlias("m.roles", "r", Criteria.LEFT_JOIN)
            .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            .add(Restrictions.idEq(input.getGroupId()));

        Group group = (Group) c.uniqueResult();

        ManagerContext managerContext = new ManagerContextGetAction(
            new ManagerContextGetInput()).run(context).getContext();

        return new GroupGetOutput(group, managerContext);
    }
}
