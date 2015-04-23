<%-- 
    Document   : edit
    Created on : Apr 20, 2015, 1:06:55 PM
    Author     : martin
--%>

<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Edit Agent</title>
    </head>
    <body>
        <h1>Edit Agent: <c:out value="${agent.name}"/>, id:  <c:out value="${agent.id}"/></h1>
        <c:if test="${not empty error}">
            <div style="border: solid 1px red; background-color: yellow; padding: 10px">
                <c:out value="${error}"/>
            </div>
        </c:if>
        <form action="${pageContext.request.contextPath}/agents/edit?id=${agent.id}" method="post">
            <table>
                <tr>
                    <th>Agent name:</th>
                    <td><input type="text" name="name" value="<c:out value='${agent.name}'/>" /></td>
                </tr>
                <tr>
                    <th>Born:</th>
                    <td><input type="text" name="born" value="<c:out value='${agent.born}'/>"/></td>
                </tr>
                <tr>
                    <th>Level:</th>
                    <td><input type="text" name="level" value="<c:out value='${agent.level}'/>"/></td>
                </tr>
                <tr>
                    <th>Note:</th>
                    <td><input type="text" name="note" value="<c:out value='${agent.note}'/>"/></td>
                </tr>
            </table>
            <br />
            <input type="submit" value="Edit" >
        </form>

                <br />
                <form method="get" action="${pageContext.request.contextPath}/agents">
                    <input type="submit" value="Cancel" />
                </form>


    </body>
</html>
