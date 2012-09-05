package org.synyx.urlaubsverwaltung.service;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateMidnight;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;

/**
 * This service handles the validation of applications for leave concerning overlapping, 
 * i.e. if there is already an existent application for leave in the same period, 
 * the user may not apply for leave in this period.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class OverlapService {
    
    private ApplicationDAO applicationDAO;
    
    @Autowired
    public OverlapService(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }
    
    
    /**
     * Check if new application is overlapping with an existent application. There are three possible cases: (1) The
     * period of the new application has no overlap at all with existent applications; i.e. you can calculate the normal
     * way and save the application if there are enough vacation days on person's holidays account. (2) The period of
     * the new application is element of an existent application's period; i.e. the new application is not necessary
     * because there is already an existent application for this period. (3) The period of the new application is part
     * of an existent application's period, but for a part of it you could apply new vacation; i.e. user must be asked
     * if he wants to apply for leave for the not overlapping period of the new application.
     *
     * @param  application  (the new application)
     *
     * @return  OverlapCase (Enum)
     */
    public OverlapCase checkOverlap(Application application) {

        if (application.getHowLong() == DayLength.MORNING) {
            return checkOverlapForMorning(application);
        } else if (application.getHowLong() == DayLength.NOON) {
            return checkOverlapForNoon(application);
        } else {
            // check if there are existent ANY applications (full day and half day)
            List<Application> apps = applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(application.getStartDate().toDate(), application.getEndDate().toDate(), application.getPerson());

            return getCaseOfOverlap(application, apps);
        }

    }
    
    
        /**
     * With this method you get a list of existent applications that overlap with the given period (information about
     * person and period in application) and have the given day length.
     *
     * @param  Application  app
     * @param  DayLength  length
     *
     * @return  List<Application> applications overlapping with the period of the given application
     */
    private List<Application> getApplicationsByPeriodAndDayLength(Application app, DayLength length) {

        if (length == DayLength.MORNING) {
            return applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.MORNING);
        } else if (length == DayLength.NOON) {
            return applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.NOON);
        } else {
            return applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.FULL);
        }
    }

    /**
     * Method to check if the given application with day length "FULL" may be applied or not. (are there existent
     * applications for this period or not?)
     *
     * @param  application
     *
     * @return  int 1 for check is alright: application for leave is valid. 2 or 3 for invalid application for leave.
     */
    protected OverlapCase checkOverlapForFullDay(Application application) {

        // check if there are existent ANY applications (full day and half day)
        List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.FULL);

        return getCaseOfOverlap(application, apps);
    }

    /**
     * Method to check if the given application with day length "MORNING" may be applied or not. (are there existent
     * applications for this period or not?)
     *
     * @param  application
     *
     * @return  int 1 for check is alright: application for leave is valid. 2 or 3 for invalid application for leave.
     */
    protected OverlapCase checkOverlapForMorning(Application application) {

        // check if there are overlaps with full day periods
        if (checkOverlapForFullDay(application) == OverlapCase.NO_OVERLAPPING) {
            // if there are no overlaps with full day periods, you have to check if there are overlaps with half day
            // (MORNING) periods
            List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.MORNING);

            return getCaseOfOverlap(application, apps);
        } else {
            return checkOverlapForFullDay(application);
        }
    }

    /**
     * Method to check if the given application with day length "NOON" may be applied or not. (are there existent
     * applications for this period or not?)
     *
     * @param  application
     *
     * @return  int 1 for check is alright: application for leave is valid. 2 or 3 for invalid application for leave.
     */
    protected OverlapCase checkOverlapForNoon(Application application) {

        // check if there are overlaps with full day periods
        if (checkOverlapForFullDay(application) == OverlapCase.NO_OVERLAPPING) {
            // if there are no overlaps with full day periods, you have to check if there are overlaps with half day
            // (NOON) periods
            List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.NOON);

            return getCaseOfOverlap(application, apps);
        } else {
            return checkOverlapForFullDay(application);
        }
    }

    /**
     * This method contains the logic how to check if there are existent overlapping applications for the given period;
     * use this method only for full day applications.
     *
     * @param  application
     *
     * @return  1 if there is no overlap at all - 2 if the given period is element of (an) existent application(s) - 3
     *          if the new application is part of an existent application's period, but for a part of it you could apply
     *          new vacation
     */
    private OverlapCase getCaseOfOverlap(Application application, List<Application> apps) {

        // case (1): no overlap at all
        if (apps.isEmpty()) {
            /* (1) The
             * period of the new application has no overlap at all with existent applications; i.e. you can calculate
             * the normal way and save the application if there are enough vacation days on person's holidays account.
             */
            return OverlapCase.NO_OVERLAPPING;
        } else {
            // case (2) or (3): overlap

            List<Interval> listOfOverlaps = getListOfOverlaps(application, apps);

            if (application.getHowLong() == DayLength.FULL) {
                List<Interval> listOfGaps = getListOfGaps(application, listOfOverlaps);

                // gaps between the intervals mean that you can apply vacation for this periods
                // this is case (3)
                if (listOfGaps.size() > 0) {
                    /* (3) The period of the new application is part
                     * of an existent application's period, but for a part of it you could apply new vacation; i.e. user
                     * must be asked if he wants to apply for leave for the not overlapping period of the new
                     * application.
                     */
                    return OverlapCase.FULLY_OVERLAPPING.PARTLY_OVERLAPPING;
                }
            }
            // no gaps mean that period of application is element of other periods of applications
            // i.e. you have no free periods to apply vacation for
            // this is case (2)

            /* (2) The period of
             * the new application is element of an existent application's period; i.e. the new application is not
             * necessary because there is already an existent application for this period.
             */
            return OverlapCase.FULLY_OVERLAPPING;
        }
    }

    /**
     * This method gets a list of applications that overlap with the period of the given application; all overlapping
     * intervals are put in this list for further checking (e.g. if there are gaps) and for getting the case of overlap
     * (1, 2 or 3)
     *
     * @param  application
     * @param  apps
     *
     * @return  List<Interval> list of overlaps
     */
    private List<Interval> getListOfOverlaps(Application application, List<Application> apps) {

        Interval interval = new Interval(application.getStartDate(), application.getEndDate());

        List<Interval> listOfOverlaps = new ArrayList<Interval>();

        for (Application a : apps) {
            Interval inti = new Interval(a.getStartDate(), a.getEndDate());
            Interval overlap = inti.overlap(interval);

            // because intervals are inclusive of the start instant, but exclusive of the end instant
            // you have to check if end of interval a is start of interval b

            if (inti.getEnd().equals(interval.getStart())) {
                overlap = new Interval(interval.getStart(), interval.getStart());
            }

            if (inti.getStart().equals(interval.getEnd())) {
                overlap = new Interval(interval.getEnd(), interval.getEnd());
            }

            // check if they really overlap, else value of overlap would be null
            if (overlap != null) {
                listOfOverlaps.add(overlap);
            }
        }

        return listOfOverlaps;
    }

    /**
     * This method gets a list of overlaps and checks with the given application if there are any gaps where a user
     * could apply for leave (these gaps are not yet applied for leave) - may be a feature in later version.
     *
     * @param  application
     * @param  listOfOverlaps
     *
     * @return  List<Interval> list of gaps
     */
    private List<Interval> getListOfGaps(Application application, List<Interval> listOfOverlaps) {

        List<Interval> listOfGaps = new ArrayList<Interval>();

        // check start and end points

        DateMidnight firstOverlapStart = listOfOverlaps.get(0).getStart().toDateMidnight();
        DateMidnight lastOverlapEnd = listOfOverlaps.get(listOfOverlaps.size() - 1).getEnd().toDateMidnight();

        if (application.getStartDate().isBefore(firstOverlapStart)) {
            Interval gapStart = new Interval(application.getStartDate(), firstOverlapStart);
            listOfGaps.add(gapStart);
        }

        if (application.getEndDate().isAfter(lastOverlapEnd)) {
            Interval gapEnd = new Interval(lastOverlapEnd, application.getEndDate());
            listOfGaps.add(gapEnd);
        }

        // check if intervals abut or gap
        for (int i = 0; (i + 1) < listOfOverlaps.size(); i++) {
            // if they don't abut, you can calculate the gap
            // test if end of interval is equals resp. one day plus of start of other interval
            // e.g. if period 1: 16.-18. and period 2: 19.-20 --> they abut
            // e.g. if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
            if (intervalsHaveGap(listOfOverlaps.get(i), listOfOverlaps.get(i + 1))) {
                Interval gap = listOfOverlaps.get(i).gap(listOfOverlaps.get(i + 1));
                listOfGaps.add(gap);
            }
        }

        return listOfGaps;
    }

    /**
     * This method checks if the two given intervals have a gap or if they abut. Some examples: (1) if period 1: 16.-18.
     * and period 2: 19.-20 --> they abut (2) if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
     *
     * @param  i1
     * @param  i2
     *
     * @return  true if they have a gap between or false if they have no gap
     */
    private boolean intervalsHaveGap(Interval i1, Interval i2) {

        // test if end of interval is equals resp. one day plus of start of other interval
        if (!(i1.getEnd().toDateMidnight().equals(i2.getStart().toDateMidnight())
                || i1.getEnd().toDateMidnight().plusDays(1).equals(i2.getStart().toDateMidnight()))) {
            return true;
        } else {
            return false;
        }
    }
}