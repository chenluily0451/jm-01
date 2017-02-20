require(['jquery','common','jquery-validation'],function($){
    $(function(){
        // 记录是否到2条变量
        var count = 0;
        // table 展示数据对象
        var globalData = [];
        $('#userName').val('');
        $('#tel').val('');
        $('#authCode').val('');
        function setSelVal(){
            if( count==1 ){
                if( globalData[0].role == 'SALESMAN' ){
                    $('#role').val('TREASURER').attr('disabled','disabled');
                }
                if( globalData[0].role == 'TREASURER' ){
                    $('#role').val('SALESMAN').attr('disabled','disabled');
                }
            }
            if( count==0 ){
                $('#role').removeAttr('disabled').val('');
            }
        }
        (function(){
            count = $('#staffTable tr').length-1;
            if( count>=2 ){
                $('#addStaffBtn').addClass('disabled');
            }
            $('#staffTable tr:not(:first)').each(function(index,item){
                globalData[index] = {};
                globalData[index].userName = $(this).find('td:eq(0)').html();
                globalData[index].securePhone = $(this).find('td:eq(1)').html();
                globalData[index].role = $(this).find('td:eq(2)').html()=='业务人员'?'SALESMAN':'TREASURER';
            });
            setSelVal();
        })();

        (function(){
            $('#addStaffBtn').on('click',function(){
                $('#addStaff').modal('show');
            });
        })();

        var formValidate = null;
        (function(){
            jQuery.validator.addMethod("nameCheck", function (value, element) {
                var val = $.trim(value);
                return /^[\w\u4e00-\u9fa5]{2,10}$/.test(val);
            },'输入格式不正确');
            var addStaffForm = $('#addStaffForm'),
                yzm          = $('#yzm');
            var staffTable = $('#staffTable');
            formValidate = addStaffForm.validate({
                rules:{
                    userName:{
                        required:true,
                        nameCheck:true
                    },
                    role:{
                        required:true,
                        minlength:1
                    },
                    tel:{
                        required:true,
                        telPattern:true
                    },
                    authCode:{
                        required:true,
                        digits:true,
                        maxlength:6,
                        minlength:6
                    }
                },
                messages:{
                    userName:{
                        required:'<div class="error">请填写姓名</div>',
                        nameCheck:'<div class="error">请填写2-10个字符</div>'
                    },
                    role:{
                        required:'<div class="error">请选择角色</div>',
                        minlength:'<div class="error">请选择角色</div>'
                    },
                    tel:{
                        required:'<div class="error">请输入手机号</div>',
                        telPattern:'<div class="error">请输入符合规范的手机号码</div>'
                    },
                    authCode:{
                        required:'<div class="error">请输入手机验证码</div>',
                        digits:'<div class="error">请输入6位数字</div>',
                        maxlength:'<div class="error">请输入6位数字</div>',
                        minlength:'<div class="error">请输入6位数字</div>'
                    }
                },
                focusInvalid:false,
                errorElement:'div',
                errorClass:'error-wrap',
                errorPlacement:function(error,element){
                    $(element).parent().siblings('.error-height').append(error);
                    if( $(element).attr('id') == 'authCode' ){
                        $(element).parent().addClass('error-status')
                    }
                },
                success:function(label){
                    label.html('<div class="success"></div>');
                    if( label.attr('id') == 'authCode-error' ){
                        label.parents('.no-padding-left').find('.input-group').removeClass('error-status');
                    }
                },
                onsubmit:true,
                submitHandler:function(form){
                    var flag = formValidate.valid();
                    var submitData = {
                        userName:$.trim($('#userName').val()),
                        role:$.trim($('#role').val()),
                        securePhone:$.trim($('#tel').val()),
                        authCode:$.trim($('#authCode').val())
                    };
                    if( flag ){
                        $.ajax({
                            url:'/account/staffManage/addEmployee',
                            method:'POST',
                            data:submitData,
                            success:function(response){
                                if( response.success ){
                                    // $('#addStaff').modal('hide');
                                    // $('#userName').val('');
                                    // $('#role').val('');
                                    // $('#tel').val('');
                                    // $('#authCode').val('');
                                    // yzm.addClass('disabled');
                                    window.location.reload();
                                }else{
                                    if( response.error == 'securePhoneIllegal' ){
                                        formValidate.showErrors({
                                            'tel':'<div class="error">手机格式不正确</div>'
                                        });
                                    }else if( response.error == 'userExisted' ){
                                        formValidate.showErrors({
                                            'tel':'<div class="error">手机号已注册</div>'
                                        });
                                    }else if( response.error == 'Auth_CodeError' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">验证码错误</div>'
                                        });
                                        $('#authCode').parent().addClass('error-status');
                                    }else if( response.error == 'Auth_CodeExpire' ){
                                        formValidate.showErrors({
                                            'authCode':'<div class="error">验证码已过期</div>'
                                        });
                                        $('#authCode').parent().addClass('error-status');
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
            var yzm = $('#yzm');
            var authCode = $('#authCode');
            yzm.getYZM(beforeFn);
            function beforeFn(){
                var flag = null;
                $.ajax({
                    url:'/account/staffManage/sendAddEmployeeAuthCode',
                    method:'GET',
                    async:false,
                    success:function(response){
                        if(response.error=='sendAuthCode_highRate'){
                            authCode.trigger('input');
                            authCode.parent().addClass('error-status');
                            if( $('#authCode-error').length ){
                                $('#authCode-error').html('<div class="error">发送频率过高, 稍后再试!</div>');
                                $('#authCode-other').hide();
                            }else{
                                $('#authCode-other').html('<div class="error">发送频率过高, 稍后再试!</div>').show();
                            }
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            authCode.trigger('input');
                            authCode.parent().addClass('error-status');
                            if( $('#authCode-error').length ){
                                $('#authCode-error').html('<div class="error">发送验证码条数今日已达上限,请明日再试!</div>');
                                $('#authCode-other').hide();
                            }else{
                                $('#authCode-other').html('<div class="error">发送验证码条数今日已达上限,请明日再试!</div>').show();
                            }
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
        })();

        (function(){
            var index = 0;
            $(document).on('click','.delete',function(){
                index = $(this).attr('href').substr('1')*1;
                $('#modal_1').modal('show');
                return false;
            });
            $('#md_ok_1').on('click',function(){
                count--;
                setSelVal();
                $('#addStaffBtn').removeClass('disabled');
                $.ajax({
                    url:'/account/staffManage/forbiddenEmployee',
                    method:'POST',
                    data:{
                        userId:$('#staffTable a').eq(index).attr('data-index')
                    },
                    success:function(response){
                        $('#modal_1').modal('hide');
                        window.location.reload();
                    }
                });
            });
            $('#addStaffTitleClose').on('click',function(){
                formValidate.resetForm();
                $('#yzm').addClass('disabled');
                $('#userName,#tel,#authCode').val('');
                if( !$('#role').is(':disabled') ){
                    $('#role').val('');
                }
                $('#addStaff .error-status').removeClass('error-status');
                setTimeout(function(){
                    $('#yzm').removeClass('disabled');
                },1500);
            });
        })();
    });
})