package teams.shibboleth;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import teams.domain.Membership;
import teams.domain.Person;
import teams.repository.MembershipRepository;
import teams.repository.PersonRepository;
import teams.security.SuperAdmin;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShibbolethPreAuthenticatedProcessingFilterTest {

    private PersonRepository personRepository;

    private MembershipRepository membershipRepository;

    private ShibbolethPreAuthenticatedProcessingFilter subject;

    private final SuperAdmin superAdmin = new SuperAdmin(Arrays.asList("demo:openconext:org:super_admins"));

    @Before
    public void before() {
        personRepository = mock(PersonRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        subject = new ShibbolethPreAuthenticatedProcessingFilter(mock(AuthenticationManager.class), personRepository,
                membershipRepository, "urn:collab:org:surf.nl", superAdmin);
        when(membershipRepository.findByUrnTeamAndUrnPerson(anyString(), anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void getPreAuthenticatedPrincipalNotValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Person person = Person.class.cast(subject.getPreAuthenticatedPrincipal(request));
        assertNull(person.getId());
    }

    @Test
    public void getPreAuthenticatedPrincipalAlreadyExists() {
        Person person = new Person("urn", "John Doe", "mail", false, false);

        when(personRepository.findByUrnIgnoreCase("urn")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(person);
        Person principal = Person.class.cast(subject.getPreAuthenticatedPrincipal(populateServletRequest("Name")));
        assertEquals(person, principal);
    }

    @Test
    public void getPreAuthenticatedPrincipalSuperAdmin() {
        Person person = new Person("urn", "John Doe", "mail", false, false);

        when(personRepository.findByUrnIgnoreCase("urn")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(person);
        when(membershipRepository.findByUrnTeamAndUrnPerson(superAdmin.getUrns().get(0), person.getUrn()))
                .thenReturn(Optional.of(new Membership()));

        Person principal = Person.class.cast(subject.getPreAuthenticatedPrincipal(populateServletRequest("Name")));
        assertTrue(principal.isSuperAdmin());
    }

    @Test
    public void getPreAuthenticatedPrincipalUTF8() {
        Person person = new Person("urn", "Ã¤Ã¼-test-Ã¯Ã«", "mail", false, false);
        Person principal = doGetPreAuthenticatedPrincipal(person);

        assertEquals(person, principal);
        assertEquals("au-test-ie", person.getName());
    }

    @Test
    public void normalize() {
        String res = subject.normalize("orčpžsíáýd");
        assertEquals("orcpzsiayd", res);
    }

    @Test
    public void getPreAuthenticatedPrincipalDoesChanged() {
        Person person = new Person("urn", "Name", "mail", true, false);

        when(personRepository.save(any(Person.class))).thenReturn(person);

        Person principal = doGetPreAuthenticatedPrincipal(person);
        assertEquals(person, principal);
    }

    private Person doGetPreAuthenticatedPrincipal(Person person) {
        MockHttpServletRequest request = populateServletRequest(person.getName());

        when(personRepository.findByUrnIgnoreCase("urn")).thenReturn(Optional.of(person));
        return Person.class.cast(subject.getPreAuthenticatedPrincipal(request));
    }

    private MockHttpServletRequest populateServletRequest(String displayName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("name-id", "urn");
        request.addHeader("displayName", displayName);
        request.addHeader("Shib-InetOrgPerson-mail", "mail");
        request.addHeader("is-member-of", "urn:collab:org:surf.nl");
        return request;
    }
}