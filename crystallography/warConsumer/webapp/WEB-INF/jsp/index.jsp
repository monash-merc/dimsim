<%--@ page session="true" --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
<title>CIMA Demo</title>
<link rel="stylesheet" href="styles/spring-demo.css" type="text/css">
</head>
<body>

<c:set var="model" value="${subscribeCommand.model}" />
<c:set var="actions" value="subscribe,unsubscribe" />

<script language="JavaScript">
	var JSON = ${model.JSON};
</script>

<form:form commandName="subscribeCommand" id="form">
    <form:hidden id="action" path="action" />
    
    Remote CIMA location: <form:input size="30" path="serverURL" />
    <input value="update" type="button" onclick="javascript:action.value='updateRemote'; form.submit(); return false;"/>
	<br/>
	<table style="text-align: left;" border="1" cellpadding="0"
		cellspacing="0">
		<tbody>
			<tr>
				<td style="text-align: center;">
					Local:
					<select name="localPlugin"> 
				        <c:forEach items="${model.localPlugins}" var="plugin" varStatus="status">
				            <option value="${plugin}">${plugin}</option>
				        </c:forEach>
				    </select>
			    </td>
				<td style="text-align: center;">
					Remote:
				    <select name="remotePlugin">
                        <c:forEach items="${model.remotePlugins}" var="plugin" varStatus="status">
                            <option value="${plugin}" <c:if test="${plugin == model.remotePlugin}">selected</c:if> >${plugin}</option>
                        </c:forEach>
				    </select>
				</td>
				<td style="text-align: center;">
				    <%--select name="actionSel">
				        <c:forEach items="${actions}" var="action" varStatus="status">
				            <option value="${action}">${action}</option>
				        </c:forEach>
				    </select>
				    <input value="submit" type="button" onclick="javascript:action.value='subscribe'; form.submit(); return false;"/--%>
				    <input value="subscribe" type="button" onclick="javascript:action.value='subscribe'; form.submit(); return false;"/>
				</td>
			</tr>
		</tbody>
	</table>
	<%--
	Actions:<br/>
	<table style="text-align: left;" border="1" cellpadding="0"
		cellspacing="0">
		<tbody>
			<tr>
				<td style="text-align: center;">Local Plugin</td>
				<td style="text-align: center;">Remote Plugin</td>
				<td style="text-align: center;">Action</td>
			</tr>
			<tr>
				<td style="text-align: center;">
					<select name="localPlugin"> 
				        <c:forEach items="${model.localPlugins}" var="plugin" varStatus="status">
				            <option value="${plugin}">${plugin}</option>
				        </c:forEach>
				    </select>
			    </td>
				<td style="text-align: center;">
				    <select name="remotePlugin">
						<option>ExampleProducer</option>
						<option>p2</option>
				    </select>
				</td>
				<td style="text-align: center;">
				    <select name="actionSel">
				        <c:forEach items="${actions}" var="action" varStatus="status">
				            <option value="${action}">${action}</option>
				        </c:forEach>
				    </select>
				    <input value="submit" type="button" onclick="javascript:action.value='subscribe'; form.submit(); return false;"/>
				</td>
			</tr>
		</tbody>
	</table--%>
	<br/>
	Subscriptions:<br/>
	<table style="text-align: left;" border="1" cellpadding="0"
		cellspacing="0">
		<tbody>
            <%--tr>
                <td>
                    <select name="subLocalPlugin"> 
                        <c:forEach items="${model.localPlugins}" var="plugin" varStatus="status">
                            <option value="${plugin}">${plugin}</option>
                        </c:forEach>
                    </select>
                </td>
                <td>
                    <select name="subRemotePlugin">
                        <c:forEach items="${model.localPlugins}" var="plugin" varStatus="status">
                            <c:forEach items="${model.subscriptions}" var="subscription" varStatus="subStatus">
                                    <c:if test="${subscription.plugin.id == plugin}">
                                        <option value="${subscription.responseInfo.sender.id}">${subscription.responseInfo.sender.id}</option>
                                </c:if>
                            </c:forEach>
                        </c:forEach>
                    </select>
                </td>
                <td><input value="unsubscribe" type="button" onclick="javascript:action.value='unsubscribe'; form.submit(); return false;"/></td>
            </tr--%>
			<tr>
				<td>
					<select name="subLocalPlugin"> 
						<c:forEach items="${model.localPlugins}" var="plugin" varStatus="status">
					        <option value="${plugin}">${plugin}</option>
						</c:forEach>
				    </select>
			    </td>
				<td>
					<select name="subRemotePlugin">
						<c:forEach items="${model.localPlugins}" var="plugin" varStatus="status">
					        <c:forEach items="${model.subscriptions}" var="subscription" varStatus="subStatus">
					        		<c:if test="${subscription.plugin.id == plugin}">
					            		<option value="${subscription.responseInfo.sender.id}">${subscription.responseInfo.sender.id}</option>
					            </c:if>
					        </c:forEach>
						</c:forEach>
				    </select>
			    </td>
				<td><input value="unsubscribe" type="button" onclick="javascript:action.value='unsubscribe'; form.submit(); return false;"/></td>
			</tr>
			<%--tr>
				<td><select name="localSubs">
					<option>p1</option>
					<option>p2</option>
				</select></td>
				<td><select name="remoteSubs">
					<option>p1</option>
					<option>p2</option>
				</select></td>
			</tr--%>
		</tbody>
	</table>
	<br>
<p>subscriptions:
<ol>
    <c:forEach items="${model.subscriptions}" var="subscription"
        varStatus="status">
        <li>plugin id: <c:out value="${subscription.responseInfo.sender.id}" /><br />
        session id: <c:out value="${subscription.responseInfo.newSessionId}" /><br />
        </li>
    </c:forEach>
</ol>
</p>
	<%--
	for (java.util.Enumeration names = request.getAttributeNames(); names.hasMoreElements(); ) {
		System.out.println("name: " + names.nextElement());
	}
	System.out.println(request.getAttribute("model").getClass().getName());

	java.util.Map model = (java.util.Map)request.getAttribute("model");
	java.util.Set values = model.entrySet();
	java.util.Iterator iterator = values.iterator();
	while (iterator.hasNext()) {
		java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
		System.out.println(entry.getKey() + ": " + entry.getValue());
	}
--%>
<%--
	<h1>CIMA Demo</h1>
	<a href="?">Do nothing</a>
	<br />
            local plugin: 
            action:
	<br />
			remote CIMA endpoint: <br />
			Plugins (comma separated):
	<br />
	<input type="submit" value="submit" />
--%>
</form:form>

<p>info:
<ul>
	<c:forEach items="${model.info}" var="info" varStatus="status">
		<li><c:out value="${info}" /><br />
		</li>
	</c:forEach>
</ul>


<p>failed subscriptions:
<ol>
	<c:forEach items="${model.failed}" var="subscription"
		varStatus="status">
		<li>plugin id: <c:out value="${subscription.responseInfo.sender.id}" /><br />
		message: <c:out value="${subscription.responseInfo.message}" /><br />
		</li>
	</c:forEach>
</ol>

<p>errors: <c:out value="${model.error}" />
<p>model: <c:out value="${model}" />
<p>subscription response: <c:out value="${model.subscription}" /><br />

<script language="javascript">
    var action = document.getElementById("action");
    var form = document.getElementById("form");
</script>
</body>
</html>

