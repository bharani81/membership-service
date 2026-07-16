package com.membership.factory;

import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;

/**
 * Handler interface for membership lifecycle actions.
 *
 * <p>Each implementation encapsulates the business logic for a single membership action
 * (subscribe, upgrade, downgrade, cancel). The Factory discovers and routes to the
 * correct handler without conditional branching in the service layer.
 */
public interface MembershipActionHandler {

    /**
     * Executes the membership action and returns the updated (or new) membership.
     *
     * @param context all data required to perform the action
     * @return the updated membership entity
     */
    UserMembership handle(MembershipActionContext context);

    /**
     * The action type this handler is responsible for.
     * Used by {@link MembershipActionFactory} to route requests.
     */
    MembershipAction supportedAction();
}
