package com.springJava.jobTracker.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;


@Entity
@Table(name = "profile")
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "profileid", unique = true, nullable = false)
	private Long profileid;

	@Column(name = "firstname", nullable = false)
	private String firstname;

	@Column(name = "lastname", nullable = false)
	private String lastname;

	private String imageloc;

	private String intro;

	private String workex;

	@Column(nullable = false)
	private String education;

	@Column(nullable = false)
	@ElementCollection
	private List<String> skills;
	
	private String phone;
	
	public Profile(Long userid, String firstname, String lastname, String imageloc, String intro, String workex,
			String education, List<String> skills, String phone) {
		super();
		this.profileid = userid;
		this.firstname = firstname;
		this.lastname = lastname;
		this.imageloc = imageloc;
		this.intro = intro;
		this.workex = workex;
		this.education = education;
		this.skills = skills;
		this.phone = phone;
	}

	public Profile() {
	}

	public Long getUserid() {
		return profileid;
	}

	public void setUserid(Long userid) {
		this.profileid = userid;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getImageloc() {
		return imageloc;
	}

	public void setImageloc(String imageloc) {
		this.imageloc = imageloc;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getWorkex() {
		return workex;
	}

	public void setWorkex(String workex) {
		this.workex = workex;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	
}
