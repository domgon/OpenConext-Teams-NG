/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teams.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teams.exception.ResourceNotFoundException;

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
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "invitations")
@Getter
@NoArgsConstructor
public class Invitation {

    private static final long TWO_WEEKS = 14L * 24L * 60L * 60L * 1000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @NotNull
    private Team team;

    @Column(name = "mailaddress", nullable = false)
    @NotNull
    private String email;

    @Column(nullable = false)
    private long timestamp;

    @Column(name = "invitation_uiid", nullable = false)
    private String invitationHash;

    @Column(name = "denied")
    private boolean declined;

    @Column(name = "accepted")
    private boolean accepted;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "invitation")
    @NotNull
    @Size(min = 1)
    private Set<InvitationMessage> invitationMessages = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "intended_role")
    @NotNull
    private Role intendedRole;

    @Enumerated(EnumType.STRING)
    private Language language;

    public Invitation(Team team, String email, Role intendedRole, Language language) throws UnsupportedEncodingException {
        this.team = team;
        this.email = email;
        this.invitationHash = generateInvitationHash();
        this.timestamp = new Date().getTime();
        this.language = language;
        this.intendedRole = intendedRole;
    }

    public boolean hasExpired() {
        return (timestamp + TWO_WEEKS) < System.currentTimeMillis();
    }

    public String getInvitationHash() {
        return invitationHash;
    }

    private String generateInvitationHash() throws UnsupportedEncodingException {
        Random secureRandom = new SecureRandom();
        byte[] aesKey = new byte[128];
        secureRandom.nextBytes(aesKey);
        String base64 = Base64.getEncoder().encodeToString(aesKey);
        return URLEncoder.encode(base64, "UTF-8").replaceAll("%", "");
    }

    @JsonIgnore
    public InvitationMessage getLatestInvitationMessage() {
        return invitationMessages.stream().max(Comparator.comparingLong(InvitationMessage::getTimestamp))
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("Invitation for team %s and person %s has no invitation message", team.getUrn(), email)));
    }

    public void accepted(boolean accepted) {
        this.accepted = accepted;
        this.declined = !accepted;
    }

    public InvitationMessage addInvitationMessage(Person person, String message) {
        InvitationMessage invitationMessage = new InvitationMessage(this, person, message);
        this.invitationMessages.add(invitationMessage);
        return invitationMessage;
    }

}