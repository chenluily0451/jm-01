require(['jquery','bootstrap-sass','common','jquery-validation'],function($){
    $(function(){
        // 认证时金融输入错误次数最多3次
        var times = 0;
        jQuery.validator.addMethod("bankNameCheck", function (value, element) {
            var val = $.trim(value);
            return  !/^([\[\]\{\}@#$%?_~!^&\*\(\)])$/.test(val)
        },'输入格式不正确');
        jQuery.validator.addMethod("cardCheck", function (value, element) {
            var val = $.trim(value);
            return  /^\d+(?:\s\d+)+$/.test(val)
        },'输入格式不正确');

        var formValidate = null;
        (function(){
            var cardForm = $('#cardForm'),
                yzm      = $('#yzm');
            formValidate = cardForm.validate({
                rules:{
                    bankName:{
                        required:true,
                        bankNameCheck:true
                    },
                    accountName:{
                        required:true,
                        cardCheck:true
                    },
                    yzm:{
                        required:true,
                        maxlength:6,
                        minlength:6
                    }
                },
                messages:{
                    bankName:{
                        required:'<div class="error">请填写开户行</div>',
                        bankNameCheck:'<div class="error">开户行不能特殊字符</div>'
                    },
                    accountName:{
                        required:'<div class="error">请填写账户号</div>',
                        cardCheck:'<div class="error">账户号格式为xxxx xxx xxx</div>'
                    },
                    yzm:{
                        required:'<div class="error">请输入手机验证码</div>',
                        maxlength:'<div class="error">请输入6位图形验证码</div>',
                        minlength:'<div class="error">请输入6位图形验证码</div>'
                    }
                },
                focusInvalid:false,
                errorElement:'div',
                errorClass:'error-wrap',
                errorPlacement:function(error,element){
                    $(element).parent().siblings('.error-height').append(error);
                    if( $(element).attr('id') == 'yzm' ){
                        $(element).parent().addClass('error-status')
                    }
                },
                success:function(label){
                    label.html('<div class="success"></div>');
                    if( label.attr('id') == 'yzm-error' ){
                        label.parent().removeClass('error-status');
                    }
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
                                // if( response == 'success' ){
                                //     window.location.href = '/admin/cardBind';
                                // }else{
                                //     if( response == 'Auth_PhoneIllegal' ){
                                //         formValidate.showErrors({
                                //             'authCode':'手机号格式不正确'
                                //         });
                                //     }else if( response == 'Auth_CodeError' ){
                                //         formValidate.showErrors({
                                //             'authCode':'手机验证码错误'
                                //         });
                                //     }else if( response == 'Auth_CodeExpire' ){
                                //         formValidate.showErrors({
                                //             'authCode':'手机验证码已过期'
                                //         });
                                //     }else if( response == 'imgCodeError' ){
                                //         formValidate.showErrors({
                                //             'imgCode':'图形验证码错误'
                                //         });
                                //         $('#imgCode').val('');
                                //     }else if( response == 'securePhoneExists' ){
                                //         formValidate.showErrors({
                                //             'authCode':'手机号已经存在'
                                //         });
                                //     }
                                // }
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
                var flag = false;
                var yzm = $('#yzm');
                var errorArr = ['发送频率过高, 稍后再试!','发送验证码条数今天已经达到上限,请明日再试!'];
                if( errorArr.indexOf($('#yzm-error .error').html())>=0 ){
                    return false;
                }
                $.ajax({
                    url:'/account/sendTransferAccountAuthCode',
                    method:'GET',
                    async:false,
                    success:function(response){
                        if(response.error=='sendAuthCode_highRate'){
                            yzm.parent().addClass('error-status');
                            if( $('#yzm-error').length ){
                                $('#yzm-error').html('<div class="error">发送频率过高, 稍后再试!</div>').show();
                                $('#yzm-other').hide();
                            }else{
                                $('#yzm-other').html('<div class="error">发送频率过高, 稍后再试!</div>').show();
                            }
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            yzm.parent().addClass('error-status');
                            if( $('#yzm-error').length ){
                                $('#yzm-error').html('<div class="error">发送验证码条数今天已经达到上限,请明日再试!</div>').show();
                                $('#yzm-other').hide();
                            }else{
                                $('#yzm-other').html('<div class="error">发送验证码条数今天已经达到上限,请明日再试!</div>').show();
                            }
                            flag = false;
                        }else{
                            flag = true;
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
        })();

        $('#edit').on('click',function(){
            $('#modal').modal('show');
        });

        $('#identity').on('click',function(){
            $('#modal_1').modal('show');
        });

        $('#checkMoney').on('keyup',function(){
            if( !/(^[1-9]\d+(\.\d{0,2})?$)|(^0(\.\d{0,2})$)/.test($.trim($(this).val())) ){
                $(this).val('');
            }
        });
        //刷新余额
        $("#refresh").on("click",function(){
            $.ajax({

            })
        })
    });
});