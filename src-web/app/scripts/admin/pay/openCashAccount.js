require(['jquery','common','jquery-validation'],function($){
    $(function(){
        (function(){
            jQuery.validator.addMethod("pwdPattern", function (value) {
                var val = $.trim(value);
                return /^[\w|_]{6,20}$/.test(val);
            },'输入格式不正确');
        })();

        var formValidate = null;

        (function(){
            var registerForm = $('#setPwdForm');
            formValidate = registerForm.validate({
                rules:{
                    authCode:{
                        required:true,
                        digits:true,
                        maxlength:6,
                        minlength:6
                    },
                    password:{
                        required:true,
                        pwdPattern:true
                    },
                    confirmPassword:{
                        required:true,
                        pwdPattern:true,
                        equalTo:'#password'
                    }
                },
                messages:{
                    authCode:{
                        required:'<div class="error">请输入手机验证码</div>',
                        digits:'<div class="error">请输入数字</div>',
                        maxlength:'<div class="error">请输入6位手机验证码</div>',
                        minlength:'<div class="error">请输入6位手机验证码</div>'
                    },
                    password:{
                        required:'<div class="error">请输入密码</div>',
                        pwdPattern:'<div class="error">请输入6－20个英文字母／数字／或者下划线组成</div>'
                    },
                    confirmPassword:{
                        required:'<div class="error">请输入确认密码</div>',
                        pwdPattern:'<div class="error">请输入6－20个英文字母／数字／或者下划线组成</div>',
                        equalTo:'<div class="error">两次密码不一致</div>'
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
                        if( $('#password').hasClass('error-wrap') || $('#confirmPassword').hasClass('error-wrap') ){
                            return false;
                        }
                        $('#loading').show();
                        $('#mask').show();
                        $.ajax({
                            url:'/admin/finance/openCashAccount',
                            method:'POST',
                            data:{
                                authCode:$.trim($('#authCode').val()),
                                payPwd:$.trim($('#password').val()),
                                payConfirmPwd:$.trim($('#confirmPassword').val())
                            },
                            success:function(response){
                                if( response.success ){
                                    setTimeout(function(){
                                        $('#loading').hide();
                                        $('#mask').hide();
                                        window.location.href = '/admin/finance/setSuccess';
                                    },2000);

                                }else{
                                    $('#loading').hide();
                                    $('#mask').hide();
                                    if( response.error == 'payPwdError' ){
                                        formValidate.showErrors({
                                            'password':'<div class="error">请输入6－20个英文字母／数字／或者下划线组成的支付密码</div>'
                                        });
                                    }else if( response.error == 'payPwdNotEquals' ){
                                        formValidate.showErrors({
                                            'confirmPassword':'<div class="error">请输入6－20个英文字母／数字／或者下划线组成的支付密码</div>'
                                        });
                                    }else if( response.error == 'loginPwdEquals'){
                                        formValidate.showErrors({
                                            'password':'<div class="error">不可与登录密码相同</div>'
                                        });
                                    }else if( response.error == 'Auth_CodeError' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码错误</div>'
                                        });
                                    }else if( response.error == 'Auth_CodeExpire' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码已过期</div>'
                                        });
                                    }
                                }
                            },
                            error:function(jqXHR, textStatus, exception){
                                $('#loading').hide();
                                $('#mask').hide();
                                var XHRStatus = jqXHR.status;
                                if(XHRStatus ==503){
                                    $("#server-error-msg").text("银行网络异常，请稍后重试...");
                                    $('#modal-server-error').modal('show');
                                }else if(XHRStatus==409){
                                    var message = JSON.parse(jqXHR.responseText).error;
                                    $("#server-error-msg").text(message);
                                }else{
                                    $("#server-error-msg").text("服务器开小差,请稍后再试...");
                                }
                                $('#modal-server-error').modal('show');
                                $("#serviceErrorConfirm").off("click").on("click",function(){
                                    $(".modal").modal("hide");
                                    window.location.href = "/admin/finance";
                                });
                            }
                        });
                    }else{
                        return false;
                    }
                    return false;
                }
            })
        })();

        (function(){
            var yzm     = $('#yzm'),
                yzmTips = $('#yzmTips');
            yzm.getYZM(beforeFn);
            function beforeFn(){
                var flag = null;
                $.ajax({
                    url:'/admin/finance/sendOpenCashAccountAuthCode',
                    method:'POST',
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
                                'authCode':'<div class="error">发送频率过高, 稍后再试!</div>'
                            });
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            formValidate.showErrors({
                                'authCode':'<div class="error">发送验证码条数今天已经达到上限,请明日再试!</div>'
                            });
                            flag = false;
                        }
                    }
                });
                return flag;
            }
        })();
    });
})