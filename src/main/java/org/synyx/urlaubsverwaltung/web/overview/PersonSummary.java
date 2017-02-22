package org.synyx.urlaubsverwaltung.web.overview;

import java.math.BigDecimal;
import java.util.List;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.application.ApplicationForLeave;

public class PersonSummary {
	private Person person;
	private BigDecimal vacationDaysLeft;
	private List<ApplicationForLeave> applications;
	
	public PersonSummary(Person person, BigDecimal vacationDaysLeft, List<ApplicationForLeave> applications) {
		this.person = person;
		this.vacationDaysLeft = vacationDaysLeft;
		this.applications = applications;
	}
	
	public Person getPerson() {
		return this.person;
	}
	
	public BigDecimal getVacationDaysLeft() {
		return this.vacationDaysLeft;
	}
	
	public List<ApplicationForLeave> getApplications() {
		return this.applications;
	}
}
