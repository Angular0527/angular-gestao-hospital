package br.com.codenation.hospital.resource;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.codenation.hospital.constant.Constant;
import br.com.codenation.hospital.domain.Hospital;
import br.com.codenation.hospital.domain.Patient;
import br.com.codenation.hospital.services.PatientService;
import br.com.codenation.hospital.services.HospitalService;

@RestController
@RequestMapping(path = Constant.V1Path)
public class PatientResource {

	@Autowired
	private PatientService service;
	
	@Autowired
	private HospitalService hospitalService;
	
	@GetMapping(path="/pacientes/{paciente}", produces="application/json")
	public ResponseEntity<Patient> findPatientById(@PathVariable("hospital_id") String hospital_id, @PathVariable("paciente") String patient_id){
		return ResponseEntity.ok().body(service.findById(patient_id));
	}
	@PostMapping(path="/pacientes/{paciente}", produces="application/json")
	public ResponseEntity<Patient> createPacient(@PathVariable("hospital_id") String hospital_id, @PathVariable("paciente") String patient_id){
		Hospital obj = hospitalService.findById(hospital_id);
		List<Patient> patients = obj.getPatients();
		Patient patient = service.findById(patient_id);
		hospitalService.update(obj);
		return ResponseEntity.ok().body(patient);
	}

	@PutMapping(path="/pacientes/{paciente}", produces="application/json")
	public ResponseEntity<Patient> checkIn(@PathVariable("hospital_id") String hospital_id, @PathVariable("paciente") String patient_id, @RequestBody String data){
		Hospital obj = hospitalService.findById(hospital_id);

		Patient p = service.findById(patient_id);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> map = new HashMap<String, String>();
		try {
			map = mapper.readValue(data, new TypeReference<Map<String, String>>(){});
			if (map.get("action").equals("check-in")){
				if(!p.isActive()) {
					obj.setAvailableBeds(obj.getAvailableBeds() - 1);
					p.checkIn();
				}
			}else if(map.get("action").equals("check-out")){
				if(p.isActive()) {
					obj.setAvailableBeds(obj.getAvailableBeds() + 1);
					p.checkOut();
				}
			}
			hospitalService.update(obj);
			service.update(p);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		}catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok().body(p);
	}
}