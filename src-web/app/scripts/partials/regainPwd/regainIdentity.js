require(['jquery','common','jquery-validation'],function($){
    $(function(){
        (function(){
            // 图片验证码
            var verifycode = $('#verifycode'),
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
            });
        })();

        var formValidate = null;
        (function(){
             var registerForm = $('#registerForm'),
                yzm          = $('#yzm');
            formValidate = registerForm.validate({
                rules:{
                    authCode:{
                        required:true,
                        digits:true,
                        maxlength:6,
                        minlength:6
                    }
                },
                messages:{
                    authCode:{
                        required:'<div class="error">请输入手机验证码</div>',
                        digits:'<div class="error">请输入数字</div>',
                        maxlength:'<div class="error">请输入6位手机验证码</div>',
                        minlength:'<div class="error">请输入6位手机验证码</div>'
                    },
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
                            url:'/regainPwd/regainIdentity',
                            method:'POST',
                            data:{
                                k:$.trim($('#userKey').val()),
                                authCode:$.trim($('#authCode').val())
                            },
                            success:function(response){
                                if( response == 'success' ){
                                    window.location.href = '/regainPwd/resetPwd?k='+$.trim($('#userKey').val());
                                }else{
                                    if( response == 'Auth_CodeError' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码错误</div>'
                                        });
                                    }else if( response == 'Auth_CodeExpire' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码已过期</div>'
                                        });
                                    }else{
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">手机验证码已过期</div>'
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
            $('#authCode').on('focus',function(){
                var curDom = getInfoDom($(this));
                if( !curDom.successWrap.is(':visible') ){
                    curDom.infoWrap.show();
                    curDom.errorWrap.hide();
                }
            }).blur(function(){
                var curDom = getInfoDom($(this));
                curDom.infoWrap.hide();
            }).keydown(function(){
                $('#authCode-error-other').hide();
                var curDom = getInfoDom($(this));
                curDom.infoWrap.hide();
            });
            function getInfoDom(obj){
                var wrapper = obj.parent().next();
                return {
                    infoWrap:wrapper.find('.info-wrap'),
                    errorWrap:wrapper.find('.error-wrap'),
                    successWrap:wrapper.find('.success-wrap')
                }
            }
        })();

        (function(){
            var yzm     = $('#yzm'),
                yzmTips = $('#yzmTips');
            yzm.getYZM(beforeFn);
            function beforeFn(){
                var flag = null;
                $.ajax({
                    url:'/regainPwd/sendRegainPwdAuthCode',
                    method:'POST',
                    async:false,
                    data:{
                        k:$.trim($('#userKey').val()),
                    },
                    success:function(response){
                        if( response.success == true ){
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
                        }else if(response.error=='sendAuthCode_highRate'){
                            if( $('#authCode-error').length ){
                                $('#authCode-error-other').hide();
                                formValidate.showErrors({
                                    'authCode':'<div class="error">发送频率过高, 稍后再试!</div>'
                                });
                            }else{
                                $('#authCode-error-other').html('发送频率过高, 稍后再试!').show();
                            }
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            formValidate.showErrors({
                                'authCode':'<div class="error">发送验证码条数今日已达上限,请明日再试!</div>'
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