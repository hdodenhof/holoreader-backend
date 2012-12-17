<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<title>Holo Reader FeedToDevice</title>
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
        <c:if test="${loggedIn == true}">
          <ul class="nav pull-right">
            <li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"> Logged in as
                ${name} <b class="caret"></b>
            </a>
              <ul class="dropdown-menu">
                <li><a href="${logoutLink}">Logout</a></li>
              </ul></li>
          </ul>
        </c:if>
      </div>
    </div>
  </div>
  <div class="container">
    <div class="row">
      <div class="span8 offset2">
        <c:choose>
          <c:when test="${loggedIn == true}">
            <c:choose>
              <c:when test="${not empty devices}">
                <form id="feeduploader">
                  <fieldset>
                    <legend>Send feeds to your device</legend>
                    <div id="devices" class="span8" style="margin-bottom: 20px">
                      <c:forEach var="device" items="${devices}">
                        <div>
                          <label class="checkbox"><input type="checkbox" checked="checked" name="devices[]"
                            value="${device.key}">${device.value}</label>
                        </div>
                      </c:forEach>
                    </div>
                    <div id="inputs" class="span8" style="margin-bottom: 20px">
                      <div class="control-group">
                        <input type="text" placeholder="http://www.google.com/news/feed.xml" class="span8"
                          name="feeds[]" id="feed_0" />
                      </div>
                    </div>
                    <div style="text-align: right;">
                      <button id="clear" type="reset" class="btn btn-danger">Clear</button>
                      <button id="submit" type="submit" disabled data-loading-text="Sending feeds..."
                        class="btn btn-primary">Enter at least one URL</button>
                    </div>
                  </fieldset>
                </form>
              </c:when>
              <c:otherwise>
                <div class="alert alert-error">
                  <b>No device registered!</b><br /> <br /> You need to register your device for push messaging before
                  using this feature. To do so open Holo Reader on your Android device, click the menu button, select
                  "Enable FeedToDevice" and follow the on screen instructions.<br /> <br /> If you don't have Holo
                  Reader installed yet, <a href="https://play.google.com/store/apps/details?id=de.hdodenhof.holoreader">click
                    here</a> to get to Google Play.<br /> <br /> When you are done click <a href="/">here</a> to refresh
                  this page.
                </div>
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            <div class="alert alert-error">
              You need to login with your Google account first, click <a href="${loginLink}">here</a>.
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
  <script src="/js/bootstrap.js"></script>
  <script src="/js/holoreader.js"></script>
</body>
</html>