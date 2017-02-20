require(['jquery','bootstrap-sass','datePicker','common','select2','select2-cn','ueconfig','ueall','jquery-validation'],function($){
    $(function(){
        var referrer=document.referrer;
       $("#returnBtn").on("click",function(){
            location.href=referrer;
       });
    });
});