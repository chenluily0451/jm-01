require(['jquery','bootstrap-sass','datePicker','common','superslide','animateNum'],function($){

    $(".jm-slide").slide({
        autoPlay:true,
        interTime: 5000,
        effect: "fold",
        delayTime: 800
    });
    var comma_separator_number_step = $.animateNumber.numberStepFactories.separator(',');
    $('.total-amount').animateNumber(
        {
            number: $("#platform-amount").val(),
            numberStep: comma_separator_number_step
        },1200
    );

    $(function() {
        $(".info-block li").lazyload({
            effect : "fadeIn"
        });
    });
    $(document).ready(function(){
        $('img').lazyload({
            effect:'fadeIn'
        });
    });

});