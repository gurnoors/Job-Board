package com.springJava.jobTracker.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.springJava.jobTracker.model.Application;
import com.springJava.jobTracker.model.ApplicationStatus;
import com.springJava.jobTracker.model.ApplicationType;
import com.springJava.jobTracker.model.Company;
import com.springJava.jobTracker.model.Job;
import com.springJava.jobTracker.model.Profile;
import com.springJava.jobTracker.model.User;
import com.springJava.jobTracker.repo.ApplicationRepo;
import com.springJava.jobTracker.repo.CompanyRepo;
import com.springJava.jobTracker.repo.JobRepo;
import com.springJava.jobTracker.repo.ProfileRepo;
import com.springJava.jobTracker.repo.UserRepo;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
public class OtherController {
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static WebController controller = new WebController();

	@Autowired
	JobRepo jobRepo;
	@Autowired
	UserRepo userRepo;
	@Autowired
	CompanyRepo compRepo;
	@Autowired
	ProfileRepo profileRepo;
	@Autowired
	ApplicationRepo appRepo;

	/**
	 * 5) Employer/User Login [POST request]
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/login", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<?> login(HttpServletRequest request, HttpEntity<String> httpEntity) throws IOException {
		boolean isUser = false;
		HttpStatus responseStatus = null;
		request.setCharacterEncoding("UTF-8");
		String body = httpEntity.getBody();

		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String emailid = jobj.get("Email ID").getAsString();
		String pwd = jobj.get("Password").getAsString();

		// String emailid = request.getParameter("Email ID");
		// String pwd = request.getParameter("Password");
		System.out.println(request.getHeader("Content-Type"));
		System.out.println(request.toString());
		System.out.println(emailid + " ---> " + pwd);
		User user = userRepo.findByEmailid(emailid);
		// TODO: check if verified
		if (user != null) {
			if (pwd.equals(user.getPassword())) {
				if (!user.isStatus()) { // did not verify the email
					/*
					 * For testing, do: update table user as u set u.status=1;
					 */
					return new ResponseEntity<ControllerError>(
							new ControllerError(HttpStatus.FORBIDDEN.value(), "User not verified"),
							HttpStatus.FORBIDDEN);
				}

				request.getSession().setAttribute("loggedIn", "user");
				request.getSession().setAttribute("email", user.getEmailid());
				responseStatus = HttpStatus.OK;
				isUser = true;
			} else { // incorrect pwd
				responseStatus = HttpStatus.FORBIDDEN;
			}
		} else {
			Company company = compRepo.findByEmailid(emailid);
			if (company != null) {
				if (pwd.equals(company.getPassword())) {
					if (!company.isStatus()) {
						return new ResponseEntity<ControllerError>(
								new ControllerError(HttpStatus.FORBIDDEN.value(), "Employer not verified"),
								HttpStatus.FORBIDDEN);
					}

					request.getSession().setAttribute("loggedIn", "employer");
					request.getSession().setAttribute("email", company.getEmailid());
					responseStatus = HttpStatus.OK;
					isUser = false;
				} else {// incorrect pwd
					responseStatus = HttpStatus.FORBIDDEN;
				}
			} else {// company, user not found
				responseStatus = HttpStatus.NOT_FOUND;
			}
		}
		
		ResponseEntity<String> responseEntity = null;
		if(isUser){
			responseEntity = new ResponseEntity<String>("user", responseStatus);;  
		}else{
			responseEntity = new ResponseEntity<String>("employer", responseStatus);
		}
		
		return responseEntity;
	}

	/**
	 * LOGOUT
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/logout", method = { RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<?> logout(HttpServletRequest request) {
		boolean alreadyLogged = false;
		if (request.getSession().getAttribute("loggedIn") == null) {
			alreadyLogged = true;
		}
		request.getSession().setAttribute("loggedIn", null);
		request.getSession().setAttribute("email", null);
		if (alreadyLogged) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.OK.value(), "Logged out. You were not logged in though ;P"),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

	/**
	 * 7) View a job from search results
	 * 
	 * @param jobId
	 * @return
	 */
	@RequestMapping(value = "/jobs/view/{jobId}", method = { RequestMethod.GET })
	public ResponseEntity<?> getJob(@PathVariable Long jobId) {
		Job job = jobRepo.findOne(jobId);
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(), "Not found"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Job>(job, HttpStatus.OK);
	}

	/**
	 * apply job
	 * 
	 * @param request
	 * @param jobId
	 * @return
	 */
	@RequestMapping(value = "/jobs/view/{jobId}/apply", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<?> applyForJob(HttpServletRequest request, @PathVariable Long jobId,
			HttpEntity<String> httpEntity) {
		Job job = jobRepo.findOne(jobId);
		// sanity checks
		if (!((String) request.getSession().getAttribute("loggedIn")).equals("user")) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.FORBIDDEN.value(), "User not logged in"), HttpStatus.FORBIDDEN);
		}

		// read body
		String body = httpEntity.getBody();
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();
		String emailid = (String) request.getSession().getAttribute("email");
		System.out.println("inside apply : Email ---> " + emailid);

		// sanity checks
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Job with id " + String.valueOf(jobId) + " Not found"), HttpStatus.NOT_FOUND);
		}
		User user = userRepo.findByEmailid(emailid);
		if (user == null) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.NOT_FOUND.value(), "User with email " + emailid + " Not found"),
					HttpStatus.NOT_FOUND);
		}
		Profile profile = profileRepo.findOne(user.getUserid());
		if (profile == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.FORBIDDEN.value(),
					"Please create your profile before applying for a job"), HttpStatus.FORBIDDEN);
		}

		ApplicationStatus status = ApplicationStatus.PENDING;

		ApplicationType type = null;
		if (jobj.get("applicationType") != null) {
			switch (jobj.get("applicationType").getAsString()) {
			case "interested":
				type = ApplicationType.INTERESTED;
				break;
			case "applied":
				type = ApplicationType.APPLIED;
				break;
			default:
				return new ResponseEntity<ControllerError>(
						new ControllerError(HttpStatus.BAD_REQUEST.value(),
								"\"applicationType\" can be either \"interested\" or \"applied\""),
						HttpStatus.BAD_REQUEST);
			}
		} else {
			type = ApplicationType.APPLIED;
		}
		Application application = new Application(user, job, type, status);
		appRepo.save(application);

		String subject = "Thank you for applying to " + job.getCompany().getName() + " via Job-Board";
		try {
			controller.sendEmail(user.getEmailid(), job.getDescription(), subject);
		} catch (Exception e) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.OK.value(), "Unable to send email. But applied to job"),
					HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * 
	 * 10) Employer details Update [PUT request] Request Body
	 * 
	 * { Email ID: “”, Company Name: “”, Website:””, Address_Headquarters: ””,
	 * Description:””, Logo_Image_URL : ”” }
	 * 
	 * @param request
	 * @param companyId
	 * @return
	 */
	@RequestMapping(value = "/employers/update", method = { RequestMethod.PUT })
	@ResponseBody
	public ResponseEntity<?> updateCompany(HttpServletRequest request, 
			HttpEntity<String> httpEntity) {
		if (request.getSession().getAttribute("loggedIn") == null
				|| !((String) request.getSession().getAttribute("loggedIn")).equals("employer")) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.FORBIDDEN.value(), "Employer not logged in"), HttpStatus.FORBIDDEN);
		}
		String email = (String) request.getSession().getAttribute("email");
		Company company = compRepo.findByEmailid(email);
		if (company == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Company with emailid " + email + " Not found"), HttpStatus.NOT_FOUND);
		}

		String body = httpEntity.getBody();
		// read body
		JsonElement jelem = gson.fromJson(body, JsonElement.class);
		JsonObject jobj = jelem.getAsJsonObject();

		JsonElement name = jobj.get("Company Name");
		JsonElement website = jobj.get("Website");
		JsonElement address = jobj.get("Address_Headquarters");
		JsonElement description = jobj.get("Description");
		JsonElement logo_image = jobj.get("Logo_Image_URL");
		if (name != null)
			company.setName(name.getAsString());
		if (website != null)
			company.setWebsite(website.getAsString());
		if (address != null)
			company.setAddress(address.getAsString());
		if (description != null)
			company.setDescription(description.getAsString());
		if (logo_image != null)
			company.setLogo_image(logo_image.getAsString());

		compRepo.save(company);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * 12) get all jobs for an employer (the one logged in) [GET request]
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/employer/jobs", method = { RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<?> getAllJobsForEmployer(HttpServletRequest request) {
		System.out.println("isLogged IN --> " + (String) request.getSession().getAttribute("employer"));
		System.out.println((String) request.getSession().getAttribute("email"));
		if (!((String) request.getSession().getAttribute("loggedIn")).equals("employer")) {
			return new ResponseEntity<ControllerError>(
					new ControllerError(HttpStatus.FORBIDDEN.value(), "Employer not logged in"), HttpStatus.FORBIDDEN);
		}

		String email = (String) request.getSession().getAttribute("email");
		Company company = compRepo.findByEmailid(email);
		List<Job> jobs = company.getJobs();

		return new ResponseEntity<List<Job>>(jobs, HttpStatus.OK);
	}

}
