package com.springJava.jobTracker.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.springJava.jobTracker.repo.SpringEmailService;
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
	SpringEmailService springEmailService;

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
		user = new User(username, emailid, password, code); // need to encrypt
															// password

		userRepo.save(user);

		request.getSession().setAttribute("loggedIn", "user");
		request.getSession().setAttribute("email", emailid);

		sendEmail(emailid, "verification code", "your verification code is " + user.getVerificationcode());
		String msg = "User with id " + user.getUserid() + " is created successfully. Verification Pending....";
		return new ResponseEntity<String>(msg, HttpStatus.CREATED); // need to
																	// send an
		// email
		// notification
		// as well.
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
				msg = "User with id " + user.getEmailid() + " is verified successfully";
				sendEmail(emailid, "Welcome to the site", "you account has been verified successfully.");
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
					sendEmail("anubha.mandal@sjsu.edu", "Welcome to the site",
							"you account has been created successfully.");
				}
			}
		}
		return new ResponseEntity<>(msg, HttpStatus.OK);
	}

	// Employer sign up
	@RequestMapping(value = "/employers/create", method = { RequestMethod.POST })

	public ResponseEntity<?> createEmployer(HttpServletRequest request, HttpEntity<String> httpEntity)
			throws UnsupportedEncodingException {

		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String emailid = jobj.get("Email ID").getAsString();
		String password = jobj.get("Password").getAsString();
		String companyname = jobj.get("Company Name").getAsString();
		String website = jobj.get("Website").getAsString();
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
		company = new Company(companyname, emailid, password, website, address, description, logo, code); // need
																											// to
																											// encrypt
																											// password
		compRepo.save(company);

		request.getSession().setAttribute("loggedIn", "employer");
		request.getSession().setAttribute("email", emailid);

		sendEmail(emailid, "verification code", "your verification code is " + company.getVerificationcode());
		String msg = "Employer with id " + company.getCompanyid() + " is created successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED); // need to send an
																// email
																// notification
																// as well.
	}

	// Job seeker profile create -- should be same for update as well
	@RequestMapping(value = "/users/profile/{userid}", method = { RequestMethod.POST }) // need
																						// to
																						// change
																						// the
																						// entry
																						// point
	public ResponseEntity<?> createUserProfile(@PathVariable("userid") Long userid, String firstname, String lastname,
			@RequestParam(value = "picture", defaultValue = "null") String picture, // data
																					// type
																					// for
																					// image?
			@RequestParam(value = "intro", defaultValue = "null") String intro, String workex, String education,
			String skills, String phone) { // check for data type of education
											// (double?)
		User user = userRepo.findOne(userid);
		if (user == null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.NOT_FOUND.value(), "User with id " + userid + "not found"),
					HttpStatus.NOT_FOUND);
		}
		Profile profile = profileRepo.findOne(userid);
		if (profile == null) {
			// List<String> skillList = Arrays.asList(skills.split("\\,"));
			// TODO: check if verified
			profile = new Profile(userid, firstname, lastname, picture, intro, workex, education, skills, phone);
			profileRepo.save(profile);
		} else {
			profileRepo.updateProfile(firstname, lastname, picture, intro, workex, education, skills, phone, userid);
		}

		String msg = "Profile with userid " + userid + "is updated successfully";
		return new ResponseEntity<>(msg, HttpStatus.OK); // need to send an
															// email
															// notification as
															// well.
	}

	// Post a job
	@RequestMapping(value = "/jobs/post/{companyid}", method = { RequestMethod.POST })
	public ResponseEntity<?> postJob(@PathVariable("companyid") Long companyid, String job_title, String skills,
			String desc, String location, int salary, JobStatus status) { // OPEN,
																			// FILLED,
																			// CANCELLED
		// List<String> skillList = Arrays.asList(skills.split("\\,"));
		Company company = compRepo.findOne(companyid);
		if (company == null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.NOT_FOUND.value(), "Company with id " + companyid + "not found"),
					HttpStatus.NOT_FOUND);
		}
		// TODO: check if verified
		Job job = new Job(job_title, skills, desc, location, salary, status, company);
		jobRepo.save(job);

		String msg = "Job with id " + job.getJobid() + "is posted successfully";
		return new ResponseEntity<>(msg, HttpStatus.CREATED);
	}

	// Update a job
	@RequestMapping(value = "/jobs/{id}", method = { RequestMethod.PUT })
	public ResponseEntity<?> updateJob(@PathVariable("id") Long id, String job_title, String skills, String desc,
			String location, int salary, JobStatus status) {
		// company id and name should be taken from company table based on login
		Job job = jobRepo.findOne(id);
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(), "Not found"),
					HttpStatus.NOT_FOUND);
		}

		jobRepo.updateJobDetails(job_title, skills, desc, location, salary, status, id);

		String msg = "Job with id " + id + "is updated successfully";
		return new ResponseEntity<>(msg, HttpStatus.OK);
	}

	// Job search by job seeker
	@RequestMapping(value = "/jobs/search/{searchTerm}/{companyName}/{location}/{salaryRange}", method = {
			RequestMethod.GET })
	public ResponseEntity<?> searchJobUser(HttpServletRequest request, @PathVariable("searchTerm") String freeText,
			@PathVariable("companyName") String companyname, @PathVariable("location") String location,
			@PathVariable("salaryRange") int salary) throws UnsupportedEncodingException {

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

	public void sendEmail(String to, String subject, String text) {
		try {
			String from = "cmpe275.project.grp36@gmail.com";
			springEmailService.send(from, to, subject, text);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
