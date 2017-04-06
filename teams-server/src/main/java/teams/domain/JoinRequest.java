/*
 * Copyright 2011 SURFnet bv, The Netherlands
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
import lombok.Setter;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.Instant;


@Entity(name = "requests")
@Setter
@Getter
@NoArgsConstructor
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @NotNull
    @JsonIgnore
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @JsonIgnore
    private Person person;

    @Column
    private Instant created;

    @Lob
    private String message;

    public JoinRequest(Person person, Team team, String message) {
        this.person = person;
        this.team = team;
        this.message = HtmlUtils.htmlEscape(message);
    }

    @JsonIgnore
    public boolean isContainsMessage() {
        return StringUtils.hasText(this.message);
    }

    @JsonIgnore
    public String getHtmlMessage() {
        return isContainsMessage() ? message.replaceAll("\n", "<br/>") : "";
    }

    public String getTeamName() {
        return team.getName();
    }

    public String getTeamUrn() {
        return team.getUrn();
    }
}