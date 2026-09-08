package vinix.dto.response;

public record EmployeeResponseDTO(// para resposta de requisição
    Long id,
    String name,
    String position
) {}