require(['jquery','common'],function($){
    $(function(){
        var openFinanceBtn = $('#openFinanceBtn');
        var agreements = $('#agreements');
        $('#imgCheck,#imgCheckTxt').on('click',function(){
            $('#imgCheck').toggleClass('active');
            openFinanceBtn.toggleClass('active');
        });
        agreements.on('click',function(){
            $('#agreementModal').modal('show');
            return false;
        });
    });
})