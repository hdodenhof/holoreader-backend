<html>
<body>
<p>Hallo <%= request.getAttribute("name") %> </p>
<p><a href="<%= request.getAttribute("logoutLink") %>">Logout</a></p>
<%
if (((Boolean) request.getAttribute("devicePresent")) != false){
%>
<form action="/holoreader" method="POST" enctype="multipart/form-data" >
<input type="file" name="file">
<input type="submit" text="Send to device"/>
</form>
<%
} else {
%>
<p>No device registered! Register in the App and click <a href="/holoreader">here</a> to refresh.</p>
<%
}
%>
</body>
</html>