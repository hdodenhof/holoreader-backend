<html>
<body>
<p>Hallo <%= request.getAttribute("name") %> </p>
<p><a href="<%= request.getAttribute("logoutLink") %>">Logout</a></p>
<form action="/holoreader" method="POST" enctype="multipart/form-data" >
<input type="file" name="file">
<input type="submit" text="Send to device"/>
</form>
</body>
</html>