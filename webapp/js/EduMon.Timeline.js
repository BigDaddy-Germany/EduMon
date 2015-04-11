EduMon.Timeline = new function() {
	var that = this;

	var tick_interval = 0.5; //in seconds
	var tick_value = 60; //in seconds, set != tick_interval for non-realtime testing
	var timer;
	
	var timeline; //comfort

	var getTime = function(){
		var now = new Date();
		return now.getHours()+":"+("0"+now.getMinutes()).slice(-2);
	}

	var setPreviousEnd = function(){
		if (timeline.slices.length>1){
			timeline.slices[timeline.slices.length-2].end = getTime();
		}
	}

	this.play = function() {
		if (timeline.status!=="play"){
			timeline.slices.push({seconds:0,type:"lecture"});
			setPreviousEnd();
			timeline.status = "play";
			timeline.start = getTime();
		} else throw "Timeline-Play nicht erlaubt, Timer läuft bereits";
		updateTimeline();
	};

	this.pause = function() {
		if (timeline.status==="play"){
			timeline.slices.push({seconds:0,type:"break"});
			setPreviousEnd();
			timeline.status = "pause";
		} else throw "Timeline-Unterbrechung nicht erlaubt, Timer lief nicht";
		updateTimeline();
	};

	this.stop = function() {
		clearInterval(timer);
		timeline.status = "stop";
		updateTimeline();
		//TODO is irreversible - shall it be? (logic: you can only finish a lecture once)
	};
	
	var tick = function(onlyUpdate) {
		if (onlyUpdate!==true && EduMon.Prefs.currentLecture.timeline.status!=="stop"){
			var currentSlice = timeline.slices[timeline.slices.length-1];
			currentSlice.seconds += tick_value;
			timeline.totalSeconds += tick_value;
		}
		updateTimeline();
	};

	this.init = function(){
		//Timer is always active, but timer tick does not always trigger action
		//this prevents seconds getting lost
		timeline = EduMon.Prefs.currentLecture.timeline;
		timer = setInterval(tick, tick_interval*1000);
		updateTimeline();

		$("#flowbox").find(".glyphicon-play").click(function(){that.play();});
		$("#flowbox").find(".glyphicon-pause").click(function(){that.pause();});
		$("#flowbox").find(".glyphicon-stop").click(function(){that.stop();});
	};

	var updateTimeline = function(){
		var totalPercentage = 0; //remember how full the bar is
		var barPercentage;
		var barActiveClass = "";
		var isLastBar = false;
		var isFirstBar = false;

		for (var i=0; i<timeline.slices.length; i++){ //refresh all slices
			isLastBar = (i==timeline.slices.length-1);
			isFirstBar = (i==0);

			//create new slice if necessary
			var progressDisplay = $("#progressdisplay");
			var hourDisplay     = $("#hourdisplay");
			if(progressDisplay.children().length<=i){
				//insert new slice in both timeline and hour display
				var barType = (timeline.slices[i].type==="lecture"?"success":"warning");
				progressDisplay.append($("<div/>").addClass("progress-bar progress-bar-"+barType));
				hourDisplay.append($("<span/>").addClass("hour"));

				if (isFirstBar){ //insert lecture start time
					hourDisplay.children().eq(0).append(
							$("<span/>")
							.addClass("start")
							.text(timeline.start)
							);
				}
				if (i>0){ //when opening a new slice, insert end time of the previous one
					hourDisplay.children().eq(i-1).append(
							$("<span/>").addClass("end").text(timeline.slices[i-1].end)
							);
				}
			}

			//calculate percentage and add it up
			barPercentage = Math.floor(timeline.slices[i].seconds/timeline.totalSeconds*100);
			totalPercentage += barPercentage;
			if (isLastBar)
			{
				//fill the space if we are at the last element and below 100 because of rounding
				barPercentage += 100-totalPercentage;
			}

			//update slice widths
			progressDisplay.children().eq(i).width(barPercentage+"%").toggleClass("active",isLastBar);
			hourDisplay.children().eq(i).animate({"width":barPercentage+"%"},450);

			//time only accumulates in the latest slice
			if (i==timeline.slices.length-1){ 
				var minutes = Math.floor(timeline.slices[i].seconds/60);
				if (minutes>0){
					progressDisplay.children().eq(i).text(minutes+"min");
				}
			}
		}

		//update clock and state display
		$("#currenttime").text(getTime());
		$("#currentstate i")
			.removeClass("glyphicon-play glyphicon-pause glyphicon-stop")
			.addClass("glyphicon-"+timeline.status);

		//display fitting control buttons
		$("#flowbox").find(".glyphicon-play").toggle(timeline.status!=="play");
		$("#flowbox").find(".glyphicon-pause").toggle(timeline.status==="play");
		$("#flowbox").find(".glyphicon-stop").toggle(timeline.status!=="stop");
	};

	//TODO create "updateControls" and implement their functionaltiy

};
