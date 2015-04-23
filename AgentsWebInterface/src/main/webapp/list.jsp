<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>

<table border="1">
    <thead>
    <tr>
        <th>Name</th>
        <th>Born</th>
        <th>Level</th>
        <th>Note</th>
    </tr>
    </thead>
    <c:forEach items="${agents}" var="agent">
        <tr>
            <td><c:out value="${agent.name}"/></td>
            <td><c:out value="${agent.born}"/></td>
            <td><c:out value="${agent.level}"/></td>
            <td><c:out value="${agent.note}"/></td>
            <td><form method="post" action="${pageContext.request.contextPath}/agents/delete?id=${agent.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Delete"></form></td>
            <td><form method="post" action="${pageContext.request.contextPath}/agents/update?id=${agent.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Edit"></form></td>
        </tr>
    </c:forEach>
</table>
    
    <br />
   <form method="post" action="${pageContext.request.contextPath}/agents/addRedirect"
                      style="margin-bottom: 0;"><input type="submit" value="Add new agent"></form>



</body>
</html>