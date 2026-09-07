package vinix.resources;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import vinix.entities.Worker;
import vinix.services.WorkerService;

/*GET    /workers      → findAll
  GET    /workers/{id} → findById
  POST   /workers      → insert  (retorna 201 Created + URI do novo recurso)
  PUT    /workers/{id} → update  (retorna 200 OK)
  DELETE /workers/{id} → delete  (retorna 204 No Content)*/

@RestController
@RequestMapping(value = "/employeers")
public class WorkerResource {

	@Autowired
	private WorkerService service;
	
	@GetMapping
	public ResponseEntity<List<Worker>> findAll(){
		return ResponseEntity.ok().body(
				service.findAll());
	}
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<Worker> findById(@PathVariable Long id) {
		return ResponseEntity.ok().body(service.findById(id));
	}
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id){
		service.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<Worker> update(@PathVariable Long id, @RequestBody Worker obj){
		return ResponseEntity.ok().body(
				service.update(id, obj));
	}
	
	@PostMapping
	public ResponseEntity<Worker> insert(@RequestBody Worker novo){
		novo = service.insert(novo);
	URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}").buildAndExpand(novo.getId()).toUri();
	return ResponseEntity.created(uri).body(novo);
	}
}
