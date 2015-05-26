// Randomly add a data point every 500ms
var interval = 1000;
var metrics = {};
var chart = new SmoothieChart({
  labels: { fillStyle:'rgb(100, 246, 255)', fontSize: 13, precision: 0},
  grid: { strokeStyle:'rgb(0, 255, 0)', fillStyle:'rgb(0, 0, 0)',
    lineWidth: 1, millisPerLine: interval, verticalSections: 4 },
  timestampFormatter: SmoothieChart.timeFormatter
});

chart.streamTo(document.getElementById("chart"), 1000);

var query = 
 'select metrics.* from metrics m ' +
 'inner join (' +
   'select host, facility, metric, max(timestamp) as timestamp from metrics group by host, facility, metric) a ' + 
   'on m.host=a.host and m.facility = a.facility and m.metric = a.metric and m.timestamp = a.timestamp;';

// Poll Phoenix for latest metric values
setInterval(function() {
  $.ajax({
    type: 'POST',
    url: 'services',
    data: query,
    success: function(data){
      var result = JSON.parse(data);
      $.each(result.result, function(index, row){
        host = row[0]; facility = row[1];
        metric = row[2]; val = row[4];
        name = host + '|' + facility + '|' + metric;

        if (metrics.hasOwnProperty(name))
          metrics[name].append(new Date().getTime(), val);
        else {
          //Haven't seen this metric before. Create a line on the chart
          metrics[name] = new TimeSeries();
          metrics[name].append(new Date().getTime(), val);

          var color = goldenColors.getHsvGolden(0.99, 0.99).toRgb();
          rgb = 'rgb('+color[0]+','+color[1]+','+color[2]+')';
          chart.addTimeSeries(metrics[name], {
            strokeStyle: rgb.replace('rgb', 'rgba').replace(')', ',1)'),
            fillStyle: rgb.replace('rgb', 'rgba').replace(')', ',0)'),
            lineWidth: 3 }
          );
          newHtml = '<tr>'+cell(host)+cell(facility)+cell(metric)+cell(square(rgb))+'</tr>';
          $('#legend tr:last').after(newHtml);
        }
      });
    }
  });
}, interval);

function square(rgb){ return '<div style="width:15px;height:15px;border:1px solid #000;background-color:'+rgb+'"> </div>'; }
function cell(val){ return '<td>'+val+'</td>'; }
