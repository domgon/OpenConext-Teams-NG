package teams.repository;

import org.junit.Test;
import teams.AbstractApplicationTest;
import teams.domain.Team;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TeamRepositoryTest extends AbstractApplicationTest {

    @Test
    public void findByUrn() throws Exception {
        Team team = teamRepository.findByUrn("nl:surfnet:diensten:giants").get();
        assertEquals("giants", team.getName());
        assertEquals("Why did I create this team", team.getPersonalNote());
    }

    @Test
    public void findByMembershipsPersonUrn() throws Exception {
        List<Team> teams = teamRepository.findByMembershipsUrnPerson(
                "urn:collab:person:surfnet.nl:jdoe");
        assertEquals(3, teams.size());
    }

    @Test
    public void autoComplete() {
        List<Object[]> result = teamRepository.autocomplete(4L, "%ERS%", 4L);
        assertEquals(2, result.size());

        List<String> teamNames = result.stream().map(s -> s[0].toString()).collect(toList());
        assertEquals(2, teamNames.size());
        assertTrue(teamNames.contains("riders"));
        assertTrue(teamNames.contains("gliders"));
    }

    @Test
    public void existsByUrn() throws Exception {
        List<Object> urns = teamRepository.existsByUrn("nl:surfnet:diensten:giants");
        assertEquals(1, urns.size());
    }

}
