EduMon.Gui = new function() {
	var that = this;

	var seatUpdateInterval = EduMon.debugging ? 50 : 1000; //milliseconds
	var seatUpdateTimer = -1;

	var countFeedMessages = 0;
	var dialogOpened = 0;
	var openedDialog = undefined;
	var popupOpened = 0;
	var popupCallback;
	var defaultButtons = {
		"yes":    {text:"Ja",       value:"yes",   class:"success"},
		"no":     {text:"Nein",     value:"no",    class:"danger" },
		"ok":     {text:"OK",       value:"ok",    class:"primary"},
		"cancel": {text:"Abbrechen",value:"cancel",class:"default"}
	};
	var seatsInfo = {width: -1, height: -1};
	var msgNoLecture = "Es ist zur Zeit keine Vorlesung aktiv!";

	this.hasFocus = true;

	/**
	 * Add message to newsfeed
	 * @method showFeedMessage
	 * @param {String} type Message type can be "info", "success", "warning" and "danger"
	 * @param {String} title The html message title
	 * @param {String} message The html message to display
	 * @return undefined
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
					.append($("<h4/>").html(title))
					.append($("<p/>").html(message))
					);
	};

	/**
	 * Refresh the "n Messages"-Counter in the newsfeed panel
	 * @method updateFeedCountView
	 * @return undefined
	 */
	this.updateFeedCountView = function() {
		$("#feedcounter").html("<span class=\"badge\">"+countFeedMessages+"</span> Nachricht"+(countFeedMessages!==1?"en":""));
	};

	/**
	 * Performs the AJAX request for dialog content. [Deduplicating code in showDialog() and switchDialog()]
	 * @method loadDialog
	 * @param {String} dialogid [see showDialog()]
	 * @return Promise
	 */
	var loadDialog = function(dialogid) {
		return new Promise(function(fulfill, reject) {
			$("#dialogcontent").load("dialogs/"+dialogid+".html", function(response, status, xhr){
				openedDialog = dialogid;
				if (status==="success"){
					$("#dialogcontainer").scrollTop(0);
					that.setDialogBlock(0);
					fulfill();
				} else {
					that.closeDialog();
					reject();
					throw "loadDialog() fehlgeschlagen";
				}
			});
		});
	};

	/**
	 * Open the given dialog in a modal on top of the seating plan (only when no dialog is open yet)
	 * @method showDialog
	 * @param {String} dialogid Name of the dialog file in the dialog folder without .html extension
	 * @param {Boolean} [attentionAbort] Flash for attention if dialog cannot be opened
	 * @return Promise
	 */
	this.showDialog = function(dialogid, attentionAbort) {
		var that = this;
		return new Promise(function(fulfill, reject) {
			if (dialogOpened){
				if (attentionAbort){
					EduMon.Gui.attention();
				}
				reject();
				throw "Cannot open another dialog. Use switchDialog() instead of openDialog()";
			}
			dialogOpened = 1;
			$("#dialogcontent").empty();
			that.setDialogBlock(true);
			$("#layercontainer").show();
			if (!popupOpened){
				$("#popupcontainer").hide();
			}
			$("#dialogcontainer").fadeIn(200);
			loadDialog(dialogid)
				.then(fulfill)
				.catch(reject);
		});
	};

	/**
	 * Switch from the current dialog to another
	 * @method switchDialog
	 * @param {String} dialogid [see showDialog()]
	 * @return Promise
	 */
	this.switchDialog = function(dialogid) {
		var that = this;
		return new Promise(function(fulfill, reject) {
			if (!dialogOpened){
				reject();
				throw "No dialog currently open to switch from. Use openDialog() instead";
			}
			that.setDialogBlock(true);
			loadDialog(dialogid)
				.then(fulfill)
				.catch(reject);
		});
	};

	/**
	 * Display or hide the loading/busy indicator of the dialog
	 * @method setDialogBlock
	 * @param {Boolean} blocked Set to 1 for display and 0 to hide the blocker
	 * @return undefined
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
	 * @method closeDialog
	 * @return undefined
	 */
	this.closeDialog = function() {
		$("#dialogcontainer").fadeOut(100,function(){
			if (!popupOpened){
				$("#layercontainer").hide();
			}
			$("#dialogcontent").empty();
			dialogOpened = 0;
			openedDialog = undefined;
		});
	};

	/**
	 * Returns the dialog's ID which is opened currently
	 * @method getOpenedDialog
	 * @return openedDialog
	 */
	this.getOpenedDialog = function() {
		return openedDialog;
	};

	/**
	 * Display a toast notification that disappears after a little while
	 * @method showToast
	 * @param {String} message Message to be displayed
	 * @return undefined
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
	 * @method showPopup
	 * @param {String} title Popup title
	 * @param {String} message (HTML-)Message to be displayed
	 * @param {Array} buttons Collection of button objects to display in the popup: [{text:"Yes, please",value:"confirmdelete",class:"danger"},{...},...]
	 * @param {Function} [callback=function() {}] Function to call after popup has been answered, value of chosen button is passed as first parameter
	 * @param {Boolean} [attentionAbort=false] Flash for attention if dialog cannot be opened
	 * @return undefined
	 */
	this.showPopup = function(title, message, buttons, callback, attentionAbort) {
		if (popupOpened && attentionAbort!==true){
			throw "Cannot open another popup. The current one has to be closed first";
		} else if (popupOpened && attentionAbort===true){
			this.attention();
			return;
		}
		popupOpened = 1;
		$("#layercontainer").show();
		$("#popuptitle").text(title);
		$("#popupmessage").html(message);
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
	 * @method closePopup
	 * @return undefined
	 */
	this.closePopup = function() {
		$("#popupcontainer").hide();
		if (!dialogOpened){
			$("#layercontainer").hide();
		}
		popupOpened = 0;
	};

	/**
	 * Reload seating plan: rewrite table according to current room and start seat update timer
	 * @method initSeating
	 * @return undefined
	 */
	this.initSeating = function(){
		if (seatUpdateTimer >= 0){
			clearInterval(seatUpdateTimer);
		}

		var room = EduMon.Prefs.currentLecture.room;
		$("#seats")
			.hide()
			.html(new Array(room.height+1).join('<div class="srow">'+new Array(room.width+1).join(
						'<div class="scell"><div class="person"><div class="name"></div><div class="group"></div></div></div>'
						)+'</div>'))
			.fadeIn();
		$("#seatscontainer .bglogo").fadeOut();

		seatUpdateTimer = setInterval(function(){
			updateStudents();
		},seatUpdateInterval);

		if (EduMon.debugging){
			$("#packageLogging").change(function() {
				if ($("#devbox").hasClass("opened")){
					$('#packageLogging').scrollTop($('#packageLogging')[0].scrollHeight);
				}
			});

		}
	};

	/**
	 * Get the jQuery-DOM-element of a seat
	 * @method getSeatElement
	 * @param {int} number Seat number from 1 (at left from moderator view) to n (X index)
	 * @param {int} row Row number from 1 (front desk) to n (Y index)
	 * @return {jQuery-Element}
	 */
	var getSeatElement = function(number, row){
		var seats = $("#seats");
        if (seatsInfo.height===-1) seatsInfo.height = seats.children().length;
		if (seatsInfo.width ===-1) seatsInfo.width  = seats.children().first().children().length;
		return seats.children().eq(seatsInfo.height-row).children().eq(number-1);
	};

	/**
	 * Update a single seat by refreshing its text content and background level
	 * @method updateSeat
	 * @param {int} number Seat number from 1 (at left from moderator view) to n (X index)
	 * @param {int} row Row number from 1 (front desk) to n (Y index)
	 * @param {String} name
	 * @param {String} group
	 * @param {float} activity Seat activity between 0 (dead) and 1 (most active)
	 * @return undefined
	 */
	var updateSeat = function(number, row, name, group, activity) {
		var seat = getSeatElement(number, row);

		var colorCoding = {low:"#4CAF50",mid:"#FF5722",high:"#F44336"};
		var color =                 colorCoding.low;
		if (activity > 0.3) color = colorCoding.mid;
		if (activity > 0.6) color = colorCoding.high;
		
		var transStart = Math.max(  0, Math.round((activity - 0.01)*100));
		var transEnd   = Math.min(100, Math.round((activity + 0.01)*100));

		var gradient = "linear-gradient(0deg, "+color+" 0%, "+color+" "+transStart+"%, #DDD "+transEnd+"%, #DDD 100%)";
		seat.css("background",gradient);

		seat.find(".person .name").text(name);
		seat.find(".person .group").text(group);
	}

	/**
	 * Removes a seat from the seating plan (GUI and data base)
	 * @param {int} x the seat's x value (seat number)
	 * @param {int} y the seat's y value (row)
	 */
	this.deleteSeat = function(x, y) {
		var currentLecture = EduMon.Prefs.currentLecture;
		if (currentLecture.seatingPlan[x]) {
			delete currentLecture.seatingPlan[x][y];
		}
		var seat = getSeatElement(x, y);
		seat.css("background","#DDD");
		seat.find("div.name").empty();
		seat.find("div.group").empty();
	};

	/**
	 * Updates all seats with the active students data of the current lecture
	 * @method updateStudents
	 * @return undefined
	 */
	var updateStudents = function(){
		EduMon.Util.forEachField(EduMon.Prefs.currentLecture.activeStudents,function(studentId,student){
			updateSeat(student.seat.x, student.seat.y, student.name, student.group,
				EduMon.Analytics.scaleDisturbanceToPercentage(student.disturbance)
				);
		});
	};


	/**
	 * Flash a red border around the seating plan and layer container, mostly to indicate that interaction with an open dialog or popup is necessary
	 * @method attention
	 * @return undefined
	 */
	this.attention = function(){
		$("#attention").stop(true,true).fadeIn(100).delay(200).fadeOut(200).delay(300).fadeIn(100).delay(200).fadeOut(200);
	};


	/**
	 * Starts (and optionally resets) the action timer in the pult-up display
	 * @method togglePultup
	 * @param {Boolean} [state] Show (true) or hide (false) pult-up display, if not given the state is toggled
	 * @return undefined
	 */
	this.togglePultup = function(state){
		$("#pultup").toggleClass("inactive",(state===undefined ? state : !state));
	};


	/**
	 * Present pult-up and initialize it
	 * @method openPultUpMode
	 * @param {string} mode Pult up mode to show
	 * @param {function} [firstUpdate] Update function, is called once once pult-up was pulled down
	 * @param {function} [restoreActionTimer=false] If activated, the action timer will be restored from the saved value in currentLecture
	 * @return undefined
	 */
	this.openPultUpMode = function(mode, firstUpdate, restoreActionTimer){
		$("#pultup").addClass("inactive");
		setTimeout(function(){
			$("#pultup").removeClass("wheel thumb rating").addClass(mode).removeClass("inactive");
			if (firstUpdate) {
				firstUpdate();
			}
			EduMon.Feedback.restartActionTimer(!restoreActionTimer);
		},800);
		EduMon.Prefs.currentLecture.gui.pultup = mode;
	};


	/**
	 * Process hotkey
	 * @method processKey
	 * @param {event} e jQuery Element that fired on key press
	 * @return undefined
	 */
	var processKey = function(e){
		//exclude events that bubbled up from actual input elements
		if ($(e.target).is("input,button,textarea,a")){
			return;
		}
		var KEY = EduMon.Util.keymap;
		//now decide how to act: base idea is "act like the user would do" - button not there? cannot click! button disabled? cannot click
		switch(e.which){
			case KEY.D: //D-aumenfeedback
				if (!$("#btnThumbs").hasClass("disabled")){
					$("#btnThumbs").trigger("click");
				}
				break;
			case KEY.B: //B-ewertung
				if (!$("#btnRating").hasClass("disabled")){
					$("#btnRating").trigger("click");
				}
				break;
			case KEY.G: //G-lücksrad
			case KEY.R: //R-ad
			case KEY.Z: //Z-ufall
				$("#btnWheel").trigger("click");
				break;
			case KEY.P: //P-lay & P-ause
				if ($("#btnPause").is(":visible")){
					$("#btnPause").trigger("click");
				} else if ($("#btnPlay").is(":visible")){
					$("#btnPlay").trigger("click");
				}
				break;
			case KEY.SHIFT:
				$("#pultup .handle").trigger("click");
				break;
			default:
				EduMon.debug("Pressed: "+e.which);
		}
	};


	/**
	 * React to a change of focus
	 * @method processFocus
	 * @param {Boolean} focus Has the document the focus? (i.e. will hotkeys work?)
	 * @return undefined
	 */
	var processFocus = function(focus){
		that.hasFocus = focus;
		$("body").toggleClass("unfocussed",!focus);
	};

	/**
	 * Initialize GUI (bind click handlers)
	 * @method init
	 * @return undefined
	 */
	this.init = function(){
		//Buttons
		$("#btnSettings").off("click").click(function(){
			EduMon.Gui.showDialog("connectionSettings",true);
		});
		$("#btnThumbs").off("click").click(function(){
			if (EduMon.lectureIsActive()) {
				EduMon.Feedback.requestFeedback("thumb");
			} else {
				EduMon.Gui.showToast(msgNoLecture);
			}
		});
		$("#btnRating").off("click").click(function(){
			if (EduMon.lectureIsActive()) {
				EduMon.Feedback.requestFeedback("rating");
			} else {
				EduMon.Gui.showToast(msgNoLecture);
			}
		});
		$("#pultup").find(".handle").off("click").click(function(){
			if (EduMon.lectureIsActive() && EduMon.Prefs.currentLecture.gui.pultup!=="") {
				EduMon.Gui.togglePultup();
			} else {
				if (!EduMon.lectureIsActive()){
					EduMon.Gui.showToast(msgNoLecture);
				}
				EduMon.Gui.showFeedMessage("info","Pult-Up-Display&trade;","Dieses Infomenü ist nur während einer Aktion verfügbar. Es öffnet sich automatisch.");
			}
		});
		$('#btnRemoveData').off('click').on('click', function() {
			EduMon.Gui.showDialog('deleteData', true);
		});
		$('.btnHelp').off('click').on('click', function() {
			EduMon.Gui.showDialog('help', true);
		});

		//Hotkeys
		$("body").off().on("keydown",function(e){processKey(e)});

		//Window focus
		$(window)
			.focus(function(){processFocus(true);})
			.blur( function(){processFocus(false);});
	};
};
