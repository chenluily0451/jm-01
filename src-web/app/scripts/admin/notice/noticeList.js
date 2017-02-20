require(['jquery', 'bootstrap-sass', 'datePicker', 'common'], function ($) {
    $(".backOutBtn").on("click", function () {
        $("#modal_1").modal("show");
        $("#modalRevokedBtn").attr("data-id",$(this).data("id"));
    });

    $("#noticeName").blur(function(){
        $(this).val($.trim($(this).val()));
    });

    $("#modalRevokedBtn").on("click", function () {
        var dataId=$(this).data("id");
        $.ajax({
            url: '/admin/notice/backOutNotice/'+dataId,
            type: 'GET',
            success: function () {
                window.location.reload();
            }
        })
    })
});