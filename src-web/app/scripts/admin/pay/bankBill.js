require(['jquery','common','select2'],function($){
    $(function(){
        $('.printBtn').on('click',function(){
            $('#printCode').html($(this).attr('data-verifcode'));
            $('#modalPrintBtn').attr('href','https://enterprise.bank.ecitic.com/corporbank/cb060400_reBill.do'+'?fundAccount='+ $(this).attr('data-cashAccountNum')+'&printCode='+ $(this).attr('data-verifcode'));
            $('#accountId').html($(this).attr('data-cashaccountnum'));
            $('#modalEdit').modal('show');
            return false;
        });
        $('select').select2({
            placeholder:'请选择',
            minimumResultsForSearch: Infinity
        });
    });
})