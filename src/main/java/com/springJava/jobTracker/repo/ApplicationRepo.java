package com.springJava.jobTracker.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.springJava.jobTracker.model.Application;
import com.springJava.jobTracker.model.ApplicationStatus;
import com.springJava.jobTracker.model.ApplicationType;
import com.springJava.jobTracker.model.Job;
import com.springJava.jobTracker.model.JobStatus;
import com.springJava.jobTracker.model.User;

public interface ApplicationRepo extends CrudRepository<Application, Long> {

	// updating the application status when job status is changed as filled
	@Transactional
    @Modifying(clearAutomatically = true)
	@Query("update Application p set p.status=?1 where p.id=?2")
	void updateApplicationStatus_JS(ApplicationStatus status, Long id);
	
	List<Application> findByStatusAndJob(ApplicationStatus status, Job job);
	List<Application> findByJob(Job job);
	List<Application> findByUserAndType(User user, ApplicationType type);
	Application findByJobAndUser(Job job, User user);
}
