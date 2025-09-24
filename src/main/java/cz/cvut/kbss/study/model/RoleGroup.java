package cz.cvut.kbss.study.model;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.util.HasOwlKey;
import cz.cvut.kbss.study.model.util.HasUri;
import cz.cvut.kbss.study.util.Constants;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_role_group)
public class RoleGroup implements Serializable, HasUri {

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = Vocabulary.s_p_label)
    private String name;

    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLObjectProperty(iri = Vocabulary.s_p_has_role)
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role){
        roles.add(role);
    }

    public void addRoles(Role ... roles) {
        if (roles != null) {
            this.roles.addAll(Set.of(roles));
        }
    }

    public boolean hasRole(Role role){
        return roles.contains(role);
    }

    public boolean hasRoles(Set<Role> roles){
        return roles.stream().anyMatch(roles::contains);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void generateUri() {
        this.uri = URI.create(Constants.BASE_URI + name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleGroup roleGroup = (RoleGroup) o;
        return Objects.equals(name, roleGroup.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
