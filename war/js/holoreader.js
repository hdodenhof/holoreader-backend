$(document)
  .ready(
    function() {
      var inputSelector = ":input:not(:button):not(:checkbox)";
      var inputCount = $("#feeduploader " + inputSelector).length;
      var nextid = inputCount + 1;

      $("#feeduploader").on('blur', inputSelector, function(event) {
        var emptyFields = 0;
        $("#feeduploader " + inputSelector).each(function() {
          if (!$(this).val()) {
            emptyFields++;
          }
        });

        if (!$(this).val() && emptyFields > 1) {
          removeInput($(this));
        }

        validateForm();
      });

      $("#feeduploader").on('keyup', inputSelector, function(event) {
        var emptyField = false;
        $("#feeduploader " + inputSelector).each(function() {
          if (!$(this).val()) {
            emptyField = true;
            return false;
          }
        });

        if (!emptyField) {
          addInput();
        }

        validateForm();
      });

      $("#devices :checkbox").click(function() {
        validateForm();
      });

      $("#clear").click(function() {
        resetForm();
        return false;
      });

      $("#submit").click(function() {
        $(this).button('loading');
        $("#clear").prop('disabled', true);
        resetMessage(true);

        var data = {};
        data['devices'] = parseDevices();
        data['feeds'] = parseFeeds();

        $.ajax({
          type : "POST",
          url : "/",
          contentType : 'application/json',
          data : JSON.stringify(data),
          processData : false,
          dataType : "json",
          success : postSuccess,
          error : postError
        });
        return false;
      });

      function validateForm() {
        var checked = false;
        $("#devices :checkbox").each(function() {
          if ($(this).is(':checked')) {
            checked = true;
            return false;
          }
        });

        var input = false;
        $("#feeduploader " + inputSelector).each(function() {
          if ($(this).val()) {
            input = true;
            return false;
          }
        });

        if (!checked) {
          $("#submit").prop("disabled", true);
          $("#submit").prop("value", "Select at least one device");
          $("#submit").text("Select at least one device");
        } else if (!input) {
          resetButtons();
        } else {
          if ($("#submit").prop("disabled")) {
            $("#submit").prop("disabled", false);
            $("#submit").prop("value", "Send feeds to your device");
            $("#submit").text("Send feeds to your device");
          }
        }
      }

      function resetForm() {
        resetMessage();
        resetInputs();
        resetButtons();
      }

      function resetMessage(instant) {
        if ($("#error").css("opacity") == '1') {
          if (instant) {
            $("#error").css("opacity", "0");
          } else {
            $("#error").animate({
              opacity : 0.0
            }, 200);
          }
        }
      }

      function resetButtons() {
        $("#submit").prop("disabled", true);
        $("#submit").prop("value", "Enter at least one URL");
        $("#submit").text("Enter at least one URL");
      }

      function resetInputs() {
        $("#devices :checkbox").each(function() {
          $(this).prop("checked", true);
        });

        if (inputCount > 1) {
          removeInputs();
          addInput();
        }
      }

      function addInput() {
        $(
          '<div class="control-group" style="display:none;"><input type="text" placeholder="e.g. http://feeds2.feedburner.com/businessinsider or just businesinsider.com" class="span8" name="feeds[]" id="feed_'
            + nextid + '"/></div>').appendTo("#inputs").slideDown("fast");
        nextid++;
        inputCount++;
      }

      function removeInputs() {
        $("#feeduploader " + inputSelector).each(function() {
          removeInput($(this));
        });
      }

      function removeInput(object) {
        object.parent().slideUp("fast", function() {
          $(this).remove();
          inputCount--;
        });
      }

      function parseDevices() {
        var devices = new Array();

        $("#devices :checkbox").each(function() {
          if ($(this).is(':checked')) {
            var device = {};
            device['id'] = $(this).val();
            devices.push(device);
          }
        });

        return devices;
      }

      function parseFeeds() {
        var feeds = new Array();

        $("#feeduploader " + inputSelector).each(function() {
          var url = $(this).val();
          if (url != "" && $(this).prop('disabled') != true) {
            var feed = {};
            feed['url'] = url;
            feed['inputid'] = $(this).attr('id');
            feeds.push(feed);
          }
        });
        return feeds;
      }

      function postSuccess(data, status, jqXHR) {
        $.each(data, function(key, val) {
          if (val == true) {
            $("#" + key).prop('disabled', true);
            $("#" + key).parent().removeClass('error');
            $("#" + key).parent().addClass('success');
          } else {
            $("#" + key).parent().addClass('error');

            if ($("#error").css("opacity") == '0') {
              $("#error").animate({
                opacity : 1.0
              }, 200);
            }
          }
        });

        $("#submit").button('reset');
        $("#clear").prop('disabled', false);
      }

      function postError(jqXHR, textStatus, errorThrown) {
        alert("Something went terribly wrong.\n" + errorThrown);
        $("#submit").button('reset');
        $("#clear").prop('disabled', false);
      }
    });