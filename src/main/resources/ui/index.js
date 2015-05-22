// Randomly add a data point every 500ms
var interval = 1000;
var metrics = {};
var chart = new SmoothieChart({
  grid: { strokeStyle:'rgb(0, 255, 0)', fillStyle:'rgb(0, 0, 0)',
  lineWidth: 1, millisPerLine: interval, verticalSections: 4},
  labels: { fillStyle:'rgb(60, 0, 0)' }
});

chart.streamTo(document.getElementById("chart"), 500);

var query = 
 'select metrics.* from metrics m ' +
 'inner join (' +
   'select host, facility, metric, max(timestamp) as timestamp from metrics group by host, facility, metric) a ' + 
   'on m.host=a.host and m.facility = a.facility and m.metric = a.metric and m.timestamp = a.timestamp;';

setInterval(function() {
  $.ajax({
    type: 'POST',
    url: 'services',
    data: query,
    success: function(data){
      var result = JSON.parse(data);
      $.each(result.result, function(index, row){
        host = row[0];
        facility = row[1];
        metric = row[2];
        val = row[4];

        name = host + '|' + facility + '|' + metric;

        if (metrics.hasOwnProperty(name))
          metrics[name].append(new Date().getTime(), val);
        else {
          metrics[name] = new TimeSeries();
          metrics[name].append(new Date().getTime(), val);

          series = Object.keys(metrics).length;
          r = 255-series*25 % 255;
          g = 255-series*15 % 255;
          b = 255-series * 3 % 255;
          rgb = 'rgb('+r+','+g+','+b+')';
debugger;
          chart.addTimeSeries(metrics[name], {
            strokeStyle: rgb,
            fillStyle: rgb.replace('rgb', 'rgba').replace(')', ',.1)'),
            lineWidth: 1 }
          );
          newHtml = '<tr>'+cell(host,rgb)+cell(facility,rgb)+cell(metric,rgb)+'</tr>';
debugger;
          $('#legend tr:last').after(newHtml);
        }
      });
    }
  });
}, interval);

function cell(val, rgb){ return '<td bgcolor="'+rgb+'">'+val+'</td>'; }
