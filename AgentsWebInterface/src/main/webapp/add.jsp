<%-- 
    Document   : add
    Created on : Apr 20, 2015, 10:32:44 PM
    Author     : martin
--%>

<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>Add agent</h2>
    <c:if test="${not empty error}">
        <div style="border: solid 1px red; background-color: yellow; padding: 10px">
            <c:out value="${error}"/>
        </div>
    </c:if>
    <form action="${pageContext.request.contextPath}/agents/add" method="post">
        <table>
            <tr>
                <th>Agent name:</th>
                <td><input type="text" name="name" value="<c:out value='${param.name}'/>"/></td>
            </tr>
            <tr>
                <th>Born:</th>
                <td><input type="text" name="born" value="<c:out value='${param.born}'/>"/></td>
            </tr>
            <tr>
                <th>Level:</th>
                <td><input type="text" name="level" value="<c:out value='${param.level}'/>"/></td>
            </tr>
            <tr>
                <th>Note:</th>
                <td><input type="text" name="note" value="<c:out value='${param.note}'/>"/></td>
            </tr>
        </table>
        <input type="Submit" value="Add" />
    </form>
</body>
</html>
