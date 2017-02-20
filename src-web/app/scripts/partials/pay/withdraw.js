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
        var val = null;
        var payBtn = $('#payBtn');
        $('#reduct').on('keyup',function(){
            val = $.trim($(this).val());
            if( !/(^[1-9]\d*(\.\d{0,2})?$)|(^0(\.\d{0,2})$)/.test(val)  ){
                $(this).val('');
                payBtn.addClass('disabled');
                return false;
            }else{
                payBtn.removeClass('disabled');
            }
        });

        var formValidate = null;
        (function(){
            var payBtn = $('#payBtn');
            payBtn.on('click',function(){
                $('#paymentModalForm').modal('show');
            });
            $('#payModalClose').on('click',function(){
                formValidate.resetForm();
            });
        })();

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
                        $.ajax({
                            url:'/account/finance/withdraw',
                            method:'POST',
                            data:{
                                authCode:$.trim($('#payTelCode').val()),
                                bankCardId:$('#payBtn').attr('data-cardid'),
                                amount:$.trim($('#reduct').val()),
                                payPwd:$.trim($('#payPwd').val()),
                                remark:$.trim($('#txtArea').val())
                            },
                            success:function(response){
                                if( response.success ){
                                    $('#paymentModalForm').modal('hide');
                                    $('#loading').show();
                                    $('#mask').show();
                                    setTimeout(function(){
                                        $('#loading').hide();
                                        $('#mask').hide();
                                        window.location.href = '/account/finance/tradeRecord';
                                    },2000);
                                }else{
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
            // 发送验证码
            var yzm     = $('#yzm'),
                yzmTips = $('#yzmTips');
            yzm.getYZM(beforeFn);
            function beforeFn(){
                var flag = null;
                $.ajax({
                    url:'/account/finance/sendWithDrawAuthCode',
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