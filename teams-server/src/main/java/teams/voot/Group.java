package teams.voot;

import org.springframework.util.Assert;

import java.util.Objects;

public class Group {

    public final String id;
    public final String displayName;
    public final String description;
    public final String membership;

    public Group(String id, String displayName, String description, String membership) {
        Assert.notNull(id, "Id can not be null");
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.membership = membership;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
