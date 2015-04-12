EduMon = window.EduMon || {};
EduMon.Wheel = function(canvas, segments, onFinish) {

    if (segments.length == 0) {
        throw "No segments supplied!";
    }

    var context = canvas.getContext('2d');

    var PI = Math.PI;
    var HALF_PI = PI / 2;
    var TAU = 2 * PI;


    var weights = EduMon.Math.sumOver(0, segments.length, function(i) {return segments[i][1]});
    var unit = TAU / weights;

    var timerHandle = -1;
    var timerDelay = 33;

    var currentAngle = 0;
    var angleDelta = 0;

    var size = 290;

    var colors = ['#000000', '#ffff00', '#ffc700', '#ff9100', '#ff6301', '#ff0000', '#c6037e', '#713697', '#444ea1', '#2772b2', '#0297ba', '#008e5b', '#8ac819'];

    var segmentColors = [];

    var maxSpeed = PI / 16;

    var upTime = 1000;
    var downTime = 17000;

    var spinStart = 0;

    var centerX = 300;
    var centerY = 300;

    var activeSegment = null;

    initializeData();

    function initializeData() {
        // Ensure we start mid way on a item
        currentAngle = TAU - 0.5 * unit * segments[0][1];

        var len = segments.length;
        var colorLen = colors.length;

        // Generate a color cache (so we have consistent coloring)
        var newSegmentColors = [];
        for (var i = 0; i < len; i++) {
            var color = colors[modulo(hashString(segments[i][0]), colorLen)];
            newSegmentColors.push([color, invertColor(color)]);
        }
        segmentColors = newSegmentColors;

        draw();
    }

    function invertColor(color) {
        var red   = (parseInt(color.substring(1, 3), 16));
        var green = (parseInt(color.substring(3, 5), 16));
        var blue  = (parseInt(color.substring(5), 16));

        var hsv = EduMon.Math.rgbToHsv(red, green, blue);
        var rgb = EduMon.Math.hsvToRgb((hsv[0] + 180) % 360, hsv[1], hsv[1] < .5 ? 1 : 0);

        red   = EduMon.Util.padLeft(rgb[0].toString(16), '0', 2);
        green = EduMon.Util.padLeft(rgb[1].toString(16), '0', 2);
        blue  = EduMon.Util.padLeft(rgb[2].toString(16), '0', 2);

        return "#" + red + green + blue;

    }

    function modulo(a, n) {
        return ((a % n) + n) % n;
    }

    /**
     * Calculates the hash code
     *
     * @param {String} a the input string
     * @returns {number} the string hash code
     */
    function hashString(a) {
        // See http://www.cse.yorku.ca/~oz/hash.html
        var hash = 5381;
        for (i = 0; i < a.length; i++) {
            char = a.charCodeAt(i);
            hash = ((hash << 5) + hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash;
    }

    this.start = function() {

        // Start the wheel only if it's not already spinning
        if (timerHandle == -1) {
            spinStart = new Date().getTime();
            maxSpeed = PI / (16 + Math.random()); // Randomly vary how hard the spin is

            timerHandle = setInterval(onTimerTick, timerDelay);
        }
    };

    function onTimerTick() {
        draw();

        var duration = (new Date().getTime() - spinStart);
        var progress = 0;
        var finished = false;

        if (duration < upTime) {
            progress = duration / upTime;
            angleDelta = maxSpeed * Math.sin(progress * HALF_PI);
        } else {
            progress = duration / downTime;
            angleDelta = maxSpeed * Math.sin(progress * HALF_PI + HALF_PI);
            if (progress >= 1) {
                finished = true;
            }
        }

        currentAngle += angleDelta;
        while (currentAngle >= TAU)
            // Keep the angle in a reasonable range
            currentAngle -= TAU;

        if (finished) {
            clearInterval(timerHandle);
            timerHandle = -1;
            angleDelta = 0;
            if (typeof onFinish == 'function') {
                onFinish(activeSegment);
            }
        }
    }

    function draw() {
        clear();
        drawWheel();
    }

    function clear() {
        context.clearRect(0, 0, 1000, 800);
    }

    function drawActive(text) {
        // Now draw the winning name
        context.textAlign = "left";
        context.textBaseline = "middle";
        context.fillStyle = '#000000';
        context.font = "2em Arial";
        context.fillText(text, centerX + size + 25, centerY);
    }

    function drawSegment(segment, color, startAngle) {
        var text = segment[0];
        var weight = segment[1];

        var endAngle = startAngle + weight * unit;

        context.save();
        context.beginPath();

        // Start in the centre
        context.moveTo(centerX, centerY);
        context.arc(centerX, centerY, size, startAngle, endAngle, false); // Draw a arc around the edge
        context.lineTo(centerX, centerY); // Now draw a line back to the centre

        // Clip anything that follows to this area
        //ctx.clip(); // It would be best to clip, but we can double performance without it
        context.closePath();

        var background = color[0];
        var foreground = color[1];

        context.fillStyle = background;
        context.fill();
        context.stroke();

        // Now draw the text
        context.save(); // The save ensures this works on Android devices
        context.translate(centerX, centerY);
        context.rotate((startAngle + endAngle) / 2);

        context.fillStyle = foreground;
        context.fillText(text.substr(0, 20), size / 2 + 20, 0);
        context.restore();

        if ((startAngle % TAU) >= (endAngle % TAU)) {
            drawActive(text, startAngle, endAngle);
            activeSegment = segment;
        }

        context.restore();



        return endAngle;
    }

    function drawWheel() {

        context.lineWidth = 1;
        context.strokeStyle = '#000000';
        context.textBaseline = "middle";
        context.textAlign = "center";
        context.font = "1.4em Arial";

        var lastAngle = currentAngle;
        var len = segments.length;

        for (var i = 0; i < len; i++) {
            lastAngle = drawSegment(segments[i], segmentColors[i], lastAngle);
        }

        // Draw a center circle
        context.beginPath();
        context.arc(centerX, centerY, 20, 0, TAU, false);
        context.closePath();

        context.fillStyle = '#ffffff';
        context.strokeStyle = '#000000';
        context.fill();
        context.stroke();

        // Draw outer circle
        context.beginPath();
        context.arc(centerX, centerY, size, 0, TAU, false);
        context.closePath();

        context.lineWidth = 10;
        context.strokeStyle = '#000000';
        context.stroke();

        // draw the needle
        context.lineWidth = 1;
        context.strokeStyle = '#000000';
        context.fillStyle = '#ffffff';

        context.beginPath();

        context.moveTo(centerX + size - 40, centerY);
        context.lineTo(centerX + size + 20, centerY - 10);
        context.lineTo(centerX + size + 20, centerY + 10);
        context.closePath();

        context.stroke();
        context.fill();
    }
};