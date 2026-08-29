package vinix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vinix.dto.request.RegisterRequestDTO;
import vinix.dto.response.UserResponseDTO;
import vinix.entities.Role;
import vinix.entities.User;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(RegisterRequestDTO dto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserResponseDTO toResponseDTO(User user);

    @Named("rolesToNames")
    default List<String> rolesToNames(Set<Role> roles) {
      return roles.stream()
          .map(Role::getRoleName)
          .toList();
    }
}