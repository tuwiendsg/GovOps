var temperatureData;

$('#temperatureChart').highcharts({
  chart : {
    type : 'line',
    events : {
      load : function() {
        temperatureData = this.series[0];
      }
    }
  },
  title : {
    text : false
  },
  xAxis : {
    type : 'datetime',
    minRange : 60 * 1000,
  },
  yAxis : {
    title : {
      text : false
    },
    max: 40
  },
  legend : {
    enabled : false
  },
  plotOptions : {
    series : {
      threshold : 0,
      marker : {
        enabled : false
      }
    }
  },
  series : [ {
    name : 'Data',
      data : [ ]
    } ]
});

var socket = new SockJS('/SDGSampleServer/temperature');
var client = Stomp.over(socket);

client.connect('user', 'password', function(frame) {

  client.subscribe("/data", function(message) {
    var point = [ (new Date()).getTime(), parseInt(message.body) ];
    var shift = temperatureData.data.length > 60;
    temperatureData.addPoint(point, true, shift);
  });

});