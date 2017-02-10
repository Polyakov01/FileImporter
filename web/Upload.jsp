<%@ page language="java" contentType="text/html; charset=windows-1251"
 pageEncoding="windows-1251"%>
<%@ page isELIgnored ="false" %>
<html>
<head>
    <meta http-equiv="Content-Type"
          content="text/html; charset=UTF-8"/>
    <title>File Upload Practice</title>
    <link rel="stylesheet" href="assets/bootstrap.min.css">
    <script src="assets/jquery-1.8.3.js"></script>
    <script src="assets/bootstrap.file-input.js"></script>
   <style>
         table {
    width: 100%; /* Ширина таблицы */
    background: white; /* Цвет фона таблицы */
    color: black; /* Цвет текста */
    border-spacing: 1px; /* Расстояние между ячейками */
   }
   td, th {
    background: white; /* Цвет фона ячеек */
    padding: 5px; /* Поля вокруг текста */
    font-size: 16px;
   }
       html, body{
           width: 100%;
           height: 100%;
           overflow-y: hidden;
       } 
       
       body{
            background-color: #F7F7F9;
            font-size: 12px
        }
        .progress-bar {
            height: 20px;
            width: 100px;
            display: none;
            border: 2px solid green;
        }
        .progress {
            background-color: blue;
            height: 100%;
            width: 0px;
        }

        .file-input-name {
            font-size: 16px;
            font-weight: bold;
        }
        .b-main-container{
            min-height: 130px;
            width: 60%;
            margin: auto;
            text-align: center;
            background-color: #E4E4E4;
            border-radius: 3px;
            margin-top: 20px;
            border: solid 2px #ccc;
            
            height: 90%;
            overflow-y: auto;
        }

        .main-container-header{
            background-color: #3E3E3E;
            color: #ffffff;
            margin-bottom: 10px;
        }

        #target-frame{
            display: none;
        }

        #progress-loader{
            display: none;
        }

        #log-container{
            display: none;
            border: solid 1px #ccc;
            text-align: left;
            background-color: #ffffff;
            padding: 10px;
            overflow-y: auto;
            width: 100%;
        }
        
                #image-list {
            list-style:none;
            margin:0;
            padding:0;
        }
        #image-list li {
           
            border: 0px solid #ccc;
            padding: 5px 5px;
        }

        #image-list li p{
            font-size: 12px;
            display: inline-block;
            margin: 0;
        }
        #image-list li img {
            width: 15px;
            height: 15px;
            float: right;
        }
        
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            var progress,
                    spFound = 0,
                    logPos = 0,
                    timerId;
            //$('input[type=file]').bootstrapFileInput();
            
                    var input = $('#upload-input')[0];
                    $('#progress-status').html('Шаг 2');

        function showFileInList(file,id){
            var list = document.getElementById("image-list");
            var li   = document.createElement("li");
            li.innerHTML = '<p>'+file.name+'</p><img src="img/loading.gif" id="'+id+'">';
            list.appendChild(li);
        }

        $('#upload-files').on('click', function () 
        {
            //$(this).attr('disabled', 'disabled');
            if(input.files.length > 0){
                for (var i = 0 ; i < input.files.length; i++ ) {
                    var file = input.files[i];
                    var fileId = 'file-'+i;
                    var formData = new FormData();
                    showFileInList(file, fileId);

                    if (formData) 
                    {
                    //  timerId = doarchive();
                    }
                
                
                   if (formData) 
                    {
                        formData.append("images", file);
                        formData.append("file_name", file.name);
                        formData.append("file_id", fileId);
                    }

                    if (formData) 
                    {
                        $.ajax({
//                            url: "uploads.php",
                            url: "http://195.82.7.166:1080/FileImporterTest/fileUploaderRoutes?file_name="+file.name+"&file_id="+fileId,
                            type: "POST",
                            data: formData,
                            processData: false,
                            contentType: false,
                            success: function (res) {
                                var response = JSON.parse(res);
                                console.log(response.id);
                                console.log(response);
                                var img = document.getElementById(response.id);
                                if(response.status == "true"){
                                    img.src = "img/done.png";
                                }else{
                                    img.src = "img/error.png";
                                }
                            }
                        });
                    }
                }
            }else{
                alert("Выберите файлы для загрузки!");
            }
        });

            $('#startUploadBtn').click(function(event){
                $('#log-container').empty();
                startUpload();
            });
            
            $('#stopUploadBtn').click(function(event){
                console.log("CLICK");
                stopUpload();
            });
            
            $('#archiveUploadBtn').click(function(event){
                doarchive();
            });
            
            function doarchive () 
            {                
                clearTimeout(timerId);
                time = new Date().getTime();
                console.log("FUNC");
                $.ajax({
                        type: "GET",
                        url: "fileUploaderRoutes",
                        data: {time: time,operation:"doArchive"},
                        success: function (data, textStatus,jqXHR )
                        {

                        }
                    });                             
            }
           
            
            
            function stopUpload () 
            {                
                time = new Date().getTime();
                console.log("FUNC");
                $.ajax({
                        type: "GET",
                        url: "fileUploaderRoutes",
                        data: {time: time,operation:"stop"},
                        success: function (data, textStatus,jqXHR )
                        {

                        }
                    });                             
            }

            function startUpload () 
            {
                // avoid concurrent processing
                if (progress) return;
               
              // var uploadform = document.getElementById('uploadform'),
                        time = new Date().getTime();
              //  var reg_select = document.getElementById('reg_select');
                /*
                uploadform.action = 'fileUploader?beging_load=1&time=' + time+'&logpos='+logPos;
                uploadform.target = 'target-frame';
                uploadform.submit();
                */
                $.ajax({
                        type: "GET",
                        url: "fileUploaderRoutes",
                        data: {time: time,operation:"register"},
                        success: function (data, textStatus,jqXHR )
                        {

                        }
                    });
                
                 startProgressbar(time);
            }

            function startProgressbar (startTime) 
            {
                // display progress bar
                $('#tFileList').html('');
                $('#progress-loader').show();
                $('#log-container').show();
                $('#progress-status').html('Загрузка файла');
                // start timer
                progress = setInterval(function () {
                    // ask progress
                    $.ajax({
                        type: "GET",
                        url: "fileUploaderRoutes",
                        data: {time: startTime,operation:"log_request"},
                        error: function (request, error) {console.log(arguments); $('#log-container').append("Error: " + error); clearInterval(progress);},
                        success: function (data, textStatus,jqXHR )
                        {
                            var d = JSON.parse(data);
                            var  uploadprogress = d.progress[startTime];
                            console.log(d);
                            console.log(d[startTime]);
                            console.log(startTime);
                            console.log((d["progress"][startTime]["status"]).toString().substr(0,6));
                            for (i=0;i< d["fileLog"].length;i++)
                            {
                               // $('#log-container').append(d["fileLog"][i]['file_name']).append("<br>");
                                var pos = d["fileLog"][i]['filePos'];
                                if ($('#tr'+pos).length>0)
                                {                                
                                    $('#tdFile'+pos).html(d["fileLog"][i]['fileName']);
                                    $('#tdRgion'+pos).html(d["fileLog"][i]['fileRegion']);
                                    $('#tdFileSum'+pos).html(d["fileLog"][i]['fileSum']);
                                    $('#tdFileFD'+pos).html(d["fileLog"][i]['fileFd']);
                                    $('#tdFileLD'+pos).html(d["fileLog"][i]['fileLd']);
                                    $('#tdFileProg'+pos).html(d["fileLog"][i]['fileProg']);
                                    $('#tdFileStatus'+pos).html(d["fileLog"][i]['fileStatus']);
                                    //$('#fileRegion'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileRegion']);
                                    //$('#fileSum'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileSum']);
                                    //$('#fileFd'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileFd']);
                                    //$('#fileLd'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileLd']);
                                    //$('#fileProg'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileProg']);
                                    //$('#fileStatus'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileStatus']);
                               }
                               else
                               {
                                    $('#tFileList').append('<tr id = "tr'+pos+'">\n\
                                                            <td id = "tdFile'+pos+'">'+d["fileLog"][i]['fileName']+'</td>\n\
                                                            <td id = "tdRgion'+pos+'">'+d["fileLog"][i]['fileRegion']+'</td>\n\
                                                            <td id = "tdFileSum'+pos+'">'+d["fileLog"][i]['fileSum']+'</td>\n\
                                                            <td id = "tdFileFD'+pos+'">'+d["fileLog"][i]['fileFd']+'</td>\n\
                                                            <td id = "tdFileLD'+pos+'">'+d["fileLog"][i]['fileLd']+'</td>\n\
                                                            <td id = "tdFileProg'+pos+'">'+d["fileLog"][i]['fileProg']+'</td>\n\
                                                            <td id = "tdFileStatus'+pos+'">'+d["fileLog"][i]['fileStatus']+'</td></tr>');
                                    //$('#file-list').append('<li class="list-group-item" id = "filePosId'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileName']+"</li>");
                                    //$('#file-region').append('<li class="list-group-item" id = "fileRegion'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileRegion']+"</li>");
                                    //$('#file-sum').append('<li class="list-group-item" id = "fileSum'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileSum']+"</li>");
                                    //$('#file-fd').append('<li class="list-group-item" id = "fileFd'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileFd']+"</li>");
                                    //$('#file-ld').append('<li class="list-group-item" id = "fileLd'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileLd']+"</li>");
                                    //$('#file-progress').append('<li class="list-group-item" id = "fileProg'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileProg']+"</li>");
                                    //$('#file-status').append('<li class="list-group-item" id = "fileStatus'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileStatus']+"</li>");

                               }
                            
                            }
                            for (i=0;i< d["progress"][startTime]["log"].length;i++)
                            {
                                $('#log-container').append(d["progress"][startTime]["log"][i]).append("<br>");                                
                            }

                            if ((d["progress"][startTime]["status"]) == "FILE_UPLOAD")
                            {
                                $('#progress-status').html('Загрузка данных из файла.');
                            }

                            if ((d["progress"][startTime]["status"]) == "PROCESSEND" )
                            {
                                $('#progress-status').html('Данные успешно загружены.');
                                $('#progress-loader').hide();
                                clearInterval(progress);
                                setTimeout(function () { progress = null;}, 1000);
                            }
                            if ((d["progress"][startTime]["status"]) == "PROCESSERROR")
                            {
                                $('#progress-status').html('Ошибка загрузки, проверьте последний файл из списка.');
                                $('#progress-loader').hide();
                                clearInterval(progress);
                                setTimeout(function () { progress = null;}, 1000);
                            }
                            
                            if ((d["progress"][startTime]["status"]) == "PROCESSSTOPED")
                            {
                                $('#progress-status').html('Ошибка загрузки, загрузка прервана');
                                $('#progress-loader').hide();
                                clearInterval(progress);
                                setTimeout(function () { progress = null;}, 1000);
                            }
                            
                        }
                    })
                }, 1000);
            }
        });
    </script>
