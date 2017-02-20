require(['jquery','bootstrap-sass','common','jquery-validation','select2','select2-cn'],function($){
    $(function(){
        jQuery.validator.addMethod("bankNameCheck", function (value, element) {
            var val = $.trim(value);
            return  !/^([\[\]\{\}@#$%?_~!^&\*\(\)])$/.test(val)
        },'输入格式不正确');
        jQuery.validator.addMethod("cardCheck", function (value, element) {
            var val = $.trim(value);
            return  /^\d+(?:\s\d+)+$/.test(val)
        },'输入格式不正确');
        jQuery.validator.addMethod("chineseCheck", function (value, element) {
            var val = $.trim(value);
            return  !/[^\u4e00-\u9fa5]/.test(val)
        },'请输入中文');

        function kyamount(){
            // 获取资金账户余额请求
            var cashCount = $('#cashCount');
            var price = null;
            var requestComplete="";
            cashCount.html('<img src="/images/common/numLoading.gif" alt=""/>');
            $.ajax({
                url:'/account/finance/asset/queryAccountBalance',
                method:'GET',
                success:function(response){
                    if( response.success ){
                        price = formatPrice(response.kyAmount);
                        if( typeof price == 'string' ){
                            cashCount.html(price);
                        }
                    }
                },
                error : function(){
                    cashCount.html("--");
                }
            });
            function formatPrice(val) {
                var n = Math.abs(parseFloat(val)).toFixed(2);
                return n.replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
            }
            return false;
        };
        kyamount();
        var formValidate = null;
        (function(){
            // 绑定出金账户表单验证
            var cardForm = $('#cardForm'),
                yzm      = $('#yzm');
            formValidate = cardForm.validate({
                rules:{
                    cardAccountName:{
                        required:true,
                        chineseCheck:true,
                        maxlength:30
                    },
                    bankName:{
                        required:true
                    },
                    provinceName:{
                        required:true
                    },
                    cityName:{
                        required:true
                    },
                    childBankName:{
                        required:true
                    },
                    accountName:{
                        required:true,
                        cardCheck:true
                    },
                    cardNum:{
                        required:true,
                        digits:true,
                        maxlength:30
                    },
                    yzm:{
                        required:true,
                        digits:true,
                        maxlength:6,
                        minlength:6
                    }
                },
                messages:{
                    cardAccountName:{
                        required:'<div class="error">请填写账户名</div>',
                        chineseCheck:'<div class="error">账户名只能输入中文不包括特殊字符</div>',
                        maxlength:'<div class="error">账户名最多为30个字</div>'
                    },
                    bankName:{
                        required:'<div class="error">请选择开户行</div>'
                    },
                    cityName:{
                        required:'<div class="error">请选择城市</div>'
                    },
                    provinceName:{
                        required:'<div class="error">请选择省份</div>'
                    },
                    childBankName:{
                        required:'<div class="error">请选择开户行支行</div>'
                    },
                    cardNum:{
                        required:'<div class="error">请填写账户号</div>',
                        digits:'<div class="error">请输入合法的账户号</div>',
                        maxlength:'<div class="error">账户号最多为30位</div>'
                    },
                    yzm:{
                        required:'<div class="error">请输入手机验证码</div>',
                        digits:'<div class="error">请输入数字</div>',
                        maxlength:'<div class="error">请输入6位手机验证码</div>',
                        minlength:'<div class="error">请输入6位手机验证码</div>'
                    }
                },
                focusInvalid:false,
                errorElement:'div',
                errorClass:'error-wrap',
                errorPlacement:function(error,element){
                    $(element).parent().siblings('.error-height').append(error);
                },
                success:function(label){
                    label.html('<div class="success"></div>');
                },
                onsubmit:true,
                submitHandler:function(form){
                    var flag = formValidate.valid();
                    if( flag ){
                        $.ajax({
                            url:'/account/finance/asset/addBindBankCard',
                            method:'POST',
                            data:{
                                cardAccountName:$.trim($('#cardAccountName').val()),
                                cardNum:$.trim($('#cardNum').val()),
                                childBankCode:$.trim($('#childBankName').val()),
                                authCode:$.trim($('#yzm').val())
                            },
                            success:function(response){
                                if( response.success==true ){
                                    $('#modal').modal('hide');
                                    setTimeout(function(){
                                        location.reload();
                                    },1000);
                                }else{
                                     if( response.error == 'Auth_PhoneIllegal' ){
                                         formValidate.showErrors({
                                             'yzm':'手机号格式不正确'
                                         });
                                     }else if( response.error == 'Auth_CodeError' ){
                                         formValidate.showErrors({
                                             'yzm':'手机验证码错误'
                                         });
                                     }else if( response.error == 'Auth_CodeExpire' ){
                                         formValidate.showErrors({
                                             'yzm':'手机验证码已过期'
                                         });
                                     }
                                }
                            }
                        })
                    }else{
                        return false;
                    }
                    return false;
                }
            })
        })();

        (function(){
            function beforeFn(){
                // 发送验证码前置回调函数
                var flag = false;
                var yzm = $('#yzmBtn');
                var errorArr = ['发送频率过高, 稍后再试!','发送验证码条数今天已经达到上限,请明日再试!'];
                if( $('#yzm-error').is(':visible') && errorArr.indexOf($('#yzm-error .error').html())>=0 ){
                    return false;
                }
                $.ajax({
                    url:'/account/finance/asset/sendBindCardAuthCode',
                    method:'GET',
                    async:false,
                    success:function(response){
                        if(response.error=='sendAuthCode_highRate'){
                            formValidate.showErrors({
                                'yzm':'<div class="error">发送频率过高, 稍后再试!</div>'
                            });
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            yzm.parent().addClass('error-status');
                            formValidate.showErrors({
                                'yzm':'<div class="error">发送验证码条数今日已达上限,请明日再试!</div>'
                            });
                            flag = false;
                        }else{
                            flag = true;
                            $('#yzmTips').animate({
                                height:22,
                                opacity:1
                            },1000,function(){
                                $(this).delay(1000).animate({
                                    height:0,
                                    opacity:0
                                },1500)
                            });
                        }
                    }
                });
                return flag;
            }
            $('#yzmBtn').getYZM(beforeFn);
        })();

        (function(){
            var btnClose = $('#modal .close');
            btnClose.on('click',function(){
                formValidate.resetForm();
                $('#yzm').parent().removeClass('error-status');
            });

            $('#identity').on('click',function(){
                $('#modal_1').modal('show');
            });
        })();

        (function(){
            $('#bindCard').on('click',function(){
                $('#modal').modal('show');
            });
        })();
        //刷新余额
        $("#refresh").click(kyamount);
        //省市联动

            $("#provinceName,#cityName,#bankName,#childBankName").select2({
                placeholder: "请选择",
                language: "zh-CN"
            });

            $.ajax({
                url:'/bank/loadAllBank',
                type:'GET',
                success:function(response){
                    $('#bankName').html('<option value=""></option>');
                    $.each(response,function(i,value){
                        $('#bankName').append('<option value='+response[i].bankCode+'>'+response[i].bankName+'</option>')
                    });
                }
            });
            function loadProvince(){
                $.ajax({
                    url:'/bank/loadBankSiteProvinces',
                    type:'GET',
                    success:function(response){
                        $('#provinceName').html('<option value=""></option>');
                        $.each(response,function(i,value){
                            $('#provinceName').append('<option value='+response[i].provinceCode+'>'+response[i].provinceName+'</option>')
                        });
                    }
                });
            }
            loadProvince();


            $('#provinceName').on('select2:select', function() {
                var provinceId=$(this).val();
                $('#cityName').prop("disabled",false)
                              .html('<option value=""></option>');
                $('#childBankName').html('<option value=""></option>');
                $.ajax({
                    url:'/bank/loadBankSiteCities/'+provinceId,
                    type:'GET',
                    success:function(response){
                        $.each(response,function(i,value){
                            $('#cityName').append('<option value='+response[i].cityCode+'>'+response[i].cityName+'</option>')
                        });
                    }
                });
            });

            $('#cityName').on('select2:select', function () {
                var cityCode=$(this).val();
                $('#childBankName').prop("disabled",false)
                                   .html('<option value=""></option>');
                $.ajax({
                    url:'/bank/loadAllChildBanks/'+cityCode+'/'+$('#bankName').val(),
                    type:'GET',
                    success:function(response){
                        $.each(response,function(i,value){
                            $("#childBankName").append('<option value='+response[i].childBankCode+'>'+response[i].childBankName+'</option>')
                        });
                    }
                });
            });
            $("#bankName").on('select2:select', function () {
                loadProvince();
                $('#provinceName').prop("disabled",false);
                $('#cityName').html('<option value=""></option>');
                $('#childBankName').html('<option value=""></option>');
            });
        $("select").change(function(){
            $(this).valid()
        });
        //删除绑定银行卡
        $("#del").on("click",function(){
            $("#modal_2").modal("show");
        });
        $("#delBtn").on("click",function(){
            var bankCardId=$(this).data("bankcard");
            $.ajax({
                url:'/account/finance/asset/deleteBankCard/',
                type:'POST',
                data:{'id':bankCardId},
                success:function(){
                    $("#modal_2").modal("hide");
                    setTimeout(function(){
                        location.reload();
                    },800)
                }
            })
        });
        //确认金额
        (function(){

            $("#checkBtn").on("click",function(){
                var $checkMoney=$("#checkMoney").val();
                if($checkMoney==''){
                    $(".checkMoneyError").text("请输入确认金额");
                }
                else if(!(/^\d+(\.\d{0,2})?$/).test($checkMoney)){
                    $(".checkMoneyError").text("确认金额最多为两位小数");
                }
                else{
                    $.ajax({
                        url:'/account/finance/asset/validateBankCard',
                        type:'POST',
                        data:{
                            id:$("#checkBtn").data("bankcard"),
                            amount: $.trim($("#checkMoney").val())
                        },
                        success:function(response){
                            if(response.success){
                                $("#modal_1").modal("hide");
                                setTimeout(function(){
                                    location.reload();
                                },800)
                            }else{
                                if(response.data == 0){
                                    location.reload();
                                }else{
                                    $(".checkMoneyError").text("确认金额出错，您还有"+response.data+"次机会")
                                }
                            }
                        }
                    })
                }
            })
        })();
    });
});