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
					.append($("<button/>",{type:"button",class:"close"}).text("x")
						.one("click",function(e){
							countFeedMessages--;
							that.updateFeedCountView();
							$(this).parent().hide();
						}))
					.append($("<h4/>").text(title))
					.append($("<p/>").html(message))
					);
	};

	this.updateFeedCountView = function updateFeedCountView() {
		$("#feedcounter").text(countFeedMessages+" Nachricht"+(countFeedMessages!==1?"en":""));
	}
};
