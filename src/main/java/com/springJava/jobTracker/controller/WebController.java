package com.springJava.jobTracker.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
import com.springJava.jobTracker.model.JobStatus;
import com.springJava.jobTracker.model.Profile;
import com.springJava.jobTracker.model.User;
import com.springJava.jobTracker.repo.CompanyRepo;
import com.springJava.jobTracker.repo.JobRepo;
import com.springJava.jobTracker.repo.ProfileRepo;
import com.springJava.jobTracker.repo.SpringEmailService;
import com.springJava.jobTracker.repo.UserRepo;


@RestController
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class WebController {
	
	@Autowired
	JobRepo jobRepo;
	@Autowired
	UserRepo userRepo;
	@Autowired
	CompanyRepo compRepo;
	@Autowired
	ProfileRepo profileRepo;
	@Autowired
    SpringEmailService springEmailService;
	
	
	//-------- Sanity Check
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome() {
		return "Welcome to your personal Job Tracker.";
	}
	
	// Job seeker sign up
	@RequestMapping(value = "/users/create", method = { RequestMethod.POST })
	public ResponseEntity<?> createUser(String username, String emailid, String password) {
		User user = userRepo.findByEmailid(emailid);
		Company company = compRepo.findByEmailid(emailid);
		if( user != null || company != null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Emailid with " +emailid+ " is already registered."), HttpStatus.BAD_REQUEST);
		}
		user = userRepo.findByUsername(username);
		company = compRepo.findByName(username);
		if( user != null || company != null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Username already taken"), HttpStatus.BAD_REQUEST);
		}
		Random rand = new Random();
		String code = String.format("%04d", rand.nextInt(10000));
		user = new User(username, emailid, password, code);      // need to encrypt password
		userRepo.save(user);

		sendEmail("anubha.mandal@sjsu.edu","verification code", "your verification code is " + user.getVerificationcode());
		String msg = "User with id " + user.getUserid() + " is created successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED); 						// need to send an email notification as well.	
	}
	
	// job seeker verification
	@RequestMapping(value = "/users/verify/{userid}", method = { RequestMethod.POST })
	public ResponseEntity<?> verifyUser(@PathVariable("userid") Long userid, String code){
		User user = userRepo.findOne(userid);
		if(!user.getVerificationcode().equals(code))
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.UNAUTHORIZED.value(),
					"Entered verification code does not match. Try Again"), HttpStatus.UNAUTHORIZED);
		}
		user.setStatus(true);
		String msg = "User with id " + user.getUserid() + " is verified successfully";
		sendEmail("anubha.mandal@sjsu.edu","Welcome to the site", "you account has been created successfully.");
		return new ResponseEntity<>(msg, HttpStatus.CREATED);
	}
	
	// Employer sign up
	@RequestMapping(value = "/employers/create", method = { RequestMethod.POST })
	public ResponseEntity<?> createEmployer(String emailid, String company_name, String password, String website, String address, 
			String description, String logo) {
		User user = userRepo.findByEmailid(emailid);
		Company company = compRepo.findByEmailid(emailid);
		if( user != null || company !=null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Emailid with " +emailid+ " is already registered, try logging in."), HttpStatus.BAD_REQUEST);
		}
		user = userRepo.findByUsername(company_name);
		company = compRepo.findByName(company_name);
		if( user != null || company != null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Name already registered"), HttpStatus.BAD_REQUEST);
		}
		Random rand = new Random();
		String code = String.format("%04d", rand.nextInt(10000));
		company = new Company(company_name, emailid, password, website, address, description, logo, code);      // need to encrypt password
		compRepo.save(company);
		String msg = "Employer with id " + company.getCompanyid() + "is created successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED); 						// need to send an email notification as well.	
	}
	
	// Employer verification
	@RequestMapping(value = "/employers/verify/{userid}", method = { RequestMethod.POST })
	public ResponseEntity<?> verifyCompany(@PathVariable("companyid") Long companyid, String code){
		Company company = compRepo.findOne(companyid);
		if(!company.getVerificationcode().equals(code))
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.UNAUTHORIZED.value(),
					"Entered verification code does not match. Try Again"), HttpStatus.UNAUTHORIZED);
		}
		company.setStatus(true);
		String msg = "Company with id " + company.getCompanyid() + " is verified successfully";
		sendEmail("anubha.mandal@sjsu.edu","Welcome to the site", "you account has been created successfully.");
		return new ResponseEntity<>(msg, HttpStatus.CREATED);
	}
	
	// Job seeker profile create  -- should be same for update as well
	@RequestMapping(value = "/users/profile/{userid}", method = { RequestMethod.POST })			//need to change the entry point
	public ResponseEntity<?> createUserProfile(@PathVariable("userid") Long userid, String firstname, String lastname, 				
			@RequestParam(value = "picture", defaultValue = "null") String picture, 			// data type for image?
			@RequestParam(value = "intro", defaultValue = "null") String intro, 
			String workex, String education, String skills, String phone) {								// check for data type of education (double?)
		User user = userRepo.findOne(userid);
		if( user == null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"User with id " + userid + "not found"), HttpStatus.NOT_FOUND);
		}
		Profile profile = profileRepo.findOne(userid);
		if(profile == null) {
			List<String> skillList = Arrays.asList(skills.split("\\,"));
			profile = new Profile(userid, firstname, lastname, picture, intro, workex, education, skillList, phone);
			profileRepo.save(profile);
		}
		else {
	//		profileRepo.updateProfile(firstname, lastname, picture, intro, workex, education, skills, phone, userid);
		}
		
		String msg = "Profile with userid " + userid + "is updated successfully";
		return new ResponseEntity<>(msg, HttpStatus.OK); 						// need to send an email notification as well.	
	}
	
	// Post a job
	@RequestMapping(value = "/jobs", method = { RequestMethod.POST })
	public ResponseEntity<?> postJob(String job_title, long compId, String skills, String desc, String location,
			int salary, JobStatus status) {	// company id and name shud be taken from employer table based on login and not i/p from employer
		List<String> skillList = Arrays.asList(skills.split("\\,"));
		Company company = compRepo.findOne(compId);
		Job job = new Job(job_title, skillList, desc, location, salary, status, company);
		jobRepo.save(job);

		String msg = "Job with id " + job.getJobid() + "is posted successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED);
	}
	
	// Update a job
	@RequestMapping(value = "/jobs/{id}", method = { RequestMethod.PUT })
	public ResponseEntity<?> updateJob(@PathVariable("id") Long id, String job_title, String skills, String desc, String location, int salary, String status) {	
		// company id and name shud be taken from employer table based on login and not i/p from employer
		Job job = jobRepo.findOne(id);
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Not found"), HttpStatus.NOT_FOUND);
		}

	//	jobRepo.updateJobDetails(job_title, empID, company_name, skill, desc, location, salary, status, id);

		String msg = "Job with id " + id + "is updated successfully";
		return new ResponseEntity<>(msg, HttpStatus.OK);
	}
	 
	
	// Job search by user
	@RequestMapping(value = "/jobs", method = { RequestMethod.GET })
	public ResponseEntity<?> searchJobUser(@RequestParam(value = "freeText", defaultValue = "null") String freeText,
			@RequestParam(value = "company", defaultValue = "null") String company, 
			@RequestParam(value = "location", defaultValue = "null") String location,
			@RequestParam(value = "salary", defaultValue = "null") float salary){
		List<Job> freeList = new ArrayList<Job>();
		List<Job> compList = new ArrayList<Job>();
		List<Job> locList = new ArrayList<Job>();
		List<Job> salList = new ArrayList<Job>();
		List<Job> res_list = new ArrayList<Job>();
		
		boolean freeFlag = false;
		boolean compFlag = false;
		boolean locFlag = false;
		boolean salFlag = false;
		
		
		return null;
	}
	
	public void sendEmail(String to, String subject, String text) {
        try {
            String from = "anubha.mandal@sjsu.edu";
            springEmailService.send(from, to, subject, text);//, inputStream, fileName, mimeType);

           // Notification.show("Email sent");
            } catch (Exception e) {
            e.printStackTrace();
            //Notification.show("Error sending the email", Notification.Type.ERROR_MESSAGE);
        }
    }
}