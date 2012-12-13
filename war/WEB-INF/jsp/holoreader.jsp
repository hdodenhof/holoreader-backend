<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List" %>
<%@page import="de.hdodenhof.holoreader.backend.persistence.entities.DeviceEntity" %>
<!DOCTYPE html>
<html>
  <head>
    <link href="/css/bootstrap.css" rel="stylesheet" media="screen">
    <style>
      body {
        padding-top: 60px;
      }
    </style>
  </head>
  <body>
    <div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
            <span class="brand" style="color: #CC0000;">Holo Reader</span>
            <%
	          if (((Boolean) request.getAttribute("loggedIn")) == true){
	    	%>
            <ul class="nav pull-right">
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                  Logged in as <%= request.getAttribute("name") %>
                  <b class="caret"></b>
                </a>
			    <ul class="dropdown-menu">
			      <li><a href="<%= request.getAttribute("logoutLink") %>">Logout</a></li>
			    </ul>
			  </li>
            </ul>
            <%
	          }
	    	%>
        </div>
      </div>
    </div>
    <div class="container">
    <div class="row">
      <div class="span8 offset2">
        <%
	        if (((Boolean) request.getAttribute("loggedIn")) == true){
	    %>
	    <%
	        if (((List) request.getAttribute("devices")).size() > 0){
	    %>
	    <div id="result">
	    <div class="alert alert-success" id="success" style="display:none">
  		  <h4>The following feeds have been added:</h4>
		  <ul id="successlist"></ul>
		</div>
		<div class="alert alert-error" id="failure" style="display:none">
  		  <h4>The following feeds could not be added:</h4>		
		  <ul id="failurelist"></ul>
		</div>
		</div>
		<form id="feeduploader">
		<fieldset>
         <legend>Send feeds to your device</legend>
         <div id="devices" class="span8" style="margin-bottom: 20px">
		 <%
	        for (DeviceEntity device : (List<DeviceEntity>) request.getAttribute("devices")) {
	     %>
	    	<div><label class="checkbox"><input type="checkbox" checked="checked" name="devices[]" value="<%= device.getWebsaveKey() %>"> <%= device.getDevice() %></label></div>
	     <%
	        }
	     %>
		 </div>
         <div id="inputs" class="span8" style="margin-bottom: 20px">
         <div><input type="text" placeholder="http://www.google.com/news/feed.xml" class="span8" name="feeds[]" id="feed_0"/></div>
         </div>
         <div style="text-align:right;"><button id="submit" type="submit" data-loading-text="Sending feeds..." class="btn btn-danger">Send feeds to your device</button></div>
		</fieldset>
		</form>
	    <%
	        } else {
	    %>
	    <div class="alert alert-error">
  		  <b>No device registered!</b><br /><br />
  		  You need to register your device for push messaging before using this feature. To do so open Holo Reader on your Android device, click the menu button, select "Enable FeedToDevice" and follow the on screen instructions.<br /><br />
  		  If you don't have Holo Reader installed yet, <a href="https://play.google.com/store/apps/details?id=de.hdodenhof.holoreader">click here</a> to get to Google Play.<br /><br />  
  		  When you are done click <a href="/">here</a> to refresh this page.
	    </div>
	    <%
	        }
	    %>
	    <%
	        } else {
	    %>
	    <div class="alert alert-error">
  		  You need to login with your Google account first, click <a href="<%= request.getAttribute("loginLink") %>">here</a>.
	    </div>	    
	    <%
	        }
	    %>
	  </div>
	</div>
    </div>
    <script src="http://code.jquery.com/jquery-latest.js"></script>
    <script src="/js/bootstrap.js"></script>
        <script>
		$(document).ready(function(){
		    var i = $("#feeduploader :input:not(:button)").length;
		    var nextid = i + 1;
		    
		    $("#feeduploader").on('blur', ':input:not(:button):not(:checkbox)', function(event) {
		    	var emptyFields = 0;
		 		$("#feeduploader :input:not(:button)").each(function(){
		 			if (!$(this).val()){
		 			  emptyFields++;
		 			}
		 		});
		 		
		 	    if (!$(this).val() && emptyFields>1){
		 	    	$(this).parent().slideUp("fast", function() { $(this).remove(); } );
		 	    	i--;
		 	    }
		 	
		 		/* var id = this.id.replace("feed_", ""); */
		 		/* validate input, if valid URL, send async to server for feed validation */
			});
		    
			$("#feeduploader").on('keyup', ':input:not(:button):not(:checkbox)', function(event) {
		 		var emptyField = false;
		 		$("#feeduploader :input:not(:button):not(:checkbox)").each(function(){
		 			if (!$(this).val()){
		 			  emptyField = true;
		 			  return false;
		 			}
		 		});
		 		
		 		if (!emptyField){
		 		    $('<div style="display:none;"><input type="text" placeholder="http://www.google.com/news/feed.xml" class="span8" name="feeds[]" id="feed_'+nextid+'"/></div>').appendTo("#inputs").slideDown("fast");
		 		    nextid++;
		 		    i++;
		 		}
			});
			
			$("#devices :checkbox").click(function(){
			    var checked = false;
			    $("#devices :checkbox").each(function(){
			        if($(this).is(':checked')){
			            checked = true;
			            return false;
			        }
			    });
			    
			    if (!checked){
			        $("#submit").attr("disabled", "disabled");
			        $("#submit").prop("value", "Select at least one device");
			        $("#submit").text("Select at least one device");
			    } else {
			    	$("#submit").removeAttr("disabled");
			    	$("#submit").prop("value", "Send feeds to your device");
			    	$("#submit").text("Send feeds to your device"); 
			    }
			    
			});
			
			$("#submit").click(function() {
			   var btn = $(this);
        	   btn.button('loading');
        	   
        	   $("#result").slideUp("fast", function() {
				$("#success").hide();
				$("#failure").hide();

        	   $("#successlist").empty();
        	   $("#failurelist").empty();
        	   
			   var submitdata = $("#feeduploader").serialize();
				$.ajax({  
				  type: "POST",  
				  url: "/",  
				  data: submitdata,
				  dataType: "json",
				  success: function(data, status, jqXHR) {  
					var success=false;
					var failure=false;
					$.each(data, function(key, val) {
						if (val == true){
							$("#successlist").append("<li>"+key+"</li>");
							success=true;
						} else {
							$("#failurelist").append("<li>"+key+"</li>");
							failure=true;
						}
						
						$("#feeduploader :input:not(:button):not(:checkbox)").each(function(){
							$(this).parent().remove();
				 		});
				 		
			 		    $('<div style="display:none;"><input type="text" placeholder="http://www.google.com/news/feed.xml" class="span8" name="feeds[]" id="feed_'+nextid+'"/></div>').appendTo("#inputs").slideDown("fast");
			 		    nextid++;
			 		    i=1;				 		
						
						if (success){
							$("#success").show();
						}
						if (failure){
							$("#failure").show();
						}
						$("#result").slideDown("fast");
					});

				    btn.button('reset');
				  },
				 error: function(jqXHR, textStatus, errorThrown){
				 	alert("Something went terribly wrong.\n"+errorThrown);
				 }
				}); } );
				return false;
			});
		});    
    </script>
  </body>
</html>