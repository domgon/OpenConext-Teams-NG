package teams.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import teams.api.validations.MembershipValidator;
import teams.domain.ClientJoinRequest;
import teams.domain.FederatedUser;
import teams.domain.JoinRequest;
import teams.domain.Person;
import teams.domain.Team;
import teams.mail.MailBox;
import teams.repository.JoinRequestRepository;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@RestController
public class JoinRequestController extends ApiController implements MembershipValidator {

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Autowired
    private MailBox mailBox;

    @PostMapping("api/teams/join")
    public JoinRequest join(@Validated @RequestBody ClientJoinRequest clientJoinRequest, FederatedUser federatedUser) throws MessagingException, IOException {
        Team team = teamByUrn(clientJoinRequest.getTeamUrn());
        Person person = federatedUser.getPerson();

        List<String> admins = admins(team);

        membershipNotAllowed(team, person);
        privateTeamDoesNotAllowMembers(team, person);

        JoinRequest joinRequest = new JoinRequest(person, team, clientJoinRequest.getMessage());
        joinRequestRepository.save(joinRequest);

        mailBox.sendJoinRequestMail(joinRequest, admins);

        LOG.info("Created joinRequest for team {} and person {}", team.getUrn(), person.getUrn());

        return joinRequest;
    }

}
