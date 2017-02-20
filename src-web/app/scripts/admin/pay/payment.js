require(['jquery','bootstrap-sass','common','jquery-validation'],function($){
    $(function(){
        var maxLength = 70;
        var val = 0,curLen = 0;
        var count = $('#count');
        $('#txtArea').on('propertychange input',function(){
            val = $.trim($(this).val());
            curLen = maxLength - val.length;
            if(val.length>maxLength){
                $(this).val(val.substr(0,maxLength));
                count.html(0);
            }else{
                count.html(curLen);
            }
        });
        $('#reduct').on('keyup',function(){
            if( !/(^[1-9]\d+(\.\d{0,2})?$)|(^0(\.\d{0,2})$)/.test($.trim($(this).val()) ) ){
                $(this).val('');
            }
        });

        (function(){
            var payBtn = $('#payBtn');
            payBtn.on('click',function(){
                $('#yzm').removeClass('disabled');
                $('#paymentModalForm').modal('show');
            });
            $('#payModalClose').on('click',function(){
                formValidate.resetForm();
                $('#yzm').addClass('disabled');
                $('#addStaff .error-status').removeClass('error-status');
            });
        })();

        var formValidate = null;
        (function(){
            var payModalForm = $('#payModalForm');
            jQuery.validator.addMethod("pwdPattern", function (value) {
                var val = $.trim(value);
                return /^[\w|_]{6,20}$/.test(val);
            },'输入格式不正确');
            formValidate = payModalForm.validate({
                rules:{
                    payTelCode:{
                        required:true,
                        maxlength:6,
                        minlength:6
                    },
                    payPwd:{
                        required:true,
                        pwdPattern:true
                    }
                },
                messages:{
                    payTelCode:{
                        required:'<div class="error">请输入手机验证码</div>',
                        minlength:'<div class="error">请输入6位数字手机验证码</div>',
                        maxlength:'<div class="error">请输入6位数字手机验证码</div>'
                    },
                    payPwd:{
                        required:'<div class="error">请输入支付密码</div>',
                        pwdPattern:'<div class="error">请输入6－20个英文字母／数字／或者下划线组成</div>'
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
                        $('#loading').show();
                        $('#mask').show();
                        $.ajax({
                            url:'/admin/finance/refundWholeDeposit',
                            method:'POST',
                            data:{
                                refundRecordId:$.trim($('#refundRecordId').val()),
                                authCode:$.trim($('#payTelCode').val()),
                                payPwd:$.trim($('#payPwd').val()),
                                remark:$.trim($('#txtArea').val())
                            },
                            success:function(response){
                                if( response.success ){
                                    $('#paymentModalForm').modal('hide');
                                    setTimeout(function(){
                                        $('#loading').hide();
                                        $('#mask').hide();
                                        window.location.href = '/admin/finance/tradeRecord';
                                    },2000);
                                }else{
                                    $('#loading').hide();
                                    $('#mask').hide();
                                    if( response.error == 'Auth_CodeError' ){
                                        formValidate.showErrors({
                                            'payTelCode':'<div class="error">手机验证码错误</div>'
                                        });
                                    }else if( response.error == 'Auth_CodeExpire' ){
                                        formValidate.showErrors({
                                            'payTelCode':'<div class="error">手机验证码已过期</div>'
                                        });
                                    }else if( response.error == 'sendAuthCode_highRate'){
                                        formValidate.showErrors({
                                            'payTelCode':'<div class="error">发送频率过高, 稍后再试!</div>'
                                        });
                                    }else if( response.error == 'sendAuthCode_limitation'){
                                        formValidate.showErrors({
                                            'payTelCode':'<div class="error">发送验证码条数今日已达上限,请明日再试!</div>'
                                        });
                                    }else{
                                        formValidate.showErrors({
                                            'payPwd':'<div class="error">密码错误</div>'
                                        });
                                    }
                                }
                            },
                            error:function(){
                                $('#loading').hide();
                                $('#mask').hide();
                            }
                        })
                    }else{
                        return false;
                    }
                    return false;
                }
            });
        })();

        (function(){
            $('#payModalClose').on('click',function(){
                formValidate.resetForm();
                $('#yzm').addClass('disabled');
            });
        })();

        (function(){
            // 发送验证码
            var yzm     = $('#yzm'),
                yzmTips = $('#yzmTips');
            yzm.getYZM(beforeFn);
            function beforeFn(){
                var flag = null;
                $.ajax({
                    url:'/admin/finance/sendRefundWholeDepositAuthCode',
                    method:'GET',
                    async:false,
                    success:function(response){
                        if( response.success){
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
                        }else if(response.error=='sendAuthCode_highRate'){
                            formValidate.showErrors({
                                'payTelCode':'<div class="error">发送频率过高, 稍后再试!</div>'
                            });
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            formValidate.showErrors({
                                'payTelCode':'<div class="error">发送验证码条数今天已经达到上限,请明日再试!</div>'
                            });
                            flag = false;
                        }
                    }
                });
                return flag;
            }
        })();
    });
});