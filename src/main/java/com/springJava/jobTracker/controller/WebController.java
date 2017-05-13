package com.springJava.jobTracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springJava.jobTracker.model.Company;
import com.springJava.jobTracker.model.Job;
import com.springJava.jobTracker.model.Profile;
import com.springJava.jobTracker.model.User;
import com.springJava.jobTracker.repo.EmpRepo;
import com.springJava.jobTracker.repo.JobRepo;
import com.springJava.jobTracker.repo.ProfileRepo;
import com.springJava.jobTracker.repo.UserRepo;

@RestController
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class WebController {
	
	@Autowired
	JobRepo jobRepo;
	@Autowired
	UserRepo userRepo;
	@Autowired
	EmpRepo empRepo;
	@Autowired
	ProfileRepo profileRepo;
	
	
	//-------- Sanity Check
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome() {
		return "Welcome to your personal Job Tracker.";
	}
	
	// Job seeker sign up
	@RequestMapping(value = "/users/create", method = { RequestMethod.POST })
	public ResponseEntity<?> createUser(String emailid, String username, String password) {
		User user = userRepo.findByEmailid(emailid);
		Company employer = empRepo.findByEmailid(emailid);
		if( user != null || employer != null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Emailid with " +emailid+ " is already registered, try logging in."), HttpStatus.BAD_REQUEST);
		}
		user = new User(emailid, username, password);      // need to encrypt password
		userRepo.save(user);

		String msg = "User with id " + user.getUserid() + "is created successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED); 						// need to send an email notification as well.	
	}
	
	// Employer sign up
	@RequestMapping(value = "/employers/create", method = { RequestMethod.POST })
	public ResponseEntity<?> createCompany(String emailid, String company_name, String password, String website, String address, 
			String description, String logo) {
		User user = userRepo.findByEmailid(emailid);
		Company employer = empRepo.findByEmailid(emailid);
		if( user != null || employer !=null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Emailid with " +emailid+ " is already registered, try logging in."), HttpStatus.BAD_REQUEST);
		}
		employer = new Company(emailid, company_name, password, website, address, description, logo);      // need to encrypt password
		empRepo.save(employer);

		String msg = "Employer with id " + employer.getCompanyid() + "is created successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED); 						// need to send an email notification as well.	
	}
	
	/**
	 * @author anubha
	 * @param userid
	 * @param firstname
	 * @param lastname
	 * @param picture
	 * @param intro
	 * @param workex
	 * @param education
	 * @param skills
	 * @param phone
	 * @return
	 */
	// Job seeker profile create  -- shud be same for update as well
//	@RequestMapping(value = "/users/profile/{userid}", method = { RequestMethod.POST })			//need to change the entry point
//	public ResponseEntity<?> createUserProfile(@PathVariable("id") Long userid, String firstname, String lastname, 				
//			@RequestParam(value = "picture", defaultValue = "null") String picture, 			// data type for image?
//			@RequestParam(value = "intro", defaultValue = "null") String intro, 
//			String workex, String education, String skills, String phone) {								// check for data type of education (double?)
//		User user = userRepo.findOne(userid);
//		if( user == null)
//		{
//			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
//					"User with id " + userid + "not found"), HttpStatus.NOT_FOUND);
//		}
//		//Profile profile = profileRepo.findOne(userid);
//		Profile profile = profileRepo.findOne(userid);
//		if(profile == null) {
//			profile = new Profile(userid, firstname, lastname, picture, intro, workex, education, skills, phone);
//			profileRepo.save(profile);
//		}
//		
//		else {
//			profileRepo.updateProfile(firstname, lastname, picture, intro, workex, education, skills, phone, userid);
//		}
//		
//		String msg = "Profile with userid " + userid + "is updated successfully";
//		return new ResponseEntity<>(msg, HttpStatus.OK); 						// need to send an email notification as well.	
//	}
	
	// Post a job
	
	/**
	 * @author anubha
	 * @param job_title
	 * @param empID
	 * @param company_name
	 * @param skill
	 * @param desc
	 * @param location
	 * @param salary
	 * @param status
	 * @return
	 */
//	@RequestMapping(value = "/jobs", method = { RequestMethod.POST })
//	public ResponseEntity<?> postJob(String job_title, Long empID, String company_name, String skill, String desc, String location,
//			String salary, String status) {	// company id and name shud be taken from employer table based on login and not i/p from employer
//		Job job = new Job(job_title, empID, company_name, skill, desc, location, salary, status);
//		jobRepo.save(job);
//
//		String msg = "Job with id " + job.getJobid() + "is posted successfully";
//		return new ResponseEntity<>(msg, HttpStatus.CREATED);
//	}
	
	// Update a job
	@RequestMapping(value = "/jobs/{id}", method = { RequestMethod.PUT })
	public ResponseEntity<?> updateJob(@PathVariable("id") Long id, String job_title, Long empID, String company_name, 
			String skill, String desc, String location, float salary, String status) {	
		// company id and name shud be taken from employer table based on login and not i/p from employer
		Job job = jobRepo.findOne(id);
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Not found"), HttpStatus.NOT_FOUND);
		}

//		jobRepo.updateJobDetails(job_title, empID, company_name, skill, desc, location, salary, status, id);

		String msg = "Job with id " + id + "is updated successfully";
		return new ResponseEntity<>(msg, HttpStatus.OK);
	}
	 
}