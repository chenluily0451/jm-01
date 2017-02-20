require(['jquery','common','jquery-validation'],function($){
    $(function(){
        (function(){
            // 图片验证码
            var verifycode = $('#verifycode,#changeImgCode'),
                imgSrc     = '/loadImgCode?time=',
                timer      = null,
                spaceTime  = 10;
            verifycode.on('click',function(){
                if( timer ){
                    clearTimeout(timer);
                }
                timer = setTimeout(function(){
                    verifycode.attr('src',imgSrc+(new Date().getTime()));
                },spaceTime);
                return false;
            });
        })();

        (function(){
            $('#agreeBtn').on('click',function(){
                $('#agreementModal').modal('show');
            });
            $('#secretBtn').on('click',function(){
                $('#secretModal').modal('show');
            });
        })();

        (function(){
            jQuery.validator.addMethod("pwdPattern", function (value, element) {
                var val = $.trim(value);
                if( val && val.length>=6 && val.length<=16 && (!/[\u4e00-\u9fa5]/.test(val)) ){
                    if( /^[a-zA-Z]+$/.test(val) || /^\d+$/.test(val) || /^[\[\]\{\}@#$%?_~!^&\*\(\)]{6,16}$/.test(val) ){
                        return false;
                    }else{
                        return  /^([\w\[\]\{\}@#$%?_~!^&\*\(\)]){6,16}$/.test(val)
                    }
                }else{
                    return false;
                }
            },'输入格式不正确');
            jQuery.validator.addMethod("securePhoneExists", function (value, element) {
                var flag = null;
                if( $(element).parent().next().find('.success-wrap').length>0 ){
                    return true;
                }
                $.ajax({
                     global:false,
                     url:'/checkSecurePhoneExists',
                     method:'POST',
                     async:false,
                     data:{
                         securePhone:$.trim(value)
                     },
                     success:function(response){
                        if(response==true){
                            flag = false;
                        }else if(response==false){
                            flag = true;
                            $('#yzm').removeClass('disabled');
                        }
                     }
                });
                return flag;
            },'输入格式不正确');
            jQuery.validator.addMethod("equalPwd", function (value, element) {
                return !$('#password-error .error').length;
            },'输入格式不正确');
        })();

        var formValidate = null;
        (function(){
            var registerForm = $('#registerForm'),
                yzm          = $('#yzm');
            formValidate = registerForm.validate({
                rules:{
                    securePhone:{
                        required:true,
                        telPattern:true,
                        securePhoneExists:true
                    },
                    password:{
                        required:true,
                        pwdPattern:true
                    },
                    confirmPassword:{
                        required:true,
                        equalTo:'#password',
                        equalPwd:true
                    },
                    imgCode:{
                        required:true,
                        maxlength:6,
                        minlength:6
                    },
                    authCode:{
                        required:true,
                        digits:true,
                        maxlength:6,
                        minlength:6
                    },
                    agreement:{
                        required:true
                    }
                },
                messages:{
                    securePhone:{
                        required:'<div class="error">请填写手机号码</div>',
                        telPattern:'<div class="error">请输入符合规范的手机号码</div>',
                        securePhoneExists:'<div class="error">手机号已经注册过</div>'
                    },
                    password:{
                        required:'<div class="error">请输入密码</div>',
                        pwdPattern:'<div class="error">6-16个字符、字母、数字、特殊符号；至少两种以上组合；字母区分大小写</div>'
                    },
                    confirmPassword:{
                        required:'<div class="error">请输入确认密码</div>',
                        equalTo:'<div class="error">两次密码不一致</div>',
                        equalPwd:'<div class="error">请输入正确的密码</div>'
                    },
                    imgCode:{
                        required:'<div class="error">请输入图形验证码</div>',
                        maxlength:'<div class="error">请输入6位图形验证码</div>',
                        minlength:'<div class="error">请输入6位图形验证码</div>'
                    },
                    authCode:{
                        required:'<div class="error">请输入手机验证码</div>',
                        digits:'<div class="error">请输入数字</div>',
                        maxlength:'<div class="error">请输入6位手机验证码</div>',
                        minlength:'<div class="error">请输入6位手机验证码</div>'
                    },
                    agreement:{
                        required:'请仔细阅读并同意条款'
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
                            url:'/register',
                            method:'POST',
                            data:{
                                securePhone:$.trim($('#securePhone').val()),
                                password:$.trim($('#password').val()),
                                confirmPassword:$.trim($('#confirmPassword').val()),
                                authCode:$.trim($('#authCode').val()),
                                imgCode:$.trim($('#imgCode').val())
                            },
                            success:function(response){
                                if( response == 'success' ){
                                    window.location.href = '/registerCompany';
                                }else{
                                    if( response == 'Auth_PhoneIllegal' ){
                                        formValidate.showErrors({
                                            'securePhone':'<div class="error">手机号格式不正确</div>'
                                        });
                                    }else if( response == 'Auth_CodeError' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码错误</div>'
                                        });
                                    }else if( response == 'Auth_CodeExpire' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码已过期</div>'
                                        });
                                    }else if( response == 'imgCodeError' ){
                                        formValidate.showErrors({
                                            'imgCode':'<div class="error">图形验证码错误</div>'
                                        });
                                    }else if( response == 'securePhoneExists' ){
                                        formValidate.showErrors({
                                            'securePhone':'<div class="error">手机号已经存在</div>'
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
            $('#securePhone,#password,#confirmPassword,#imgCode,#authCode').on('focus',function(){
                var curDom = getInfoDom($(this));
                if( !curDom.successWrap.is(':visible') ){
                    curDom.infoWrap.show();
                }
            }).blur(function(){
                var curDom = getInfoDom($(this));
                curDom.infoWrap.hide();
            }).keydown(function(){
                var curDom = getInfoDom($(this));
                curDom.infoWrap.hide();
            }).keyup(function(){
                $('#authCode-error-other').hide();
                if( $('#securePhone-error .success').length && /^\w{6}$/.test($.trim($('#imgCode').val())) ){
                    $('#yzm').removeClass('disabled');
                }else{
                    $('#yzm').addClass('disabled');
                }
            });
            function getInfoDom(obj){
                var wrapper = obj.parent().siblings('.info');
                var formValidate = obj.parent().siblings('.error-height');
                return {
                    infoWrap:wrapper.find('.info-wrap'),
                    errorWrap:formValidate.find('.error'),
                    successWrap:formValidate.find('.success')
                }
            }
        })();

        (function(){
            var yzm     = $('#yzm'),
                yzmTips = $('#yzmTips');
            yzm.getYZM(beforeFn);
            function beforeFn(){
                var flag = null;
                if( $('#imgCode-error .error').length ){
                    return false;
                }
                $.ajax({
                    url:'/sendRegisterAuthCode',
                    method:'POST',
                    async:false,
                    data:{
                        securePhone:$.trim($('#securePhone').val()),
                        imgCode:$.trim($('#imgCode').val())
                    },
                    success:function(response){
                        if( response.success){
                            $('#yzmTips').animate({
                                height:22,
                                opacity:1
                            },1000,function(){
                                $(this).delay(1000).animate({
                                    height:0,
                                    opacity:0
                                },1500)
                            });
                            flag = true;
                        }else if( response.error == 'imgCodeError' ){
                            formValidate.showErrors({
                                'imgCode':'<div class="error">图片验证码错误</div>'
                            });
                            flag = false;
                        }else if( response.error == 'userExisted') {
                            formValidate.showErrors({
                                'securePhone':'<div class="error">手机号已经存在,请尝试换个手机号码!</div>'
                            });
                            $('#yzm').addClass('disabled');
                            flag = false;
                        } else if(response.error=='sendAuthCode_phoneIllegal'){
                            formValidate.showErrors({
                                'securePhone':'<div class="error">手机号格式错误,请输入正确的手机号!</div>'
                            });
                            $('#yzm').addClass('disabled');
                            flag = false;
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

        (function(){
            // 勾选同意条款
            $('#argument').on('change',function(){
                if( $(this).prop('checked') ){
                    $('#submitBtn').removeClass('disabled');
                }else{
                    $('#submitBtn').addClass('disabled');
                }
            });
        })();
    });
})