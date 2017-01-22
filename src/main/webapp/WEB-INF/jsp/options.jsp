<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>

    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>

    <script type="text/javascript">
        $(document).ready(function () {

            function showTable() {
                $.ajax('/load-table', {
                    success: function (data) {
                        var table = $('.resultTable');
                        table.hide();
                        table.text('');
                        table.append(data);
                        table.slideDown();
                    }
                });
            }

//            Show DB as table
            $('.showWithAjax').on('click', function () {
                showTable();
            } );

            //            Delete from db
            $('.deleteAllAjax').on('click', function() {
                $.ajax('/delete-all', {
                    success: function (data) {
                        $('.resultTable').hide();

                    }
                });
            });

//            Uploading file to DB
            $('.sendFileToDBWithAjax').on('click', function () {
                var data = new FormData();
                jQuery.each(jQuery('.file')[0].files, function (i, file) {
                    data.append('file-' + i, file);
                });

                jQuery.ajax({
                    url: '/upload',
                    data: data,
                    cache: false,
                    contentType: false,
                    processData: false,
                    type: 'POST',
                    success: function (data) {
                        //alert(data);
                    },
                    error: function() {
                        $('#uploadResult').text('Error...');;
                    },
                    timeout: 5000,
                    beforeSend: function() {
                        $('#uploadResult').text('Loading...');
                    },
                    complete: function() {
                        $('#uploadResult').text('Loading complete!');
                        showTable();
                    }
                });
            })

        });
    </script>
    <style>
        table {
            width: 80%;
            background-color: aliceblue;
            margin-left: 10%;
            margin-top: 5%;
            border-radius: 10px;
            border: 5px solid green;
            display: none;
        }

        th {
            background-color: aqua;
        }

        tr {
            border-bottom: solid;
            border-color: brown;
        }
    </style>
</head>
<body>

<div>
    <input class="file" type="file" name="file">
    <button class="sendFileToDBWithAjax">Отправить</button>
    <div id="uploadResult">  </div>
</div>

<button class="deleteAllAjax">Delete all</button>
<button class="showWithAjax">Show all</button>

<table class="resultTable"></table>

</body>
</html>