package vinix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vinix.dto.request.EmployeePositionRequestDTO;
import vinix.dto.request.EmployeeRequestDTO;
import vinix.dto.response.EmployeeDetailsResponseDTO;
import vinix.dto.response.EmployeeResponseDTO;
import vinix.dto.response.EmployeeSalaryResponseDTO;
import vinix.entities.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  EmployeeResponseDTO toDTO(Employee entity);

  EmployeeDetailsResponseDTO toDetailsDTO(Employee entity);

  EmployeeSalaryResponseDTO toSalaryDTO(Employee entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "hireDate", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Employee toEntity(EmployeeRequestDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "cpf", ignore = true)
  @Mapping(target = "birthDate", ignore = true)
  @Mapping(target = "hireDate", ignore = true)
  @Mapping(target = "dailyIncome", ignore = true)
  @Mapping(target = "active", ignore = true)
  void updatePosition(EmployeePositionRequestDTO dto, @MappingTarget Employee entity);
}
