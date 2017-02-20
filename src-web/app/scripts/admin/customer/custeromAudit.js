require(['jquery', 'lightbox', 'bootstrap-sass'], function ($, lightbox) {
    $(function () {
        (function () {
            $('#lightbox .lb-outerContainer').prepend('<a class="lb-close-customer"></a>');
            $('.lb-close-customer').on('click',function(){
                $('#lightboxOverlay').trigger('click');
            });
        })();

        (function () {
            var checkBtn = $('#checkBtn'),
                failBtn = $('#failBtn'),
                passBtn = $('#passBtn'),
                textarea = $('#textarea'),
                modalReason = $('#modalReason');
            checkBtn.on('click', function () {
                modalReason.modal('show');
            });
            failBtn.on('click', function () {
                if ($.trim(textarea.val())) {
                    $.ajax({
                        url: '/admin/customer/auditNotPass',
                        method: 'POST',
                        data: {recordId: $(this).attr("data-companyid"), auditMsg: textarea.val()},
                        success: function () {
                            modalReason.modal('hide');
                            window.location.reload();
                        }
                    });
                } else {
                    textarea.focus();
                }
            });
            passBtn.on('click', function () {
                $.ajax({
                    url: '/admin/customer/auditPass',
                    method: 'POST',
                    data: {recordId: $(this).attr("data-companyid"), auditMsg: textarea.val()},
                    success: function () {
                        modalReason.modal('hide');
                        window.location.reload();
                    }
                });
            });
        })();

        (function(){
            $('#modalReason .close_modal').on('click',function(){
                $('#textarea').val('');
                $('#lastWord').html('200');
            });
        })();

        (function(){
            var maxLength = 200;
            var val = 0,curLen = 0;
            var lastWord = $('#lastWord');
            $('#textarea').on('propertychange input',function(){
                val = $.trim($(this).val());
                curLen = maxLength - val.length;
                if(val.length>maxLength){
                    $(this).val(val.substr(0,maxLength));
                    lastWord.html(0);
                }else{
                    lastWord.html(curLen);
                }
            });
        })();
    });
});