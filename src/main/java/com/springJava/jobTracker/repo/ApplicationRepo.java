package com.springJava.jobTracker.repo;

import org.springframework.data.repository.CrudRepository;

import com.springJava.jobTracker.model.Application;

public interface ApplicationRepo extends CrudRepository<Application, Long> {

}
