require(['jquery','common','jquery-validation'],function($){
    $(function(){
        (function(){
            var max = 10;
            var curSeconds = $('#curSeconds');
            setInterval(function(){
                max--;
                if(max<0){
                    window.location.href = '/account/finance';
                }else{
                    curSeconds.html(max+'s');
                }
            },1000);
        })();
    });
})