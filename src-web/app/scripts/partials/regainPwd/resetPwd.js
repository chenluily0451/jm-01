require(['jquery','common','jquery-validation'],function($){
    $(function(){

        (function(){
            jQuery.validator.addMethod("pwdPattern", function (value, element) {
                var val = $.trim(value);
                if( val && val.length>=6 && val.length<=16 && (!/[\u4e00-\u9fa5]/.test(value)) ){
                    if( /^[a-zA-Z]+$/.test(val) || /^\d+$/.test(val) || /^[\[\]\{\}@#$%?_~!^&\*\(\)]{6,16}$/.test(val) ){
                        return false;
                    }else{
                        return  /^([\w\[\]\{\}@#$%?_~!^&\*\(\)]){6,16}$/.test(val)
                    }
                }else{
                    return false;
                }
            },'输入格式不正确');
            jQuery.validator.addMethod("equalPwd", function (value, element) {
                return !$('#password-error .error').length;
            },'输入格式不正确');
        })();

        (function(){
            var registerForm = $('#registerForm'),
                formValidate = null,
                yzm          = $('#yzm');
            formValidate = registerForm.validate({
                rules:{
                    password:{
                        required:true,
                        pwdPattern:true
                    },
                    confirmPassword:{
                        required:true,
                        pwdPattern:true,
                        equalTo:'#password',
                        equalPwd:true
                    }
                },
                messages:{
                    password:{
                        required:'<div class="error">请输入密码</div>',
                        pwdPattern:'<div class="error">6-16个字符，字母、数字、特殊符号;至少两种以上组合，字母区分大小写</div>'
                    },
                    confirmPassword:{
                        required:'<div class="error">请输入确认密码</div>',
                        pwdPattern:'<div class="error">6-16个字符，字母、数字、特殊符号；至少两种以上组合；字母区分大小写</div>',
                        equalTo:'<div class="error">两次密码不一致</div>',
                        equalPwd:'<div class="error">请输入正确的密码</div>'
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
                        $.ajax({
                            url:'/regainPwd/resetPwd',
                            method:'POST',
                            data:{
                                k:$.trim($('#userKey').val()),
                                plainPassword:$.trim($('#password').val()),
                                confirmPassword:$.trim($('#confirmPassword').val())
                            },
                            success:function(response){
                                if( response == 'success' ){
                                    window.location.href = '/regainPwd/resetPwdSuccess';
                                }else{
                                    if( response == 'passWordError' ){
                                        formValidate.showErrors({
                                            'confirmPassword':'<div class="error">两次密码不一致</div>'
                                        });
                                    }else if( response == 'prevPasswordEquals' ){
                                        formValidate.showErrors({
                                            'password':'<div class="error">不能设置为原始密码</div>'
                                        });
                                    }else{
                                        formValidate.showErrors({
                                            'password':'<div class="error">用户不存在</div>'
                                        });
                                    }
                                }
                            }
                        });
                    }else{
                        return false;
                    }
                    return false;
                }
            })
            $('#password,#confirmPassword').on('focus',function(){
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
                var formValidate = obj.parent().siblings('.error-height');
                return {
                    infoWrap:wrapper.find('.info-wrap'),
                    errorWrap:formValidate.find('.error'),
                    successWrap:formValidate.find('.success')
                }
            }
        })();
    });
})