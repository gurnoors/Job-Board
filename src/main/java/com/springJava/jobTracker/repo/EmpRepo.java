package com.springJava.jobTracker.repo;

import org.springframework.data.repository.CrudRepository;
import com.springJava.jobTracker.model.Employer;

public interface EmpRepo extends CrudRepository<Employer, Long> {
	Employer findByEmailid(String emailid);
}
