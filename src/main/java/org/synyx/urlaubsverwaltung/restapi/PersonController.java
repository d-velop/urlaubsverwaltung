package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Persons", description = "Get information about the persons of the application")
@Controller("restApiPersonController")
public class PersonController {

    private static final String ROOT_URL = "/persons";

    @Autowired
    private PersonService personService;

    @ApiOperation(
        value = "Get all active persons of the application", notes = "Get all active persons of the application"
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public PersonListResponse persons(
        @ApiParam(value = "LDAP Login")
        @RequestParam(value = "ldap", required = false)
        String ldapName) {

        List<Person> persons = new ArrayList<Person>();

        if (ldapName == null) {
            persons = personService.getActivePersons();
        } else {
            Optional<Person> person = personService.getPersonByLogin(ldapName);

            if (person.isPresent()) {
                persons.add(person.get());
            }
        }

        List<PersonResponse> personResponses = Lists.transform(persons, new Function<Person, PersonResponse>() {

                    @Override
                    public PersonResponse apply(Person person) {

                        return new PersonResponse(person);
                    }
                });

        return new PersonListResponse(personResponses);
    }
}
