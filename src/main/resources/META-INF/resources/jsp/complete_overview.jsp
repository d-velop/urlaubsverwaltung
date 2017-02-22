<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head/>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/application"/>

<uv:menu/>

<div class="content print--only-landscape">
    <div class="container">
    
    	<legend>
    		<spring:message code="complete_overview.title" />
    		<uv:month-selector month="${month}" hrefPrefix="${URL_PREFIX}/complete_overview?year=${year}&month=" />
    		<uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/complete_overview?month=${month}&year=" />
    	</legend>    
    
    	<c:forEach items="${person_summaries}" var="person_summary">
        	<div class="row">
            	<div class="col-xs-8">
            		<h3>${person_summary.person.firstName} ${person_summary.person.lastName}</h3>
            	</div>
            	<div class="col-xs-4">
            		<h3>${person_summary.vacationDaysLeft}</h3>
           		</div>
        	</div>
        	<div class="row">
        		<div class="col-xs-12">
                    <c:choose>
                    	<c:when test="${fn:length(person_summary.applications) gt 0}">
	                    	<ul>
		                    	<c:forEach items="${person_summary.applications}" var="application">
		                    		<li class="state ${application.status}">
		                    			<c:choose>
		                    				<c:when test="${application.startDate == application.endDate}">
		                    					<c:set var="APPLICATION_DATE">
		                    						<spring:message code="${application.weekDayOfStartDate}.short"/>,
		                    						<uv:date date="${application.startDate}"/>
		                    					</c:set>
		                    					<c:choose>
		                    						<c:when test="${application.startTime != null && application.endTime != null}">
		                    							<c:set var="APPLICATION_START_TIME">
		                    								<uv:time dateTime="${application.startDateWithTime}" />
		                    							</c:set>
		                    							<c:set var="APPLICATION_END_TIME">
		                    								<uv:time dateTime="${application.endDateWithTime}" />
		                    							</c:set>
		                    							<c:set var="APPLICATION_TIME">
		                    								<spring:message code="absence.period.time" arguments="${APPLICATION_START_TIME};${APPLICATION_END_TIME}" argumentSeparator=";" />
		                    							</c:set>
		                    							<spring:message code="absence.period.singleDay" arguments="${APPLICATION_DATE};${APPLICATION_TIME}" argumentSeparator=";" />
		                    						</c:when>
		                    						<c:otherwise>
		                    							<c:set var="APPLICATION_DAY_LENGTH">
		                    								<spring:message code="${application.dayLength}" />
		                    							</c:set>
		                    							<spring:message code="absence.period.singleDay" arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}" argumentSeparator=";" />
		                    						</c:otherwise>
		                    					</c:choose>
		                    				</c:when>
		                    				<c:otherwise>
		                    					<c:set var="APPLICATION_START_DATE">
		                    						<spring:message code="${application.weekDayOfStartDate}.short" />,
		                    						<uv:date date="${application.startDate}" />
		                    					</c:set>
		                    					<c:set var="APPLICATION_END_DATE">
		                    						<spring:message code="${application.weekDayOfEndDate}.short" />,
		                    						<uv:date date="${application.endDate}" />
		                    					</c:set>
		                    					<spring:message code="absence.period.multipleDays" arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}" argumentSeparator=";" />
		                    				</c:otherwise>
		                    			</c:choose>           
		                    			
		                    			<spring:message code="${application.status}" />       
		                    			
		                    			<c:if test="${application.status == 'WAITING'}">
		                    				<a class="positive" href="${URL_PREFIX}/application/${application.id}?action=allow&shortcut=true">
                                            	<spring:message code="action.allow" />
                                          	</a>
		                    			</c:if>
		                    		</li>
		                    	</c:forEach>
	                    	</ul>
                    	</c:when>
                    	<c:otherwise>
                    		<spring:message code="complete_overview.no_applications" />                    		
                    	</c:otherwise>
                    </c:choose>                                       
                </c:forEach>
            </div>
        </div>
    </div>
</div>
</div>
</body>
</html>
