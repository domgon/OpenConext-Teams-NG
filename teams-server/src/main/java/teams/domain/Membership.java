package teams.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity(name = "memberships")
@Getter
@Setter
@EqualsAndHashCode(of = {"urnPerson", "urnTeam"})
@ToString
@NoArgsConstructor
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @Column
    private Instant created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "urn_person")
    @NotNull
    private String urnPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

    @Column(name = "urn_team")
    @NotNull
    private String urnTeam;

    public Membership(Role role, Team team, Person person) {
        Assert.notNull(team.getUrn(), "Urn team required");
        Assert.notNull(person.getUrn(), "Urn person required");
        this.role = role;
        this.team = team;
        this.urnTeam = team.getUrn();
        this.person = person;
        this.urnPerson = person.getUrn();
    }

    public Membership(String urnTeam, String urnPerson, Role role) {
        this.urnPerson = urnPerson;
        this.urnTeam = urnTeam;
        this.role = role;
    }
}