</head>
<body>
<div class="b-main-container">
    <div class="main-container-header">
        <h2 style="margin: 0px; font-size: 21px; background-color: #E4E4E4; color: black;">Мастер загрузки данных о продажах из Excel файлов (БАТ КТ)</h2>
    </div>
   <div>  
 
  <details open="open" style="font-size: 20px;">
    <summary>Шаг 1</summary> 
    <div  class="form-group" style = "width: 100%;float: left; text-align: left; padding-right: 10px; padding-bottom: 10px; padding-top: 10px; border: solid 2px #ccc; ">
     <form id="uploadform" method="POST" enctype="multipart/form-data" action  = "fileUploaderRoutes"> 
          <div class="form-group" style="width: 100%;float: left; text-align: left; padding-right: 10px; padding-bottom: 10px; padding-top: 10px; border: solid 0px #ccc;">              
              <input class="btn btn-primary" type="file" name="xlsfiles" id="upload-input" multiple />                        
              <button id="upload-files" type="button" name="upfile" class="btn btn-primary">Загрузить файл</button> 
          </div>       
      </form> 

      <div id="response" style = "float:left; width: 100%; padding-top: 10px; padding-bottom: 10px; overflow-y: auto; max-height: 20%;">                
          <ul id="image-list"> </ul>        
      </div>      
     </div>
  </details>

  
   <details open="open" style="font-size: 20px;">
    <summary><h3 id="progress-status"></h3></summary> 
    <div class="b-progress-bar">          
          <img id="progress-loader" src="assets/ajax-loader.gif">        
    </div>     
    <div class="form-group" style="width: 100%;float: left; text-align: left; padding-right: 10px; padding-bottom: 10px; padding-top: 10px; border: solid 0px #ccc;">            
            <button type="button" class="btn btn-primary" value="Press" id="startUploadBtn">Начать загрузку</button>
            <button type="button" class="btn btn-primary" value="Press" id="stopUploadBtn">Остановить</button>
            <button type="button" class="btn btn-primary" value="Press" id="archiveUploadBtn">Заархивировать файлы</button>
        </div>
    <iframe id="target-frame" name="target-frame" class="frame"></iframe>        
    <div class="form-group" style="font-size: 12px; width: 100%;float: left; text-align: left; padding-left: 10px; padding-right: 10px; padding-bottom: 10px; padding-top: 10px; border: solid 0px #ccc;">
		<table class="datatbl" border = "1" style="width:100%">
		   <col class="yellow"/>
		   <col class="red"/>
		   <thead>
			  <tr>
				 <th rowspan="1">Файл</th>
				 <th rowspan="1" scope="col">Регион</th>
				 <th rowspan="1" scope="col">Сумма</th>
				 <th rowspan="1" scope="col">Первый день</th>
				 <th rowspan="1" scope="col">Последний день</th>
				 <th rowspan="1" scope="col">Прогресс</th>
				 <th rowspan="1" scope="col">Статус</th>
			  </tr>
		   </thead>
		   <tbody id = "tFileList">			 
	<!-- 		  <tr id = "tr2"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr> -->
		   </tbody>
		</table>
    </div>
    <details style="font-size: 10px;">
    <summary>Детальный лог</summary>
        <div id="log-container" class="form-group" style="width: 100%;float: left; text-align: left; padding-right: 10px; padding-bottom: 10px; padding-top: 10px; border: solid 2px #ccc; overflow-y: auto; max-height: 30%;" ></div>
    </details>
  </details>
</div>
    
</body>
</html>