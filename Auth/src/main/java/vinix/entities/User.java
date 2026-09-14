package vinix.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Getter
@Entity @Table(name = "tb_user")
public class User implements UserDetails {
    private static final long serialVersionUID = 1L;

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter @Column(nullable = false, length = 100)
    private String name;

    @Setter @Column(nullable = false, unique = true)
    private String email;

    @Setter @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Setter
    @Column(name = "employee_id", nullable = false, unique = true)
    private Long employeeId;

    @Setter
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tb_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(Long id, String name, String email, String password, Long employeeId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.employeeId = employeeId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return roles.stream()
          .map(x -> new SimpleGrantedAuthority(x.getRoleName()))
          .collect(Collectors.toList());
    }

    @Override // retorna o email como identificador do usuário
    public String getUsername() { return email; }

    @Override // conta não expirada
    public boolean isAccountNonExpired() { return true; }

    @Override // conta não bloqueada
    public boolean isAccountNonLocked() { return true;}

    @Override // senha não expirada
    public boolean isCredentialsNonExpired() { return true; }

    @Override // conta ativa
    public boolean isEnabled() { return active; }
}