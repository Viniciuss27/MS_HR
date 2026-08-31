package vinix.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vinix.entities.Payment;
import vinix.feignclients.WorkerFeignClient;
import vinix.service.exceptions.ResourceNotFoundException;

@Service
public class PaymentService {

    @Autowired
    private WorkerFeignClient feign;

    public Payment getPayment(Long workerId, Integer days) throws ResourceNotFoundException {
        Worker worker = feign.findById(workerId).getBody();

        if (worker == null || worker.getName().equals("Fallback")) {
            throw new ResourceNotFoundException(workerId); //serviço indisponível
        }

        return new Payment(worker.getName(), worker.getDailyIncome(), days);
    }
}
