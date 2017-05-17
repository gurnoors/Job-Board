package com.springJava.jobTracker.repo;

import org.springframework.data.repository.CrudRepository;

import com.springJava.jobTracker.model.Company;

public interface CompanyRepo extends CrudRepository<Company, Long> {
	Company findByEmailid(String emailid);
	Company findByName(String name);
	Company findByNameContaining(String name);
}
