<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="month" type="java.lang.String" required="true" %>
<%@attribute name="hrefPrefix" type="java.lang.String" required="true" %>

<script type="text/javascript">

    $(function () {

        var currentYear = new Date().getFullYear();

        var $dropdown = $('#month-selection').find('.dropdown-menu');
        
        
        var months = [
        	<c:forEach begin="1" end="12" varStatus="loop">
            	'<spring:message code="month.${loop.index}" />',
        	</c:forEach>
        ];
        
        for (var i = 0; i < 12; i++) {
        	$dropdown.append('<li><a href="${hrefPrefix}' + (i + 1) + '">' + months[i] + '</a></li>');
        }
    });

</script>



<div id="month-selection" class="legend-dropdown dropdown">
    <a id="dropdownLabel" data-target="#" href="#" data-toggle="dropdown"
       aria-haspopup="true" role="button" aria-expanded="false">
      	<spring:message code="month.${month}" /><span class="caret"></span>
    </a>

    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownLabel"></ul>
</div>