require(['jquery','bootstrap-sass','common'],function($) {
$(function(){

    if($(window).scrollTop()>$("#menuTop").offset().top){
        $(".abs").show();
        scrollFun()
    }

    $(".left-ban li").on("click",function(){

        var $index=$(this).index();
        if($index==0){
            return false;
        }
        $(".left-ban a").removeClass("active");

        $(".left-ban").each(function(){
            $(this).find("li")
                .eq($index)
                .children("a")
                .addClass("active");
        });
    });

    $(window).scroll(function(){scrollFun()});

    function scrollFun(){
        var menuOffsetTop=$("#menuTop").offset().top,
            absOffsetTop=$(".abs").offset().top,
            $staticMenu=$(".staticMenu"),
            scrollTop=$(window).scrollTop();
        if(scrollTop >= menuOffsetTop){
            $(".abs").show();
        }else{
            $(".abs").hide();
        }

        $(".staticMenu").each(function(i){

            if(absOffsetTop > menuOffsetTop && absOffsetTop >= $staticMenu.eq(i).offset().top-30){
                $(".left-ban").each(function(){
                    $(".left-ban a").removeClass("active");
                    $(this).find("li")
                        .eq(i+1)
                        .children("a")
                        .addClass("active");
                });
            }
        });
    }

});

});