window.EduMon.Gui = new function Gui() {
	var that = this;

	var countFeedMessages = 0;

	/**
	 * Add message to newsfeed
	 * @param {String} type Message type can be "info", "success", "warning" and "danger"
	 * @param {String} title The message title
	 * @param {String} message The html message to display
	 */
	this.showFeedMessage = function showFeedMessage(type, title, message) {
		countFeedMessages++;
		that.updateFeedCountView();
		$("#alertcontainer")
			.prepend($("<div/>",{class:"alert alert-dismissable alert-"+type})
					.css("opacity","0")
					.slideDown(300,function(){
						$(this).animate({opacity:1},200);
					})
					.append($("<button/>",{type:"button",class:"close"}).text("x")
						.one("click",function(e){
							countFeedMessages--;
							that.updateFeedCountView();
							$(this).parent()
							.css("visibility","hidden")
							.slideUp(200,function(){
								$(this).remove();
							});
						}))
					.append($("<h4/>").text(title))
					.append($("<p/>").html(message))
					);
	};

	this.updateFeedCountView = function updateFeedCountView() {
		$("#feedcounter").html("<span class=\"badge\">"+countFeedMessages+"</span> Nachricht"+(countFeedMessages!==1?"en":""));
	};

	this.showDialog = function showDialog(dialogid) {
		$("#dialogcontent").empty();
		$("#dialogcontent").load("dialogs/"+dialogid+".html", function(){
			$("#dialogcontainer").fadeIn(200);
			$("#dialogcontainer").scrollTop(0);
		});
	};

	this.closeDialog = function closeDialog(dialogid) {
		$("#dialogcontainer").fadeOut(200,function(){
			$("#dialogcontent").empty();
		});
	};
};
