package com.springJava.jobTracker.repo;

import org.springframework.data.repository.CrudRepository;

import com.springJava.jobTracker.model.Job;

public interface UserRepo extends CrudRepository<User, Long>{
	User findByEmailid(String emailid);
}
