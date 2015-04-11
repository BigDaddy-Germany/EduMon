EduMon.Gui = new function() {
	var that = this;

	var countFeedMessages = 0;
	var dialogOpened = 0;
	var popupOpened = 0;
	var popupCallback;
	var defaultButtons = {
		"yes":    {text:"Ja",       value:"yes",   class:"success"},
		"no":     {text:"Nein",     value:"no",    class:"danger" },
		"ok":     {text:"OK",       value:"ok",    class:"primary"},
		"cancel": {text:"Abbrechen",value:"cancel",class:"default"}
	};
	var seatsInfo = {width: -1, height: -1};

	/**
	 * Add message to newsfeed
	 * @param {String} type Message type can be "info", "success", "warning" and "danger"
	 * @param {String} title The message title
	 * @param {String} message The html message to display
	 */
	this.showFeedMessage = function(type, title, message) {
		countFeedMessages++;
		this.updateFeedCountView();
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
									$(e.target).remove();
								});
						}))
					.append($("<h4/>").text(title))
					.append($("<p/>").html(message))
					);
	};

	/**
	 * Refresh the "n Messages"-Counter in the newsfeed panel
	 */
	this.updateFeedCountView = function() {
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
	};

	/**
	 * Open the given dialog in a modal on top of the seating plan (only when no dialog is open yet)
	 * @param {String} dialogid Name of the dialog file in the dialog folder without .html extension
	 */
	this.showDialog = function(dialogid) {
		if (dialogOpened){
			throw "Cannot open another dialog. Use switchDialog() instead of openDialog()";
		}
		dialogOpened = 1;
		$("#dialogcontent").empty();
		this.setDialogBlock(true);
		$("#layercontainer").show();
		if (!popupOpened){
			$("#popupcontainer").hide();
		}
		$("#dialogcontainer").fadeIn(200);
		loadDialog(dialogid);
	};

	/**
	 * Switch from the current dialog to another
	 * @param {String} dialogid [see showDialog()]
	 */
	this.switchDialog = function(dialogid) {
		if (!dialogOpened){
			throw "No dialog currently open to switch from. Use openDialog() instead";
		}
		this.setDialogBlock(true);
		loadDialog(dialogid);
	};

	/**
	 * Display or hide the loading/busy indicator of the dialog
	 * @param {Boolean} blocked Set to 1 for display and 0 to hide the blocker
	 */
	this.setDialogBlock = function(blocked) {
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
	this.closeDialog = function() {
		$("#dialogcontainer").fadeOut(100,function(){
			if (!popupOpened){
				$("#layercontainer").hide();
			}
			$("#dialogcontent").empty();
			dialogOpened = 0;
		});
	};

	/**
	 * Display a toast notification that disappears after a little while
	 * @param {String} message Message to be displayed
	 */
	this.showToast = function(message) {
		$("#toastlist")
			.prepend($("<li/>")
					.append($("<div/>").text(message))
					.one("click",function(e){
						$(e.target).remove();
					})
					.delay(2000)
					.fadeOut(1000,function(){
						$(this).remove();
					}));
	};

	/**
	 * Display a popup box
	 * @param {String} title Popup title
	 * @param {String} message Message to be displayed
	 * @param {Array} buttons Collection of button objects to display in the popup: [{text:"Yes, please",value:"confirmdelete",class:"danger"},{...},...]
	 * @param {Function} callback Function to be called on button click, will be given button value as parameter
	 */
	this.showPopup = function(title, message, buttons, callback) {
		if (popupOpened){
			throw "Cannot open another popup. The current one has to be closed first";
		}
		popupOpened = 1;
		$("#layercontainer").show();
		$("#popuptitle").text(title);
		$("#popupmessage").text(message);
		var popupfooter = $("#popupfooter");
		popupfooter.empty();
		for (var i = 0; i < buttons.length; i++){
			if ((typeof (buttons[i])==="string") && (buttons[i] in defaultButtons)){
				buttons[i] = defaultButtons[buttons[i]];
			}
			popupfooter.append(
					$("<button/>",{type:"button"})
					.addClass("btn btn-"+buttons[i].class)
					.text(buttons[i].text)
					.data("returnvalue",buttons[i].value)
					);
		}
		popupCallback = callback || function(){};
		popupfooter.find("button").one("click",function(){
			that.closePopup();
			popupCallback($(this).data("returnvalue"));
		});
		$("#popupcontainer").show();
	};

	/**
	 * Closes the popup box
	 */
	this.closePopup = function() {
		$("#popupcontainer").hide();
		if (!dialogOpened){
			$("#layercontainer").hide();
		}
		popupOpened = 0;
	};

	/**
	 * Update a seat
	 * @param {int} row Row number from 1 (front desk) to n
	 * @param {int} number Seat number from 1 (at left from moderator view) to n
	 * @param {float} activity Seat activity between 0 (dead) and 1 (most active)
	 * @param {String} content Text to display on the seat (TO BE EXTENDED BY SIO [SEAT INFORMATION OBJECT])
	 */
	this.updateSeat = function(row, number, activity, content) {
		var seats = $("#seats").find("tbody");
        if (seatsInfo.height===-1) seatsInfo.height = seats.children().length;
		if (seatsInfo.width ===-1) seatsInfo.width  = seats.children().first().children().length;
		var seat = seats.children().eq(seatsInfo.height-row).children().eq(seatsInfo.width-number);

		seat.text(content);

		var colorCoding = {low:"#4CAF50",mid:"#FF5722",high:"#F44336"};
		var color =                 colorCoding.low;
		if (activity > 0.3) color = colorCoding.mid;
		if (activity > 0.6) color = colorCoding.high;
		
		var transStart = Math.max(  0, Math.round((activity - 0.01)*100));
		var transEnd   = Math.min(100, Math.round((activity + 0.01)*100));

		seat.css("background","linear-gradient(0deg, "+color+" 0%, "+color+" "+transStart+"%, #DDD "+transEnd+"%, #DDD 100%");
	}
};
