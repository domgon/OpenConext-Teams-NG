package teams.api.validations;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import teams.domain.*;
import teams.exception.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface InvitationValidator {

    Pattern emailPattern = Pattern.compile("\\S+@\\S+");

    default void validateClientInvitation(ClientInvitation clientInvitation) {
        List<String> emails = clientInvitation.getEmails();
        if (CollectionUtils.isEmpty(emails) && StringUtils.isEmpty(clientInvitation.getCsvEmails())) {
            throw new IllegalInviteException("Either emails or file with emails is required");
        }
    }

    default void validateInvitation(Invitation invitation, Person person) {
        if (invitation.expired()) {
            throw new InvitationExpiredException();
        }
        if (invitation.isAccepted() || invitation.getTeam().member(person.getUrn()).isPresent()) {
            throw new InvitationAlreadyAcceptedException();
        }
        if (invitation.isDeclined()) {
            throw new InvitationAlreadyDeclinedException();
        }
    }

    default void membershipRequired(Team team, Person person) {
        if (team.getMemberships().stream().noneMatch(membership -> membership.getUrnPerson().equals(person.getUrn()))) {
            throw new IllegalInviteException(String.format(
                    "Person %s must be a member of team %s", person.getUrn(), team.getUrn()));
        }
    }

    default void mustBeTeamAdminOrManager(Invitation invitation, FederatedUser federatedUser) {
        Membership membership = invitation.getTeam().member(federatedUser.getUrn()).orElseThrow(() ->
                new NotAllowedException(String.format(
                        "Person %s is a member of team %s", federatedUser.getUrn(), invitation.getTeam().getUrn())));

        if (membership.getRole().equals(Role.MEMBER)) {
            throw new NotAllowedException(String.format(
                    "Person %s is not the inviter of invitation %s", federatedUser.getUrn(), invitation.getId()));
        }
    }

    default Role determineFutureRole(Team team, Person person, Role intendedRole) {
        Optional<Membership> membershipOptional = team.member(person.getUrn());
        return membershipOptional.map(membership -> doDetermineFutureRole(membership.getRole(), intendedRole))
                .orElseThrow(() -> new IllegalInviteException(
                        String.format("Person %s must be a member of team %s", person.getUrn(), team.getUrn())));
    }

    default Role doDetermineFutureRole(Role role, Role intendedRole) {
        switch (role) {
            case ADMIN:
                return intendedRole;
            case MANAGER:
                return Role.MEMBER;
            default:
                throw new IllegalInviteException("Only ADMIN and MANAGER can invite members");
        }
    }

    default List<String> emails(ClientInvitation clientInvitation) throws IOException {
        validateClientInvitation(clientInvitation);
        List<String> fromFile = StringUtils.hasText(clientInvitation.getCsvEmails()) ?
                Stream.of(clientInvitation.getCsvEmails().split(","))
                        .filter(email -> emailPattern.matcher(email.trim()).matches())
                        .collect(toList()) : Collections.emptyList();
        List<String> fromInput = clientInvitation.getEmails();
        fromInput.addAll(fromFile);
        return fromInput;

    }


}
