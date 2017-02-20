require(['jquery', 'bootstrap-sass', 'common', 'spinner'], function ($) {
    $(document).ready(function () {
        (function () {
            $("#priceSpinner").spinner('changing', function (e, newVal, oldVal) {
                var step = $("#bidPrice").data('step'),
                    minVal = ($("#bidPrice").data("min") * 1).toFixed(2),
                    diff = Math.abs((newVal - oldVal).toFixed(2));
                if (newVal <= minVal) {
                    $("#priceSpinner .subBtn").addClass("disableBtn");
                } else {
                    $("#priceSpinner .subBtn").removeClass("disableBtn");
                }
                if (diff % step != 0 && newVal != minVal) {
                    $("#bidPrice").val(oldVal.toFixed(2));
                    if (oldVal == minVal) {
                        $("#priceSpinner .subBtn").addClass("disableBtn");
                    }
                    $(".errorWrap").text("加价金额应为上浮金额的整数倍");
                    setTimeout(function () {
                        $(".errorWrap").text("");
                    }, 5000)
                }
            });

        })();

        (function () {
            $("#quantitySpinner").spinner('changing', function (e, newVal, oldVal) {
                var step = $("#quantity").data('step'),
                    minVal = ($("#quantity").data("min") * 1).toFixed(2),
                    maxVal = ($("#quantity").data("max") * 1).toFixed(2),
                    quantity=$("#quantity").val(),
                    diff = Math.abs((newVal - oldVal).toFixed(2));
                if (newVal <= minVal) {
                    $("#quantitySpinner .subBtn").addClass("disableBtn");
                } else {
                    $("#quantitySpinner .subBtn").removeClass("disableBtn");
                }
                if (newVal >= maxVal) {
                    $("#quantitySpinner .addBtn").addClass("disableBtn");
                } else {
                    $("#quantitySpinner .addBtn").removeClass("disableBtn");
                }
                if (diff % step != 0 && newVal != minVal) {
                    $("#quantity").val(oldVal);
                    if (oldVal == minVal) {
                        $("#quantitySpinner .subBtn").addClass("disableBtn");
                    }
                    $(".errorWrap").text("竞买吨数为1000的倍数");
                    setTimeout(function () {
                        $(".errorWrap").text("");
                    }, 5000)
                }
                var array=
                    [
                        {
                            "amount":1000,
                            "margins":10
                        },
                        {
                            "amount":2000,
                            "margins":15
                        },
                        {
                            "amount":3000,
                            "margins":20
                        },
                        {
                            "amount":4000,
                            "margins":25
                        },
                        {
                            "amount":5000,
                            "margins":30
                        }
                    ]
                    for(var i=0;i<array.length;i++){
                        if(quantity==array[i].amount){
                            $("#marginsTxt").text(array[i].margins);
                            $("#marginsVal").val(array[i].margins);
                        }
                        if(quantity>=array[array.length-1].amount){
                            $("#marginsTxt").text("30");
                            $("#marginsVal").val("30");
                        }
                    }

            });

        })();

        (function () {
            $('.tenderMenu li').on("click", function () {
                var index=$(this).index();
                $('body,html').animate({
                    "scrollTop" : ($(".tenderTitWrap").eq(index).offset().top-80)+"px"
                });
                $('.tenderMenu li').removeClass("selected");
                $('.tenderMenu').each(function(){
                    $(this).children("li").eq(index).addClass("selected");
                })
            });
        })();

        (function(){
            var mainContentOffsetTop=$("#fixContent").offset().top;
            $(window).scroll(function(){
                var that=$(this);
               if(that.scrollTop()>mainContentOffsetTop){
                    $(".fixed").show();
               }else{
                   $(".fixed").hide();
               }
                $(".tenderTitWrap").each(function(i){
                    var thisOffsetTop=$(this).offset().top;
                    if($(window).scrollTop()>=thisOffsetTop-180){
                        $('.tenderMenu li').removeClass("selected");
                        $('.tenderMenu').each(function() {
                            $(this).children("li").eq(i).addClass("selected");
                        });

                    }
                });
            });
        })();

        //倒计时调用
        (function () {
            var $leftStartTime = $("#leftStartTime"),
                leftStartTime = $leftStartTime.siblings("input"),
                $leftEndTime = $("#leftEndTime"),
                leftEndTime = $leftEndTime.siblings("input"),
                distanceTime = '';
            if ($leftStartTime.length) {
                $leftStartTime.deadLine({time: leftStartTime.val(), showSeconds: false});
            }
            if ($leftEndTime.length) {
                $leftEndTime.deadLine({time: leftEndTime.val(), showSeconds: false});
            }
        })();

        //submit
        (function () {
            $("#tenderSubmit").on("click", function () {
                var tenderId = $("#tenderId").val(),
                    bidPrice = $("#bidPrice").val(),
                    bidAmount = $("#quantity").val() * 1,
                    saleAmount = $("#saleAmount").val() * 1,
                    digits = /^[0-9]*[1-9][0-9]*$/,
                    minimumSaleAmount = $("#minimumSaleAmount").val() * 1;

                if (bidAmount == "") {
                    $(".amountError").text("请输入竞买吨数！");
                    return false;
                } else if (bidAmount < minimumSaleAmount) {
                    $(".amountError").text("竞买吨数不能小于" + minimumSaleAmount + "吨！");
                    return false;
                } else if (bidAmount > saleAmount) {
                    $(".amountError").text("竞买吨数不能大于" + saleAmount + "吨！");
                    return false;
                } else if (!digits.test(bidAmount)) {
                    $(".amountError").text("竞买吨数必须为整数!");
                    return false;
                }else if($("#lastAmount").length){
                    var lastAmount=$("#lastAmount").val()*1;
                    if(bidAmount<lastAmount){
                        $(".amountError").text("竞买吨数不得小于上一次竞买量！");
                        return false;
                    }
                }else {
                    $(".amountError").text("");
                }

                $.ajax({
                    url: '/account/bid/releaseBid',
                    type: 'POST',
                    data: {
                        tenderId: tenderId,
                        quotePrice: bidPrice,
                        quoteQuantity: bidAmount
                    },
                    success: function (response) {
                        if (response.success) {
                            $(".bg_img").addClass("yes");
                            $("#modalInfo_1").text("报价成功！");
                            $("#modalRemind_1").modal("show");
                        } else {
                            if (response.error == "noPayDeposit") {
                                $("#modalInfo_1").text("您还没有缴纳保证金！");
                                $("#modalRemind_1").modal("show");
                            }
                        }
                        $("#md_ok_1").on("click", function () {
                            location.href='/account/bid';
                        });
                    }
                });

            });
            //保证金
            $("#signTender").on("click", function () {
                var tenderId = $("#tenderId").val(),
                    margins= $("#marginsVal").val(),
                    quantity=$("#quantity").val();
                $.ajax({
                    url: '/account/bid/apply',
                   type: 'POST',
                    data: {
                        tenderId: tenderId,
                        margins:margins,
                        quantity:quantity
                    },
                    success: function (response) {
                        if (response.success) {
                            $(".bg_img").addClass("yes");
                            $("#modalInfo_1").text("报名成功！");
                            $("#modalRemind_1").modal("show");
                        } else {
                            if (response.error == "noHasTreasurer") {
                                $("#modalInfo_1").text("没有分配财务人员，已短信通知管理员！");
                                $("#modalRemind_1").modal("show");
                            }
                            if (response.error == "noOpenCashAccount") {
                                $("#modalInfo_1").text("没有开通资金账户，已短信通知财务人员！");
                                $("#modalRemind_1").modal("show");
                            }
                        }
                        $("#md_ok_1").on("click", function () {
                            location.href = "/tender";
                        });
                    }
                });
            });
        })();

        (function () {
            $("#reservationBtn").on("click", function () {
                $("#reservation").modal("show");
            });
            $("#validateBtn").on("click", function () {
                var $inputPhone = $("#inputPhone").val(),
                $tenderId= $("#tenderId").val(),
                    partten = /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/;
                if ($inputPhone == "") {
                    $(".modalErrorWrap").text("预约手机号不能为空！");
                    return false;
                } else if (!partten.test($inputPhone)) {
                    $(".modalErrorWrap").text("请输入正确格式的手机号！");
                    return false;
                } else {
                    $(".modalErrorWrap").text("");
                    $.ajax({
                        url: '/tender/subscribeTender',
                        method: 'POST',
                        data: {
                            tenderId:$tenderId,
                            phone: $inputPhone
                        },
                        success: function (response) {
                           if(response.success){
                               $(".modal").modal("hide");
                               setTimeout(function(){
                                   $("#modalRemind_2 .bg_img").addClass("yes");
                                   $("#modalRemind_2").modal("show");
                                   $("#md_ok_2").on("click",function(){
                                       $(".modal").modal("hide");
                                       setTimeout(function(){
                                           window.location.reload();
                                       });
                                   });
                               },500);

                           }
                        }

                    });
                }
            });
        })()
    });
});