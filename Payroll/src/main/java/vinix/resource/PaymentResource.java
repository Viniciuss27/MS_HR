package vinix.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vinix.entities.Payment;
import vinix.services.PaymentService;

@RestController
@RequestMapping(value = "/payments")
public class PaymentResource {

	@Autowired
	private PaymentService serv;
	
	@GetMapping(value = "/{workerId}/days/{days}")
	public ResponseEntity<Payment> payment(
			@PathVariable Long workerId,
			@PathVariable Integer days){
		
		return ResponseEntity.ok(serv.getPayment(workerId, days));
	}
}
