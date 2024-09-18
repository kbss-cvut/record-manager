package cz.cvut.kbss.study.model;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.util.HasDerivableUri;
import cz.cvut.kbss.study.model.util.HasOwlKey;
import cz.cvut.kbss.study.util.Constants;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_role_group)
public class RoleGroup implements HasDerivableUri {

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = Vocabulary.s_p_label)
    private String name;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_role)
    private Set<Role> roles;

    public void addRole(Role role){
        if(roles == null){
            roles = new HashSet<>();
        }
        roles.add(role);
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

    @Override
    public void generateUri() {
        this.uri = URI.create(Constants.BASE_URI + name);
    }
}
