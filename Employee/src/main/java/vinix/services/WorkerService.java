package vinix.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import vinix.entities.Worker;
import vinix.repositories.EmployeeRepository;
import vinix.services.exceptions.DatabaseException;
import vinix.services.exceptions.ResourceNotFoundException;

@Service
public class WorkerService {

	// TODO: endpoint GET /workers (sem {id}) retornando lista de funcionários ativos,
	// usado pelo Payroll.launchPayroll() para gerar a folha do mês em lote

	@Autowired
	private EmployeeRepository repository;
	
	public List<Worker> findAll(){
		return repository.findAll();
	}
	
	public Worker findById(Long id) {
		Optional<Worker> worker = repository.findById(id);
		return worker.orElseThrow(() -> new ResourceNotFoundException(id));
	}
	
	public void deleteById(Long id) {
		try {
			repository.deleteById(id);
		}catch(EmptyResultDataAccessException e){ // se não existir o id
			throw new ResourceNotFoundException(id);// mensagem personalizada
		}catch(DataIntegrityViolationException e) {// quando viola as regras do banco
			throw new DatabaseException(e.getMessage()); // mensagem personalizada
		}
	}
	
	public Worker insert(Worker novo) {
		return repository.save(novo);
	}
	
	public Worker update(Long id, Worker obj) {
	    try {
	        Worker entity = repository.getReferenceById(id);
	        updateData(entity, obj);
	        return repository.save(entity);
	    } catch (EntityNotFoundException e) {
	        throw new ResourceNotFoundException(id);
	    }
	}

	private void updateData(Worker entity, Worker obj) {
		entity.setName(obj.getName());
		entity.setDailyIncome(obj.getDailyIncome());

	}
}
