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
            });
        })();

        (function(){
            var registerForm = $('#registerForm'),
                formValidate = null,
                yzm          = $('#yzm'),
                submitError  = $('#submitError');
            formValidate = registerForm.validate({
                rules:{
                    securePhone:{
                        required:true,
                        telPattern:true
                    },
                    imgCode:{
                        required:true,
                        maxlength:6,
                        minlength:6
                    }
                },
                messages:{
                    securePhone:{
                        required:'<div class="error">请填写手机号码</div>',
                        telPattern:'<div class="error">请输入符合规范的手机号码</div>'
                    },
                    imgCode:{
                        required:'<div class="error">请输入图形验证码</div>',
                        maxlength:'<div class="error">请输入6位图形验证码</div>',
                        minlength:'<div class="error">请输入6位图形验证码</div>'
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
                    if( $('.error-wrap .error').length ){
                        return false;
                    }
                    if( flag ){
                        $.ajax({
                            url:'/regainPwd',
                            method:'POST',
                            data:{
                                securePhone:$.trim($('#securePhone').val()),
                                imgCode:$.trim($('#imgCode').val())
                            },
                            success:function(response){
                                if(response.success==true){
                                    submitError.hide();
                                    window.location.href = 'regainPwd/regainIdentity?k='+response.data;
                                }else{
                                    if( response.error == 'accountLocked' ){
                                        formValidate.showErrors({
                                            'securePhone':'<div class="error">账户名已被锁定</div>'
                                        });
                                    }else if( response.error == 'userNotExists' ){
                                        formValidate.showErrors({
                                            'securePhone':'<div class="error">账户名不存在,请核对后重新输入</div>'
                                        });
                                    }else if( response.error == 'imgCodeError' ){
                                        formValidate.showErrors({
                                            'imgCode':'<div class="error">图片验证码错误</div>'
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
            $('#securePhone').on('focus',function(){
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
            });
            function getInfoDom(obj){
                var wrapper = obj.parent().siblings('.info');
                var formValidate= obj.parent().siblings('.error-height');
                return {
                    infoWrap:wrapper.find('.info-wrap'),
                    errorWrap:formValidate.find('.error'),
                    successWrap:formValidate.find('.success')
                }
            }
        })();
    });
})