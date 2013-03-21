<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<title>Holo Reader FeedPusher (beta)</title>
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
        <c:choose>
          <c:when test="${loggedIn == true}">
            <ul class="nav pull-right">
              <li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"> Logged in as
                  ${name} <b class="caret"></b>
              </a>
                <ul class="dropdown-menu">
                  <li><a href="${logoutLink}">Logout</a></li>
                </ul></li>
            </ul>
          </c:when>
          <c:otherwise>
            <ul class="nav pull-right">
              <li><a href="${loginLink}"> Login </a></li>
            </ul>
          </c:otherwise>
        </c:choose>
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
                    <div id="inputs" class="span8" style="margin-bottom: 10px">
                      <div class="control-group">
                        <input type="text" placeholder="e.g. http://rss.cnn.com/rss/edition.rss or just cnn.com"
                          class="span8" name="feeds[]" id="feed_0" />
                      </div>
                    </div>
                    <div id="error" style="cursor: default; opacity: 0.0; text-align: right;"
                      class="span8 control-group error">
                      <div class="control-label">It seems some URLs don't point to a valid feed - these were not
                        added!</div>
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
                  "Enable FeedPusher" and follow the on screen instructions.<br /> <br /> If you don't have Holo
                  Reader installed yet, just go to <a
                    href="https://play.google.com/store/apps/details?id=de.hdodenhof.holoreader">Google Play</a>.<br />
                  <br /> When you're done, you have to <a href="/">refresh</a> this page.
                </div>
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            <div class="alert alert-info">
              <b>Welcome to Holo Reader FeedPusher (beta)!</b><br /> <br /> After logging in with your Google account
              you can easily add feeds to Holo Reader on your device(s) from any web browser. To login, just click the
              link in the top right corner.<br /> <br />Please keep in mind that this service is still in its testing
              stage. I appreciate any feedback via <a href="mailto:holoreader@hdodenhof.de?subject=FeedPusher">holoreader@hdodenhof.de</a>.<br />
              <br />Thanks!<br /> <br /> <small>P.S. There is a quota limit for using this service as long as
                it's in its testing stage. The quota is reset every 24 hours, so if you can't reach this page, please
                try again later.</small>
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