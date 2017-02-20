require(['jquery','bootstrap-sass','common','select2'],function($){
    $(function(){
        $('select').select2({
            placeholder:'请选择',
            minimumResultsForSearch: Infinity
        });
    });
});