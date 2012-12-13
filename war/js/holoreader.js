$(document)
		.ready(
				function() {
					var i = $("#feeduploader :input:not(:button)").length;
					var nextid = i + 1;

					$("#feeduploader").on(
							'blur',
							':input:not(:button):not(:checkbox)',
							function(event) {
								var emptyFields = 0;
								$("#feeduploader :input:not(:button)").each(
										function() {
											if (!$(this).val()) {
												emptyFields++;
											}
										});

								if (!$(this).val() && emptyFields > 1) {
									$(this).parent().slideUp("fast",
											function() {
												$(this).remove();
											});
									i--;
								}
							});

					$("#feeduploader")
							.on(
									'keyup',
									':input:not(:button):not(:checkbox)',
									function(event) {
										var emptyField = false;
										$(
												"#feeduploader :input:not(:button):not(:checkbox)")
												.each(function() {
													if (!$(this).val()) {
														emptyField = true;
														return false;
													}
												});

										if (!emptyField) {
											$(
													'<div style="display:none;"><input type="text" placeholder="http://www.google.com/news/feed.xml" class="span8" name="feeds[]" id="feed_'
															+ nextid
															+ '"/></div>')
													.appendTo("#inputs")
													.slideDown("fast");
											nextid++;
											i++;
										}
									});

					$("#devices :checkbox").click(
							function() {
								var checked = false;
								$("#devices :checkbox").each(function() {
									if ($(this).is(':checked')) {
										checked = true;
										return false;
									}
								});

								if (!checked) {
									$("#submit").attr("disabled", "disabled");
									$("#submit").prop("value",
											"Select at least one device");
									$("#submit").text(
											"Select at least one device");
								} else {
									$("#submit").removeAttr("disabled");
									$("#submit").prop("value",
											"Send feeds to your device");
									$("#submit").text(
											"Send feeds to your device");
								}

							});

					$("#submit")
							.click(
									function() {
										var btn = $(this);
										btn.button('loading');

										$("#result")
												.slideUp(
														"fast",
														function() {
															$("#success")
																	.hide();
															$("#failure")
																	.hide();

															$("#successlist")
																	.empty();
															$("#failurelist")
																	.empty();

															var submitdata = $(
																	"#feeduploader")
																	.serialize();
															$
																	.ajax({
																		type : "POST",
																		url : "/",
																		data : submitdata,
																		dataType : "json",
																		success : function(
																				data,
																				status,
																				jqXHR) {
																			var success = false;
																			var failure = false;
																			$
																					.each(
																							data,
																							function(
																									key,
																									val) {
																								if (val == true) {
																									$(
																											"#successlist")
																											.append(
																													"<li>"
																															+ key
																															+ "</li>");
																									success = true;
																								} else {
																									$(
																											"#failurelist")
																											.append(
																													"<li>"
																															+ key
																															+ "</li>");
																									failure = true;
																								}

																								$(
																										"#feeduploader :input:not(:button):not(:checkbox)")
																										.each(
																												function() {
																													$(
																															this)
																															.parent()
																															.remove();
																												});

																								$(
																										'<div style="display:none;"><input type="text" placeholder="http://www.google.com/news/feed.xml" class="span8" name="feeds[]" id="feed_'
																												+ nextid
																												+ '"/></div>')
																										.appendTo(
																												"#inputs")
																										.slideDown(
																												"fast");
																								nextid++;
																								i = 1;

																								if (success) {
																									$(
																											"#success")
																											.show();
																								}
																								if (failure) {
																									$(
																											"#failure")
																											.show();
																								}
																								$(
																										"#result")
																										.slideDown(
																												"fast");
																							});

																			btn
																					.button('reset');
																		},
																		error : function(
																				jqXHR,
																				textStatus,
																				errorThrown) {
																			alert("Something went terribly wrong.\n"
																					+ errorThrown);
																		}
																	});
														});
										return false;
									});
				});