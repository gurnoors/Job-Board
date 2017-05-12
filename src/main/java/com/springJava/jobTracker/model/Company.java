package com.springJava.jobTracker.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="company")
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long companyid;

	private String name;

	@Column(unique = true, nullable = false)
	private String registered_email;

	private String website;
	private String address;
	private String description;
	private String logo_image;
	private String password;
	
	@OneToMany(mappedBy = "company")
	private List<Job> jobs;

	public Company() {
	}
	
	public Company(Long companyid, String name, String registered_email, String website, String address,
			String description, String logo_image, String password, List<Job> jobs) {
		super();
		this.companyid = companyid;
		this.name = name;
		this.registered_email = registered_email;
		this.website = website;
		this.address = address;
		this.description = description;
		this.logo_image = logo_image;
		this.password = password;
		this.jobs = jobs;
	}

	public Long getCompanyid() {
		return companyid;
	}

	public void setCompanyid(Long companyid) {
		this.companyid = companyid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegistered_email() {
		return registered_email;
	}

	public void setRegistered_email(String registered_email) {
		this.registered_email = registered_email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogo_image() {
		return logo_image;
	}

	public void setLogo_image(String logo_image) {
		this.logo_image = logo_image;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}



}
