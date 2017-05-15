package com.springJava.jobTracker.repo;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.springJava.jobTracker.model.Job;
import com.springJava.jobTracker.model.JobStatus;


public interface JobRepo extends CrudRepository<Job, Long>{

	@Transactional
    @Modifying(clearAutomatically = true)
	@Query("update Job p set p.jobtitle=?1, p.skill=?2, p.description=?3, p.location=?4, p.salary=?5, p.status=?6 where p.id=?7")
	void updateJobDetails(String job_title, String skill, String desc, String location, int salary, JobStatus status, Long id);

}