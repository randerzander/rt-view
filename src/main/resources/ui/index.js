$('#submit').on('click', function(){
  cleanup(); //Clear existing populated results
  var $btn = $(this).button('loading'); //Set 'Submit' button loading state

  $.ajax({ //Submit the query
    type: 'POST',
    url: 'services',
    data: $('#txtquery').val(),
    success: function(data){
      var result = JSON.parse(data);
      var csv = "data:text/csv;charset=utf-8,";
      //Append headers
      csv += result.columns.join(',') + '\n';
      $.each(result.columns, function(i, v){
        $('#rows>thead>tr').append('<th>' + v + '</th>');
      });
      //Append rows
      $.each(result.result, function(i, v){
        //Append cells
        csv += v.join(',') + '\n';
        var row = '<tr>';
        $.each(v, function(index, val){
          row += '<td>' + val + '</td>';
        });
        $('#rows>tbody').append(row + '</tr>');
      });
      $btn.button('reset'); //clear 'Submit' button loading state

      //Create CSV link
      var link = document.createElement('a');
      link.setAttribute('href', encodeURI(csv));
      link.setAttribute('download', 'result.csv');
      link.innerHTML='Save CSV';
      $('#csvlink').removeClass('hidden').append(link);

      //Update collapsible panes
      $('#collapseOne').removeClass('in').addClass('collapsing');
      $('#collapseTwo').addClass('in');
    }
  });
});

function cleanup(){ //Clear old results
  $('#rows>thead>tr>th').remove();
  $('#rows>tbody>tr').remove();
  $('#csvlink>a').remove();
}
