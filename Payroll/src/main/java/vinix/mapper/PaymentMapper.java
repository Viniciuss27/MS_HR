package vinix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vinix.dto.request.PaymentRequestDTO;
import vinix.dto.response.PaymentResponseDTO;
import vinix.entities.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentResponseDTO toDTO(Payment payment);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "workerName", ignore = true)
  @Mapping(target = "dailyIncome", ignore = true)
  @Mapping(target = "grossAmount", ignore = true)
  @Mapping(target = "paymentDate", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Payment toEntity(PaymentRequestDTO dto);
}
