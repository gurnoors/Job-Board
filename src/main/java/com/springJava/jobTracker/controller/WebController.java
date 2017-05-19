package com.springJava.jobTracker.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.springJava.jobTracker.model.Company;
import com.springJava.jobTracker.model.Job;
import com.springJava.jobTracker.model.JobStatus;
import com.springJava.jobTracker.model.Profile;
import com.springJava.jobTracker.model.User;
import com.springJava.jobTracker.repo.CompanyRepo;
import com.springJava.jobTracker.repo.JobRepo;
import com.springJava.jobTracker.repo.ProfileRepo;
import com.springJava.jobTracker.repo.UserRepo;

@RestController
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class WebController {
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Autowired
	JobRepo jobRepo;
	@Autowired
	UserRepo userRepo;
	@Autowired
	CompanyRepo compRepo;
	@Autowired
	ProfileRepo profileRepo;
	@Autowired
    private JavaMailSender sender;

	// -------- Sanity Check
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome() {
		return "Welcome to your personal Job Tracker.";
	}

	// Job seeker sign up
	@RequestMapping(value = "/users/create", method = { RequestMethod.POST })
	public ResponseEntity<?> createUser(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String username = jobj.get("username").getAsString();
		String password = jobj.get("password").getAsString();
		String emailid = jobj.get("emailID").getAsString();

		if (username == null || password == null || emailid == null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.BAD_REQUEST.value(), "Insufficient data"), HttpStatus.BAD_REQUEST);
		}
		User user = userRepo.findByEmailid(emailid);
		Company company = compRepo.findByEmailid(emailid);
		if (user != null || company != null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Emailid with " + emailid + " is already registered."), HttpStatus.BAD_REQUEST);
		}
		user = userRepo.findByUsername(username);
		company = compRepo.findByName(username);
		if (user != null || company != null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.BAD_REQUEST.value(), "Username already taken"),
					HttpStatus.BAD_REQUEST);
		}
		Random rand = new Random();
		String code = String.format("%04d", rand.nextInt(10000));
		user = new User(username, emailid, password, code);

		userRepo.save(user);

		request.getSession().setAttribute("loggedIn", "user");
		request.getSession().setAttribute("email", emailid);

		try {
			sendEmail(emailid, "Dear User,\n\nThank you for registering in Job-Borad. Your verification code is " + user.getVerificationcode() + ".\n\nThanks,\nJob-board", 
					"Verification code from Job-Board");
			return new ResponseEntity<String>("Email sent successfully with the verification code", HttpStatus.OK);
		} catch(Exception ex) {
			return new ResponseEntity<String>("Error sending email " +ex, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// job seeker & employer verification
	@RequestMapping(value = "/users/verify", method = { RequestMethod.POST })
	public ResponseEntity<?> verifyUser(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");

		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String verificationCode = jobj.get("verificationCode").getAsString();

		// get email id from the session
		String emailid = (String) request.getSession().getAttribute("email");

		User user = userRepo.findByEmailid(emailid);
		String msg = null;
		if (user != null) {
			if (!user.getVerificationcode().equals(verificationCode)) {
				return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.UNAUTHORIZED.value(),
						"Entered verification code does not match. Try Again"), HttpStatus.UNAUTHORIZED);
			} else {
				user.setStatus(true);
				try {
					sendEmail(emailid, "Dear User,\n\nYour account has been verified successfully. Happy job hunting.\n\nThanks,\nJob-board", 
							"Welcome to Job-Board");
					return new ResponseEntity<String>("Email sent successfully", HttpStatus.OK);
					
				} catch (Exception e) {
					return new ResponseEntity<String>("Error sending email " +e, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		} else {
			Company company = compRepo.findByEmailid(emailid);
			if (company != null) {
				if (!company.getVerificationcode().equals(verificationCode)) {
					return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.UNAUTHORIZED.value(),
							"Entered verification code does not match. Try Again"), HttpStatus.UNAUTHORIZED);
				} else {
					company.setStatus(true);
					msg = "User with id " + company.getEmailid() + " is verified successfully";
					try {
						sendEmail("anubha.mandal@sjsu.edu", "Dear Employer,\n\nYour account has been verified successfully.\n\nThanks,\nJob-board",
								"Welcome to Job-Board");
						return new ResponseEntity<String>("Email sent successfully", HttpStatus.OK);
					} catch (Exception e) {
						return new ResponseEntity<String>("Error sending email " +e, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}
		}
		return new ResponseEntity<>(msg, HttpStatus.OK);
	}

	// Employer sign up
	@RequestMapping(value = "/employers/create", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<?> createEmployer(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException {

		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();
		System.out.println(body);
		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String emailid = jobj.get("Email ID").getAsString();
		String password = jobj.get("Password").getAsString();
		String companyname = jobj.get("Company Name").getAsString();;
		String website = null;
		if(jobj.get("Website") != null){
			website = jobj.get("Website").getAsString();
		}
		String address = jobj.get("Address_Headquarters").getAsString();
		String description = jobj.get("Description").getAsString();
		String logo = jobj.get("Logo_Image_URL").getAsString();

		if (emailid == null || password == null || companyname == null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.BAD_REQUEST.value(), "Insufficient data"), HttpStatus.BAD_REQUEST);
		}

		User user = userRepo.findByEmailid(emailid);
		Company company = compRepo.findByEmailid(emailid);
		if (user != null || company != null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.BAD_REQUEST.value(),
							"Emailid with " + emailid + " is already registered, try logging in."),
					HttpStatus.BAD_REQUEST);
		}
		user = userRepo.findByUsername(companyname);
		company = compRepo.findByName(companyname);
		if (user != null || company != null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.BAD_REQUEST.value(), "Name already registered"),
					HttpStatus.BAD_REQUEST);
		}
		
		
		Random rand = new Random();
		String code = String.format("%04d", rand.nextInt(10000));
		company = new Company(companyname, emailid, password, website, address, description, logo, code);
		compRepo.save(company);

		request.getSession().setAttribute("loggedIn", "employer");
		request.getSession().setAttribute("email", emailid);
		
		//String msg = null;
		try {
			sendEmail(emailid, "Dear Employer,\n\nThank you for registering in Job-Borad. Your verification code is " + company.getVerificationcode() + ".\n\nThanks,\nJob-board", 
					"Verification code from Job-Board");
			return new ResponseEntity<String>("Email sent successfully with the verification code", HttpStatus.CREATED);
		} catch(Exception ex) {
			return new ResponseEntity<String>("Error sending email " +ex, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Job seeker profile create  -- same for update as well
	@RequestMapping(value = "/userprofile/create", method = { RequestMethod.POST })			//need to change the entry point
	public ResponseEntity<?> createUserProfile(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException{

		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String firstname = jobj.get("First Name").getAsString();
		String lastname = jobj.get("Last Name").getAsString();
		String picture = jobj.get("Picture").getAsString();
		String intro = jobj.get("Self-introduction").getAsString();
		String workex = jobj.get("Work Experience").getAsString();
		String education = jobj.get("Education").getAsString();
		String skills = jobj.get("Skills").getAsString();
		String phone = jobj.get("Phone").getAsString();

		if (firstname == null || lastname == null || workex == null || education == null || skills == null){
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Insufficient data"), HttpStatus.BAD_REQUEST);
		}

		//get email id from the session
		String emailid = (String) request.getSession().getAttribute("email");

		User user = userRepo.findByEmailid(emailid);
		if( user == null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"User not found"), HttpStatus.NOT_FOUND);
		}

		Profile profile = profileRepo.findOne(user.getUserid());
		if(profile == null) {
			//List<String> skillList = Arrays.asList(skills.split("\\,"));
			profile = new Profile(user.getUserid(), firstname, lastname, picture, intro, workex, education, skills, phone);
			profileRepo.save(profile);
		}
		else {
			profileRepo.updateProfile(firstname, lastname, picture, intro, workex, education, skills, phone, user.getUserid());
		}

		String msg = "Profile with userid " + user.getUserid() + "is updated successfully";
		return new ResponseEntity<>(msg, HttpStatus.OK); 						// need to send an email notification as well.
	}

	// Post a job
	@RequestMapping(value = "/jobs/post", method = { RequestMethod.POST })
	public ResponseEntity<?> postJob(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException {

		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String job_title = jobj.get("Title").getAsString();
		String desc = jobj.get("Description").getAsString();
		String skills = jobj.get("Responsibilities").getAsString();
		String location = jobj.get("Office Location").getAsString();
		int salary = jobj.get("Salary").getAsInt();

		if(job_title == null || skills == null)
		{
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Insufficient data"), HttpStatus.BAD_REQUEST);
		}

		String emailid = (String) request.getSession().getAttribute("email");

		Company company = compRepo.findByEmailid(emailid);
		if (company == null){
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Company with id " + emailid + "not found"), HttpStatus.NOT_FOUND);
		}
		Job job = new Job(job_title, skills, desc, location, salary, JobStatus.OPEN, company);
		jobRepo.save(job);

		try {
			sendEmail(emailid, "Dear Employer,\n\nJob with id #" + job.getJobid() + " is posted successfully. Below are the job details:\nJob-Title: " +job_title+ "\nSkills: " +skills+ "\ndescription: " +desc+ "\nLocation: " +location+ ".\n\nThanks,\nJob-Borad.", 
					"New job posted in Job-Board");
			return new ResponseEntity<String>("Email sent successfully with the job details", HttpStatus.CREATED);
		} catch(Exception ex) {
			return new ResponseEntity<String>("Error sending email " +ex, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Update a job
	@RequestMapping(value = "/jobs/update", method = { RequestMethod.PUT })
	public ResponseEntity<?> updateJob(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException {

		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		Long id = jobj.get("id").getAsLong();
		String job_title = jobj.get("Title").getAsString();
		String desc = jobj.get("Description").getAsString();
		String skills = jobj.get("Responsibilities").getAsString();
		String location = jobj.get("Office Location").getAsString();
		int salary = jobj.get("Salary").getAsInt();
		String status_tmp = jobj.get("Status").getAsString();

		JobStatus status = JobStatus.OPEN;
		if (status_tmp.equals("FILLED")){
			status = JobStatus.FILLED;
		}
		else if (status_tmp.equals("CANCELLED")){
			status = JobStatus.CANCELLED;
		}
		else {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.BAD_REQUEST.value(),
					"Not a valid Status"), HttpStatus.BAD_REQUEST);
		}

		Job job = jobRepo.findOne(id);
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Job with id" +id + "Not found"), HttpStatus.NOT_FOUND);
		}

		jobRepo.updateJobDetails(job_title, skills, desc, location, salary, status, id);
		String emailid = (String) request.getSession().getAttribute("email");

		try {
			sendEmail(emailid, "Dear Employer,\n\nJob with id " + job.getJobid() + " is updated successfully. Below are the job details:\nJob-Title: " +job_title+ "\nSkills: " +skills+ "\ndescription: " +desc+ "\nLocation: " +location+ ".\n\nThanks,\nJob-Borad.", 
					"New job posted in Job-Board");
			return new ResponseEntity<String>("Email sent successfully with the job details", HttpStatus.OK);
		} catch(Exception ex) {
			return new ResponseEntity<String>("Error sending email " +ex, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Job search by job seeker
	@RequestMapping(value = "/jobs/search/{searchTerm}/{companyName}/{location}/{salaryRange}", method = {
			RequestMethod.GET })
	public ResponseEntity<?> searchJobUser(HttpServletRequest request, @PathVariable("searchTerm") String freeText,
			@PathVariable("companyName") String companyname, @PathVariable("location") String location,
			@PathVariable("salaryRange") String salary1) throws UnsupportedEncodingException {

		int salary = Integer.parseInt(salary1);
		List<Job> freeList = new ArrayList<Job>();
		List<Job> compList = new ArrayList<Job>();
		List<Job> locList = new ArrayList<Job>();
		List<Job> salList = new ArrayList<Job>();
		List<Job> res_list = new ArrayList<Job>(); // Final output list

		boolean freeFlag = false;
		boolean compFlag = false;
		boolean locFlag = false;
		boolean salFlag = false;

		if (!freeText.equals("null")) {
			freeList = getFreeTextJobs(freeText);
			freeFlag = true;
			res_list = freeList;
		}
		if (!companyname.equals("null")) {
			compList = getCompanyNameJobs(companyname);
			compFlag = true;
			if (compList.size() > res_list.size()) {
				res_list = compList;
			}
		}
		if (!location.equals("null")) {
			locList = getLocationJobs(location);
			locFlag = true;
			if (locList.size() > res_list.size()) {
				res_list = locList;
			}
		}
		if (salary != 0) {
			salList = getSalaryJobs(salary);
			salFlag = true;
			if (salList.size() > res_list.size()) {
				res_list = salList;
			}
		}
		if (freeFlag)
			res_list = my_intersect(res_list, freeList);
		if (compFlag)
			res_list = my_intersect(res_list, compList);
		if (locFlag)
			res_list = my_intersect(res_list, locList);
		if (salFlag)
			res_list = my_intersect(res_list, salList);

		if (res_list.isEmpty()) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.NOT_FOUND.value(), "No job match."), HttpStatus.NOT_FOUND);
		}
		ResponseEntity<List<Job>> response = new ResponseEntity<List<Job>>(res_list, HttpStatus.OK);
		return response;
	}

	public List<Job> getFreeTextJobs(String freeText) {
		List<String> inputList = Arrays.asList(freeText.split(","));
		List<Job> res = new ArrayList<Job>(); // final output list
		// Search in job title
		for (String l : inputList) {
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndJobtitleContaining(JobStatus.OPEN, l));
			tmp.removeAll(res);
			res.addAll(tmp);
		}
		// Search in location
		for (String l : inputList) {
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndLocationContaining(JobStatus.OPEN, l));
			tmp.removeAll(res);
			res.addAll(tmp);
		}
		// Search in skills
		for (String l : inputList) {
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndSkillContaining(JobStatus.OPEN, l));
			tmp.removeAll(res);
			res.addAll(tmp);
		}
		// Search in company Name
		for (String l : inputList) {
			// Company company = compRepo.findByNameContaining(l);
			Company company = compRepo.findByName(l);
			// List<Job> tmp = new
			// ArrayList<>(jobRepo.findByStatusAndCompanyContaining(JobStatus.OPEN,
			// company));
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndCompany(JobStatus.OPEN, company));
			tmp.removeAll(res);
			res.addAll(tmp);
		}
		// Search in description
		for (String l : inputList) {
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndDescriptionContaining(JobStatus.OPEN, l));
			tmp.removeAll(res);
			res.addAll(tmp);
		}
		return res;
	}

	public List<Job> getCompanyNameJobs(String companyname) {
		List<String> inputList = Arrays.asList(companyname.split(","));
		List<Job> res = new ArrayList<Job>(); // final output list
		// Search in company Name
		for (String l : inputList) {
			Company company = compRepo.findByName(l);
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndCompany(JobStatus.OPEN, company));
			res.addAll(tmp);
		}
		return res;
	}

	public List<Job> getLocationJobs(String location) {
		List<String> inputList = Arrays.asList(location.split(","));
		List<Job> res = new ArrayList<Job>(); // final output list
		// Search in Locations
		for (String l : inputList) {
			List<Job> tmp = new ArrayList<>(jobRepo.findByStatusAndLocationContaining(JobStatus.OPEN, l));
			res.addAll(tmp);
		}
		return res;
	}

	public List<Job> getSalaryJobs(int salary) {
		// Search in salary
		List<Job> res = new ArrayList<>(jobRepo.findByStatusAndSalaryGreaterThan(JobStatus.OPEN, salary)); // final
																											// output
																											// list
		return res;
	}

	public void sendEmail(String recepient, String msg, String subject) throws Exception{
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        
        helper.setTo(recepient);
        helper.setText(msg);
        helper.setSubject(subject);
        
        sender.send(message);
    }

	public static List<Job> my_intersect(List<Job> a, List<Job> b) {
		List<Job> result = new ArrayList<Job>();
		for (Job j : a) {
			for (Job v : b) {
				if (v.getJobid() == j.getJobid())
					result.add(j);
			}
		}
		return result;
	}
}
