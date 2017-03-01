<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Call tracking pro</title>
    <script src="js/jquery-3.1.1.min.js"></script>
    <script src="js/script.js"></script>
    <script src="js/jquery.cookie.js"></script>


    <link rel="stylesheet" href="css/table.css">
    <link rel="stylesheet" href="css/styles.css">

</head>
<body>


<button id="exitButton">Выйти</button>
<div id="headerHorizontalDiv">
    <div>
        <h1>Состояние</h1>
        <table id="phonesTable" class="simple-little-table"></table>
    </div>

    <div>
        <h1>События</h1>
        <textarea id="logArea"></textarea>
    </div>

    <div>
        <h1>Заблокированные IP</h1>
        <textarea id="blackListArea"></textarea>
        <div id="blockButtons">
            <input id="ipToBlock" type="text">
            <button id="buttonBlockIp" type="submit">Заблокировать</button>
            <button id="buttonUnBlockIp" type="submit">Разблокировать</button>
        </div>
        <h1>Минут до автоблока</h1>
        <div id="blockTimerButtons">
            <input id="timeToBlock" type="text">
            <button id="saveBlockTimeButton" type="submit">Задать</button>
        </div>
    </div>
</div>


<h1>История звонков</h1>
<div id="historyButtons">
    <input type="text" id="dateFrom" value="2017-02-07 00:00:00">
    <input type="text" id="dateTo" value="2017-02-15 00:00:00">
    <button type="submit" id="historyInButton">Показать входящие</button>
    <button type="submit" id="historyOutButton">Показать исходящие</button>
    <button type="submit" id="timeResetButton">Сброс</button>
</div>

<table id="divHistory" class="simple-little-table"></table>

</body>
</html>
