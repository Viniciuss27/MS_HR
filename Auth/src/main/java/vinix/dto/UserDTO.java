package vinix.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class UserDTO implements Serializable, UserDetails{
	private static final long serialVersionUID = 1L;
	
	@Setter
	private Long id;
	
	@Setter
	private String name;
	
	@Setter
	private String email;
	
	@Setter
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	private Set<RoleDTO> roles = new HashSet<>();
	
	public UserDTO(Long id, String name, String email, String password) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream()
				.map(x -> new SimpleGrantedAuthority 
				(x.getRoleName()))
				.collect(Collectors.toList());
		

	}

	@Override
	public String getUsername() {
		return email; /*retorna o email como identificador do usuário*/
	}

	@Override
	public boolean isAccountNonExpired() {
		return true; /*conta não expirada*/
	}

	@Override
	public boolean isAccountNonLocked() {
		return true; /*conta não bloqueada*/
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; /*senha não expirada*/
	}

	@Override
	public boolean isEnabled() {
		return true; /*conta ativa*/
	}
}
