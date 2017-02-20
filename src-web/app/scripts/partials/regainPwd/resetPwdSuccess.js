require(['jquery','common','jquery-validation'],function($){
    $(function(){
        (function(){
            var deadLine = $('#deadLine');
            var timer    = null;
            var maxTime  = 15;
            timer = setInterval(function(){
                if(maxTime<=0){
                    clearInterval(timer);
                    window.location.href = '/login';
                }else{
                    maxTime--;
                    deadLine.html(maxTime+'秒后');
                }
            },1000);
        })();
    });
})