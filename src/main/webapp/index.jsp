<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>抢红包</title>
    <script src="/static/js/jquery.min.js"></script>
</head>
<body>
<button onclick="grapRedPacket()">开始抢红包</button>
<div id="msg"></div>
<script>
    function grapRedPacket() {
        var max = 1000;
        for (var i = 1; i <= max;i ++){
            $.ajax({
                type:'POST',
                url:"userRedPacket/grapRedPacketByRedis?redPacketId=5&userId="+i,
                success:function(data){
                    $('#msg').append("<p>"+data.message+"</p>");
                },
                error:function () {
//                    alert('error');
                }
            });
        }
    }
</script>
</body>
</html>
