require(['jquery', 'select2'], function ($, lightbox) {
    $(function(){
        $('select').select2({
            placeholder:'请选择',
            minimumResultsForSearch: Infinity
        });
    });
});