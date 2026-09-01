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
  Payment toEntity(PaymentRequestDTO paymentRequestDTO);
}
