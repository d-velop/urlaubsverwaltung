<%-- 
    Document   : login
    Created on : 31.10.2011, 10:00:23
    Author     : Johannes Reuter + Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/login.css' />" /> 
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
        <script src="<spring:url value='/jquery/js/jquery-1.6.2.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/jquery/js/jquery-ui-1.8.16.custom.min.js' />" type="text/javascript" ></script>
        <script type="text/javascript">
                $(document).ready(function() {
                    
                    var url = document.URL;
            
                    if(url.indexOf("login_error") != -1) {
                        $('#login-error').show('drop', {direction: "up"}); 
                    } 
                });
            </script>
    </head>

    <body>

        <div id="top-menu">
            Login
        </div>

        <div id="header">

            <h1 style="top:50%;
                left:50%;">Urlaubsverwaltung</h1>

        </div>

        <div id="login-content">

                <div id="wrapper">
                    
                <form method="post" action="j_spring_security_check">
                    
                    <table id="login-tbl">
                        <tr>
                            <td style="text-align: left">
                                <label for="j_username">Username</label>
                            </td>
                            <td>
                                <input type="text" name="j_username" id="j_username" />
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align: left">
                                <label for="j_password">Passwort</label>
                            </td>
                            <td>
                                <input type="password" name="j_password" id="j_password" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                &nbsp;
                            </td>
                            <td style="padding-top: 1em;">
                               <input type='checkbox' name='_spring_security_remember_me' />Angemeldet bleiben
                            </td>
                        </tr>
                        <tr>
                            <td>
                                &nbsp;
                            </td>
                            <td style="padding-top: 1em;">
                               <input type="submit" value="Login" name="Login" style="float: right;" />
                            </td>
                        </tr>
                    </table>
                </form>
                    
                    <div id="login-error" style="display:none">
                Der eingegebene Nutzername oder das Passwort ist falsch.
            </div>
                    
            </div>

        </div>    
    </body>

</html>
