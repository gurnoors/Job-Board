package com.springJava.jobTracker.repo;

import org.springframework.data.repository.CrudRepository;

import com.springJava.jobTracker.model.Company;

public interface EmpRepo extends CrudRepository<Company, Long> {
	Company findByEmailid(String emailid);
}
