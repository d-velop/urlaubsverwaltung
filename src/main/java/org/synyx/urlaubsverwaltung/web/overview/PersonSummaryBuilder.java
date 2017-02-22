package org.synyx.urlaubsverwaltung.web.overview;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.web.application.ApplicationForLeave;

@Component
public class PersonSummaryBuilder {
	private AccountService accountService;
	private ApplicationService applicationService;
	private VacationDaysService vacationDaysService;
	private WorkDaysService calendarService;
	
    @Autowired
    public PersonSummaryBuilder(AccountService accountService, ApplicationService applicationService,
    	VacationDaysService vacationDaysService, WorkDaysService calendarService) {

        this.accountService = accountService;
        this.applicationService = applicationService;
        this.vacationDaysService = vacationDaysService;
        this.calendarService = calendarService;
    }
    
    public PersonSummary build(Person person, DateMidnight upTo) {
    	Optional<Account> account = accountService.getHolidaysAccount(upTo.getYear(), person);
    	if (!account.isPresent()) {
    		return new PersonSummary(person, null, null);
    	}
    	
    	BigDecimal vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDaysUpTo(account.get(), upTo);
    	
    	DateMidnight startOfMonth = DateUtil.getFirstDayOfMonth(upTo.getYear(), upTo.getMonthOfYear());
    	
    	List<ApplicationForLeave> applications = applicationService.getApplicationsForACertainPeriodAndPerson(startOfMonth, upTo, person).stream()
    			.map(app -> new ApplicationForLeave(app, calendarService))
    			.sorted(dateComparator())
    			.collect(Collectors.toList());
    	
    	return new PersonSummary(person, vacationDaysLeft, applications);
    }
    
    private Comparator<ApplicationForLeave> dateComparator() {

        return (o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate());
    }

}
