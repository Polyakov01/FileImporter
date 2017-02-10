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
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            var progress,
                    spFound = 0,
                    logPos = 0;
            $('input[type=file]').bootstrapFileInput();

            $('#startUploadBtn').click(function(event){
                $('#log-container').empty();
                startUpload();
            });
            
            $('#stopUploadBtn').click(function(event){
                console.log("CLICK");
                stopUpload();
            });
            
            
            function stopUpload () 
            {                
                time = new Date().getTime();
                console.log("FUNC");
                $.ajax({
                        type: "GET",
                        url: "fileUploader",
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
                        url: "fileUploader",
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
                $('#progress-loader').show();
                $('#log-container').show();
                $('#progress-status').html('Загрузка файла');
                // start timer
                progress = setInterval(function () {
                    // ask progress
                    $.ajax({
                        type: "GET",
                        url: "fileUploader",
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
                                console.log(d["fileLog"][i]['fileName']);

                            if ($('#filePosId'+d["fileLog"][i]['filePos']).length>0)
                            {                                
                                $('#fileRegion'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileRegion']);
                                $('#fileSum'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileSum']);
                                $('#fileFd'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileFd']);
                                $('#fileLd'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileLd']);
                                $('#fileProg'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileProg']);
                                $('#fileStatus'+d["fileLog"][i]['filePos']).html(d["fileLog"][i]['fileStatus']);
                           }
                            else
                            {
                                $('#file-list').append('<li class="list-group-item" id = "filePosId'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileName']+"</li>");
                                $('#file-region').append('<li class="list-group-item" id = "fileRegion'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileRegion']+"</li>");
                                $('#file-sum').append('<li class="list-group-item" id = "fileSum'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileSum']+"</li>");
                                $('#file-fd').append('<li class="list-group-item" id = "fileFd'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileFd']+"</li>");
                                $('#file-ld').append('<li class="list-group-item" id = "fileLd'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileLd']+"</li>");
                                $('#file-progress').append('<li class="list-group-item" id = "fileProg'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileProg']+"</li>");
                                $('#file-status').append('<li class="list-group-item" id = "fileStatus'+d["fileLog"][i]['filePos']+'">'+d["fileLog"][i]['fileStatus']+"</li>");

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
        <h2 style="margin: 0px; font-size: 21px;">Мастер загрузки данных о продажах из Excel файлов (БАТ КТ)</h2>
    </div>

   <!-- <form id="uploadform" method="POST" enctype="multipart/form-data"> -->
        <div class="form-group" style="width: 50%;float: left; text-align: left; padding-left: 10px;">
            <input type="file" name="upfile" title="Выберите файл для загрузки" class="btn btn-primary">
        </div>
        <div class="form-group" style="width: 50%;float: left; text-align: right; padding-right: 10px;">
            <button type="button" class="btn btn-primary" value="Press" id="startUploadBtn">Начать загрузку</button>
            <button type="button" class="btn btn-primary" value="Press" id="stopUploadBtn">Остановить</button>
        </div>
    <!-- </form> -->
    <iframe id="target-frame" name="target-frame" class="frame"></iframe>
    <!-- progress bar -->
    <div class="b-progress-bar">
        <img id="progress-loader" src="assets/ajax-loader.gif">
        <h3 id="progress-status"></h3>
    </div>     
    <div>
        <ul style="width: 20%; float: left;" class="list-group" id="file-list">
            <li class="list-group-item">Файл</li>
        </ul>
        <ul style="width: 10%; float: left;" class="list-group" id="file-region">
            <li class="list-group-item">Регион</li>
        </ul>                
        <ul style="width: 10%; float: left;" class="list-group" id="file-sum">
            <li class="list-group-item">Сумма</li>
        </ul>
        <ul style="width: 18%; float: left;" class="list-group" id="file-fd">
            <li class="list-group-item">Первый день</li>
        </ul>
        <ul style="width: 18%; float: left;" class="list-group" id="file-ld">
            <li class="list-group-item">Последний день</li>
        </ul>
        <ul style="width: 12%; float: left;" class="list-group" id="file-progress">
            <li class="list-group-item">Прогресс</li>
        </ul>
        <ul style="width: 12%; float: left;" class="list-group" id="file-status">
            <li class="list-group-item">Статус</li>
        </ul>                
    </div>
    
    <div>
		<table class="datatbl" border = "1" style="width:100%" bgcolor = "#0000">
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
		   <tbody >
			  <tr>
				 <td id = "tFilePosId"></td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
			  </tr>
			  <tr id = "tr2">
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
				 <td>Алматы</td>
			  </tr>
		   </tbody>
		</table>
	</div>
    
    
    <div id="log-container"></div>
</div>
</body>
</html>