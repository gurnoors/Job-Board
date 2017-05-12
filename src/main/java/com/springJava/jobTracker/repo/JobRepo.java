package com.springJava.jobTracker.repo;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.springJava.jobTracker.model.Job;


public interface JobRepo extends CrudRepository<Job, Long>{
	
	@Transactional
    @Modifying(clearAutomatically = true)
	@Query("update Job p set p.jobTitle=?1, p.empID=?2, p.companyName=?3, p.skill=?4, p.desc=?5, p.location=?6, p.salary=?7, p.status=?8 where p.id=?9")
	void updateJobDetails(String job_title, Long empID, String company_name, String skill, String desc, String location, float salary, String status, Long id);

}