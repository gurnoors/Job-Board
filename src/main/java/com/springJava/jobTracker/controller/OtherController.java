package com.springJava.jobTracker.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.springJava.jobTracker.model.Company;
import com.springJava.jobTracker.model.User;
import com.springJava.jobTracker.repo.CompanyRepo;
import com.springJava.jobTracker.repo.JobRepo;
import com.springJava.jobTracker.repo.ProfileRepo;
import com.springJava.jobTracker.repo.UserRepo;

import javax.servlet.http.HttpServletRequest;

@RestController
public class OtherController {
	@Autowired
	JobRepo jobRepo;
	@Autowired
	UserRepo userRepo;
	@Autowired
	CompanyRepo compRepo;
	@Autowired
	ProfileRepo profileRepo;

	/**
	 * user, company login
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = { RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<?> login(HttpServletRequest request) {
		HttpStatus responseStatus = null;
		String emailid = request.getParameter("Email ID");
		String pwd = request.getParameter("Password");
		// later: (in create requests) get body and map it to entity using JAXB
		// or GSON
		// request.getReader()
		User user = userRepo.findByEmailid(emailid);
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

}
