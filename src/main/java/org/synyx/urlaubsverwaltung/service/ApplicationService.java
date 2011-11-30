package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * use this service to access to the application-data (who, how many days, ...)
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface ApplicationService {

    /**
     * use this to save an edited application
     *
     * @param  application  the application to be saved
     */
    void save(Application application);


    /**
     * use this to set a application to allowed (only boss)
     *
     * @param  application  the application to be edited
     */
    void allow(Application application);


    /**
     * use this to set a application to rejected (only boss)
     *
     * @param  application  the application to be edited
     * @param  reasonToReject  the reason of the rejection
     */
    void reject(Application application, Person boss, String reasonToReject);


    /**
     * if a user makes an application for leave, the application's state is set to waiting until a boss allows it
     *
     * @param  application
     */
    void wait(Application application);


    /**
     * application's state is set to cancelled if user cancels vacation
     *
     * @param  application
     */
    void cancel(Application application);


    /**
     * sick days are added to application's attribute sickDays and the number of sick days is credited to person's leave
     * account, because sick days are not counted among to holidays
     *
     * @param  application
     * @param  sickDays
     */
    void addSickDaysOnHolidaysAccount(Application application, double sickDays);


    /**
     * use this to get all applications of a certain person
     *
     * @param  person  the person you want to get the applications of
     *
     * @return  returns all applications of a person as a list of Application-objects
     */
    List<Application> getAllApplicationsForPerson(Person person);


    /**
     * use this to get all applications of a certain state (like waiting)
     *
     * @param  state
     *
     * @return  returns all applications of a state as a list of application-objects
     */
    List<Application> getAllApplicationsByState(ApplicationStatus state);


    /**
     * use this to get all applications with vacation time between startDate x and endDate y
     *
     * @param  startDate
     * @param  endDate
     *
     * @return
     */
    List<Application> getAllApplicationsForACertainTime(DateMidnight startDate, DateMidnight endDate);


    /**
     * signs an application with the private key of the signing boss
     *
     * @param  application
     * @param  boss
     */
    void signApplicationByBoss(Application application, Person boss);


    /**
     * signs an application with the private key of the signing user (applicant)
     *
     * @param  application
     * @param  user
     */
    void signApplicationByUser(Application application, Person user);


    /**
     * check if application is valid and may be send to boss to be allowed or rejected or if person's leave account has
     * too little residual number of vacation days, so that taking holiday isn't possible
     *
     * @param  application
     *
     * @return  boolean: true if application is okay, false if there are too little residual number of vacation days
     */
    boolean checkApplication(Application application);
}