package com.springJava.jobTracker.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="job")
public class Job {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long jobid;

	private String jobtitle;
	
	@Column(nullable = false)
	private String companyname;
	
	@ElementCollection
	private List<String> skill;
	
	private String description;
	private String location;
	private int salary;
	private JobStatus status;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="companyid")
	private Company company;
	
	public Job(Long jobid, String jobtitle, String companyname, List<String> skill, String description, String location,
			int salary, JobStatus status, Company company) {
		super();
		this.jobid = jobid;
		this.jobtitle = jobtitle;
		this.companyname = companyname;
		this.skill = skill;
		this.description = description;
		this.location = location;
		this.salary = salary;
		this.status = status;
		this.company = company;
	}

	public Job() {
	}

	public Long getJobid() {
		return jobid;
	}

	public void setJobid(Long jobid) {
		this.jobid = jobid;
	}

	public String getJobtitle() {
		return jobtitle;
	}

	public void setJobtitle(String jobtitle) {
		this.jobtitle = jobtitle;
	}

	public String getCompanyname() {
		return companyname;
	}

	public void setCompanyname(String companyname) {
		this.companyname = companyname;
	}

	public List<String> getSkill() {
		return skill;
	}

	public void setSkill(List<String> skill) {
		this.skill = skill;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

}
