EduMon.Timeline = new function() {
	var that = this;

	var tick_interval = 5; //in seconds
	var tick_value = 60; //in seconds, set != tick_interval for non-realtime testing
	var timer;
	
	var timeline; //comfort

	var getTime = function getTime(){
		return EduMon.Prefs.currentLecture.timeline.totalSeconds; //TODO format time
	}

	var setPreviousEnd = function setPreviousEnd(){
		if (timeline.slices.length>1){
			timeline.slices[timeline.slice.length-2].end = getTime();
		}
	}

	this.play = function play() {
		if (EduMon.Prefs.currentLecture.timeline.status!=="play"){
			EduMon.Prefs.currentLecture.timeline.slices.push({seconds:0,type:"lecture"});
			setPreviousEnd();
			EduMon.Prefs.currentLecture.timeline.status = "play";
			EduMon.Prefs.currentLecture.timeline.start = getTime();
		} else throw "Timeline-Play nicht erlaubt, Timer läuft bereits";
	};

	this.pause = function() {
		if (EduMon.Prefs.currentLecture.timeline.status==="play"){
			EduMon.Prefs.currentLecture.timeline.slices.push({seconds:0,type:"break"});
			setPreviousEnd();
			EduMon.Prefs.currentLecture.timeline.status = "pause";
		} else throw "Timeline-Unterbrechung nicht erlaubt, Timer lief nicht";
	};

	this.stop = function() {
		clearInterval(timer);
		EduMon.Prefs.currentLecture.timeline.status = "stop";
		//TODO is irreversible - shall it be? (logic: you can only finish a lecture once)
	};
	
	var tick = function() {
		if (EduMon.Prefs.currentLecture.timeline.status!=="stop"){
			var currentSlice = timeline.slices[timeline.slices.length-1];
			currentSlice.seconds += tick_value;
			timeline.totalSeconds += tick_value;
			updateTimeline();
		}
	};

	this.init = function(){
		//Timer is always active, but timer tick does not always trigger action
		//this prevents seconds getting lost
		timer = setInterval(tick, tick_interval*1000);
		timeline = EduMon.Prefs.currentLecture.timeline;
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

			//calculate percentage and add it up
			barPercentage = Math.floor(timeline.slices[i].seconds/timeline.totalSeconds*100);
			totalPercentage += barPercentage;
			if (isLastBar)
			{
				//fill the space if we are at the last element and below 100 because of rounding
				barPercentage += 100-totalPercentage;
			}

			//create new slice if necessary
			var progressDisplay = $("#progressdisplay");
            if(progressDisplay.children().length<=i){
				var barType = (timeline.slices[i].type==="lecture"?"success":"warning");
				$("#progressdisplay").append($("<div/>").addClass("progress-bar progress-bar-"+barType));
				$("#hourdisplay").append($("<span/>").addClass("hour"));
				if (isFirstBar){
					$("#hourdisplay").children().eq(0).append(
							$("<span/>")
							.addClass("start")
							.text(timeline.start)
							);
				}
			}

			//update slice
			progressDisplay.children().eq(i)
				.width(barPercentage+"%")
				.text(Math.floor(timeline.slices[i].seconds/60)+"min")
				.toggleClass("active",isLastBar)
				;
			$("#hourdisplay").children().eq(i)
				.width(barPercentage+"%")
				.text(timeline.slices[i].end)
				;
		}

		//TODO: update clock
	};

	//TODO create "updateControls" and implement their functionaltiy

};
