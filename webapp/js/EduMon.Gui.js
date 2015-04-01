window.EduMon.Gui = new function Gui() {
	var that = this;

	var countFeedMessages = 0;
	var dialogOpened = 0;

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

	/**
	 * Refresh the "n Messages"-Counter in the newsfeed panel
	 */
	this.updateFeedCountView = function updateFeedCountView() {
		$("#feedcounter").html("<span class=\"badge\">"+countFeedMessages+"</span> Nachricht"+(countFeedMessages!==1?"en":""));
	};

	/**
	 * Performs the AJAX request for dialog content. [Deduplicating code in showDialog() and switchDialog()]
	 * @param {String} dialogid [see showDialog()]
	 */
	var loadDialog = function loadDialog(dialogid) {
		$("#dialogcontent").load("dialogs/"+dialogid+".html", function(response, status, xhr){
			if (status==="success"){
				$("#dialogcontainer").scrollTop(0);
				that.setDialogBlock(0);
			} else {
				that.closeDialog();
				throw "loadDialog() fehlgeschlagen";
			}
		});
	}

	/**
	 * Open the given dialog in a modal on top of the seating plan (only when no dialog is open yet)
	 * @param {String} dialogid Name of the dialog file in the dialog folder without .html extension
	 */
	this.showDialog = function showDialog(dialogid) {
		if (this.dialogOpened){
			throw "Cannot open another dialog. Use switchDialog() instead of openDialog()";
			return;
		}
		this.dialogOpened = 1;
		$("#dialogcontent").empty();
		this.setDialogBlock(1);
		$("#layercontainer").show();
		$("#dialogcontainer").fadeIn(200);
		loadDialog(dialogid);
	};

	/**
	 * Switch from the current dialog to another
	 * @param {String} dialogid [see showDialog()]
	 */
	this.switchDialog = function switchDialog(dialogid) {
		if (!this.dialogOpened){
			throw "No dialog currently open to switch from. Use openDialog() instead";
			return;
		}
		this.setDialogBlock(1);
		loadDialog(dialogid);
	};

	/**
	 * Display or hide the loading/busy indicator of the dialog
	 * @param {Boolean} blocked Set to 1 for display and 0 to hide the blocker
	 */
	this.setDialogBlock = function setDialogBlock(blocked) {
		if (blocked){
			$("#loadingbox").hide().delay(500).fadeIn(500);
			$("#loadinglayer").show();
		} else {
			$("#loadinglayer").hide();
			$("#loadingbox").stop();
		}
	};

	/**
	 * Close the active dialog
	 */
	this.closeDialog = function closeDialog() {
		$("#dialogcontainer").fadeOut(100,function(){
			$("#layercontainer").hide();
			$("#dialogcontent").empty();
			that.dialogOpened = 0;
		});
	};

	/**
	 * Display a toast notification that disappears after a little while
	 * @param {String} message Message to be displayed
	 */
	this.showToast = function showToast(message) {
		$("#toastlist")
			.prepend($("<li/>")
					.append($("<div/>").text(message))
					.one("click",function(e){
						$(this).remove();
					})
					.delay(2000)
					.fadeOut(1000,function(){
						$(this).remove();
					}));
	};
};
