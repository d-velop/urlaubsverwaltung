package org.synyx.urlaubsverwaltung.core.account.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.period.NowService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;

import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Provides calculation of used / left vacation days.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class VacationDaysService {

    private final WorkDaysService calendarService;
    private final NowService nowService;
    private final ApplicationService applicationService;

    @Autowired
    public VacationDaysService(WorkDaysService calendarService, NowService nowService,
        ApplicationService applicationService) {

        this.calendarService = calendarService;
        this.nowService = nowService;
        this.applicationService = applicationService;
    }

    /**
     * Calculates the total number of days that are left to be used for applying for leave.
     *
     * <p>NOTE: The calculation depends on the current date. If it's before April, the left remaining vacation days are
     * relevant for calculation and if it's after April, only the not expiring remaining vacation days are relevant for
     * calculation.</p>
     *
     * @param  account  {@link org.synyx.urlaubsverwaltung.core.account.domain.Account}
     *
     * @return  total number of left vacation days
     */
    public BigDecimal calculateTotalLeftVacationDays(Account account) {

        VacationDaysLeft vacationDaysLeft = getVacationDaysLeft(account);

        // it's before April - the left remaining vacation days must be used
        if (DateUtil.isBeforeApril(nowService.now()) && account.getYear() == nowService.currentYear()) {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDays());
        } else {
            // it's after April - only the left not expiring remaining vacation days must be used
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
    }

    public BigDecimal calculateTotalLeftVacationDaysUpTo(Account account, DateMidnight lastMilestone) {
        VacationDaysLeft vacationDaysLeft = getVacationDaysLeftUpTo(account, lastMilestone);

        if (DateUtil.isBeforeApril(lastMilestone)) {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDays());
        } else {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
    }

    public VacationDaysLeft getVacationDaysLeftUpTo(Account account, DateMidnight lastMilestone) {
        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        BigDecimal daysBeforeApril = getUsedDaysBeforeAprilUpTo(account, lastMilestone);
        BigDecimal daysAfterApril = getUsedDaysAfterAprilUpTo(account, lastMilestone);

        return VacationDaysLeft.builder()
            .withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring)
            .forUsedDaysBeforeApril(daysBeforeApril)
            .forUsedDaysAfterApril(daysAfterApril)
            .get();
    }

    public VacationDaysLeft getVacationDaysLeft(Account account) {

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        BigDecimal daysBeforeApril = getUsedDaysBeforeApril(account);
        BigDecimal daysAfterApril = getUsedDaysAfterApril(account);

        return VacationDaysLeft.builder()
            .withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring)
            .forUsedDaysBeforeApril(daysBeforeApril)
            .forUsedDaysAfterApril(daysAfterApril)
            .get();
    }

    BigDecimal getUsedDaysBeforeAprilUpTo(Account account, DateMidnight lastMilestone) {
        DateMidnight firstOfJanuary = DateUtil.getFirstDayOfMonth(lastMilestone.getYear(), DateTimeConstants.JANUARY);
        DateMidnight lastOfMarch = DateUtil.getLastDayOfMonth(lastMilestone.getYear(), DateTimeConstants.MARCH);
        if (lastMilestone.compareTo(lastOfMarch) > 0) {
            // Part of our timespan is after April.
            return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastOfMarch);
        } else {
            // Everything before April.
            return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastMilestone);
        }
    }

    BigDecimal getUsedDaysAfterAprilUpTo(Account account, DateMidnight lastMilestone) {
        DateMidnight firstOfApril = DateUtil.getFirstDayOfMonth(lastMilestone.getYear(), DateTimeConstants.APRIL);
        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfApril, lastMilestone);
    }

    BigDecimal getUsedDaysBeforeApril(Account account) {

        DateMidnight firstOfJanuary = DateUtil.getFirstDayOfMonth(account.getYear(), DateTimeConstants.JANUARY);
        DateMidnight lastOfMarch = DateUtil.getLastDayOfMonth(account.getYear(), DateTimeConstants.MARCH);

        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastOfMarch);
    }


    BigDecimal getUsedDaysAfterApril(Account account) {

        DateMidnight firstOfApril = DateUtil.getFirstDayOfMonth(account.getYear(), DateTimeConstants.APRIL);
        DateMidnight lastOfDecember = DateUtil.getLastDayOfMonth(account.getYear(), DateTimeConstants.DECEMBER);

        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfApril, lastOfDecember);
    }


    BigDecimal getUsedDaysBetweenTwoMilestones(Person person, DateMidnight firstMilestone, DateMidnight lastMilestone) {

        // get all applications for leave
        List<Application> allApplicationsForLeave = applicationService.getApplicationsForACertainPeriodAndPerson(
                firstMilestone, lastMilestone, person);

        // filter them since only waiting and allowed applications for leave of type holiday are relevant
        List<Application> applicationsForLeave = allApplicationsForLeave.stream()
                .filter(input ->
                            VacationCategory.HOLIDAY.equals(input.getVacationType().getCategory())
                            && (input.hasStatus(ApplicationStatus.WAITING)
                                || input.hasStatus(ApplicationStatus.ALLOWED)))
                .collect(Collectors.toList());

        BigDecimal usedDays = BigDecimal.ZERO;

        for (Application applicationForLeave : applicationsForLeave) {
            DateMidnight startDate = applicationForLeave.getStartDate();
            DateMidnight endDate = applicationForLeave.getEndDate();

            if (startDate.isBefore(firstMilestone)) {
                startDate = firstMilestone;
            }

            if (endDate.isAfter(lastMilestone)) {
                endDate = lastMilestone;
            }

            usedDays = usedDays.add(calendarService.getWorkDays(applicationForLeave.getDayLength(), startDate, endDate,
                        person));
        }

        return usedDays;
    }
}
