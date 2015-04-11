window.EduMon.Timeline = new function Timeline() {
	var that = this;

	var tick_interval = 5; //in seconds
	var tick_value = 60; //in seconds, set != tick_interval for non-realtime testing
	var timer;
	
	var timeline; //comfort

	this.play = function play() {
		if (EduMon.Prefs.currentLecture.timeline.status!=="play"){
			EduMon.Prefs.currentLecture.timeline.slices.push({seconds:0,type:"lecture"});
			EduMon.Prefs.currentLecture.timeline.status = "play";
		} else throw "Timeline-Play nicht erlaubt, Timer läuft bereits";
	};

	this.pause = function pause() {
		if (EduMon.Prefs.currentLecture.timeline.status==="play"){
			EduMon.Prefs.currentLecture.timeline.slices.push({seconds:0,type:"break"});
			EduMon.Prefs.currentLecture.timeline.status = "pause";
		} else throw "Timeline-Unterbrechung nicht erlaubt, Timer lief nicht";
	};

	this.stop = function stop() {
		clearInterval(timer);
		EduMon.Prefs.currentLecture.timeline.status = "stop";
		//TODO is irreversible - shall it be? (logic: you can only finish a lecture once)
	};
	
	var tick = function tick() {
		if (EduMon.Prefs.currentLecture.timeline.status!=="stop"){
			var currentSlice = timeline.slices[timeline.slices.length-1];
			currentSlice.seconds += tick_value;
			timeline.totalSeconds += tick_value;
			updateTimeline();
		}
	};

	this.init = function init(){
		//Timer is always active, but timer tick does not always trigger action
		//this prevents seconds getting lost
		timer = setInterval(tick, tick_interval*1000);
		timeline = EduMon.Prefs.currentLecture.timeline;
	};

	var updateTimeline = function updateTimeline(){
		var totalPercentage = 0; //remember how full the bar is
		var barPercentage;
		var barActiveClass = "";
		var isLastBar = false;

		for (var i=0; i<timeline.slices.length; i++){ //refresh all slices
			isLastBar = (i==timeline.slices.length-1);

			//calculate percentage and add it up
			barPercentage = Math.floor(timeline.slices[i].seconds/timeline.totalSeconds*100);
			totalPercentage += barPercentage;
			if (isLastBar)
			{
				//fill the space if we are at the last element and below 100 because of rounding
				barPercentage += 100-totalPercentage;
			}

			//create new slice if necessary
			if($("#progressdisplay").children().length<=i){
				var barType = (timeline.slices[i].type==="lecture"?"success":"warning");
				$("#progressdisplay").append($("<div/>").addClass("progress-bar progress-bar-"+barType));
			}

			//update slice
			$("#progressdisplay").children().eq(i)
				.width(barPercentage+"%")
				.text(Math.floor(timeline.slices[i].seconds/60)+"min")
				.toggleClass("active",isLastBar)
				;
		}
	};

};
