package com.springJava.jobTracker.controller;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	ApplicationRepo appRepo;

	/**
	 * 5) Employer/User Login [POST request]
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/loginFoo", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<?> loginFoo(HttpServletRequest request, HttpEntity<String> httpEntity) throws IOException {
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
				request.getSession().setAttribute("loggedIn", "user");
				request.getSession().setAttribute("email", user.getEmailid());
				responseStatus = HttpStatus.OK;
			} else { // incorrect pwd
				responseStatus = HttpStatus.FORBIDDEN;
			}
		} else {
			Company company = compRepo.findByEmailid(emailid);
			if (company != null) {
				if (pwd.equals(company.getPassword())) {
					request.getSession().setAttribute("loggedIn", "employer");
					request.getSession().setAttribute("email", company.getEmailid());
					responseStatus = HttpStatus.OK;
				} else {// incorrect pwd
					responseStatus = HttpStatus.FORBIDDEN;
				}
			} else {// company, user not found
				responseStatus = HttpStatus.NOT_FOUND;
			}
		}
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseStatus);
		return responseEntity;
	}

	private String readBody(HttpServletRequest request) throws IOException {
		BufferedReader bodyReader = request.getReader();
		StringBuffer buffer = new StringBuffer();
		String lineRead = bodyReader.readLine();
		while (lineRead != null) {
			buffer.append(lineRead);
		}
		return buffer.toString();
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
	 * Body expects param: Email
	 * 
	 * 
	 * @param request
	 * @param jobId
	 * @return
	 */
	@RequestMapping(value = "/jobs/view/{jobId}/apply", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<?> applyForJob(HttpServletRequest request, @PathVariable Long jobId) {
		Job job = jobRepo.findOne(jobId);
		if (job == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Job with id " + String.valueOf(jobId) + " Not found"), HttpStatus.NOT_FOUND);
		}
		User user = userRepo.findByEmailid(request.getParameter("Email"));
		if (user == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"User with email " + request.getParameter("Email") + " Not found"), HttpStatus.NOT_FOUND);
		}
		ApplicationStatus status = ApplicationStatus.PENDING;
		ApplicationType type = ApplicationType.APPLIED;
		Application application = new Application(user, job, type, status);

		appRepo.save(application);
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
	@RequestMapping(value = "/employers/{companyId}/update", method = { RequestMethod.PUT })
	@ResponseBody
	public ResponseEntity<?> updateCompany(HttpServletRequest request, @PathVariable Long companyId) {
		Company company = compRepo.findOne(companyId);
		if (company == null) {
			return new ResponseEntity<ControllerError>(new ControllerError(HttpStatus.NOT_FOUND.value(),
					"Company with id " + String.valueOf(companyId) + " Not found"), HttpStatus.NOT_FOUND);
		}
		String emailid = request.getParameter("Email ID");
		String name = request.getParameter("Company Name");
		String website = request.getParameter("Website");
		String address = request.getParameter("Address_Headquarters");
		String description = request.getParameter("Description");
		String logo_image = request.getParameter("Logo_Image_URL");
		if (emailid != null)
			company.setEmailid(emailid);
		if (name != null)
			company.setName(name);
		if (website != null)
			company.setWebsite(website);
		if (address != null)
			company.setAddress(address);
		if (description != null)
			company.setDescription(description);
		if (logo_image != null)
			company.setLogo_image(logo_image);

		compRepo.save(company);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
