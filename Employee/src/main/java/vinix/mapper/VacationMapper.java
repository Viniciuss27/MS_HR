package vinix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vinix.dto.request.VacationRequestDTO;
import vinix.dto.response.VacationResponseDTO;
import vinix.entities.VacationRequest;

@Mapper(componentModel = "spring")
public interface VacationMapper {

  @Mapping(target = "employeeId", source = "employee.id")
  @Mapping(target = "employeeName", source = "employee.name")
  VacationResponseDTO toResponseDTO(VacationRequest entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", ignore = true)
  @Mapping(target = "daysRequested", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "requestedAt", ignore = true)
  @Mapping(target = "decidedAt", ignore = true)
  @Mapping(target = "decidedBy", ignore = true)
  @Mapping(target = "acquisitionStartDate", ignore = true)
  @Mapping(target = "acquisitionEndDate", ignore = true)
  @Mapping(target = "unjustifiedAbsences", ignore = true)
  VacationRequest toEntity(VacationRequestDTO dto);
}