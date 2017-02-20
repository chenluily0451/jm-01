require(['jquery','bootstrap-sass','common'],function($){
    (function(){

        $(".sortWrap li").on("click",function(){
            var dataKey=$(this).data('key');
            $(this).parent().siblings("input").val(dataKey);
            $("#searchWrap").submit();
        });

    })()
});