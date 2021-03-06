import I18n from "i18n-js";
import {isEmpty} from "../utils/utils";

export const ROLES = {
    ADMIN: {icon: "fa fa-star", name: "admin", role: "ADMIN"},
    OWNER: {icon: "fa fa-universal-access", name: "owner", role: "OWNER"},
    MANAGER: {icon: "fa fa-black-tie", name: "manager", role: "MANAGER"},
    MEMBER: {icon: "fa fa-user", name: "member", role: "MEMBER"},
    SUPER_ADMIN: {name: "super_admin", role: "SUPER_ADMIN"},
    JOIN_REQUEST: {icon: "fa fa-envelope", name: "join_request", role: "JOIN_REQUEST"},
    INVITATION: {icon: "fa fa-clock-o", name: "invitation", role: "INVITATION"}
};

export function labelForRole(role) {
    return I18n.t(`icon_legend.${ROLES[role].name}`);
}

export function allowedToLeave(team, currentUser) {
    const isMember = team.memberships.find(membership => membership.urnPerson === currentUser.urn);
    const admins = team.memberships
        .filter(membership => (membership.role === ROLES.ADMIN.role || membership.role === ROLES.OWNER.role)
            && membership.urnPerson !== currentUser.urn);
    return admins.length > 0 && !isEmpty(isMember) ;
}

export function hasOneAdmin(team, currentUser) {
    const pendingAdminInvitations = (team.invitations || []).filter(invitation => (invitation.intendedRole === ROLES.ADMIN.role ||
        invitation.intendedRole === ROLES.OWNER.role) && !invitation.declined);
    const hasPendingAdminInvitations = pendingAdminInvitations.length > 0;
    const admins = team.memberships
        .filter(membership => (membership.role === ROLES.ADMIN.role || membership.role === ROLES.OWNER.role)
            && membership.urnPerson !== currentUser.urn);
    return admins.length === 0 && !hasPendingAdminInvitations;
}

export function currentUserRoleInTeam(team, currentUser) {
    if (currentUser.superAdminModus) {
        return "SUPER_ADMIN";
    }
    return team.memberships.filter(membership => membership.urnPerson === currentUser.urn)[0].role;
}

export function isOnlyAdmin(team, currentUser) {
    const admins = team.memberships.filter(membership => membership.role === ROLES.ADMIN.role || membership.role === ROLES.OWNER.role);
    const userRoleInTeam = currentUserRoleInTeam(team, currentUser);
    return admins.length === 1 && (userRoleInTeam === ROLES.ADMIN.role || userRoleInTeam === ROLES.OWNER.role);
}

export function iconForRole(role) {
    return ROLES[role].icon;
}
