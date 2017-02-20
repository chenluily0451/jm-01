require(['jquery','bootstrap-sass','common'],function($){
    $(function(){
        // status = 0 修改登录密码弹窗
        // status = 1 修改手机号码弹窗
        // status = 2 修改支付密码弹窗
        var status = 0;
        var globalKey = null;
        var urlGetYzm = [
            '/account/accountSetting/sendResetPwdAuthCode',
            '/account/accountSetting/sendOldPhoneAuthCode',
            '/account/accountSetting/sendResetPayPwdAuthCode'
        ];
        (function(){
            $('#modal_2 .modal-body div:first').addClass('pad-t20');
            $('#modal_2 .modal-body .attention').addClass('yes');
            $('#modalInfo_2').html('保存成功');
            $('#modal_2 .close').hide();
        })();

        (function(){
            // 图片验证码
            var verifycode = $('#verifycode,#telVerifycode'),
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
            function urlRequestHandler(){
                // 密码重置第一步
                var flag = false;
                var imgCode = $('#imgCode');
                var yzm = $('#yzm');
                var errorArr = ['发送频率过高, 稍后再试!','发送验证码条数今日已达上限,请明日再试!'];
                if( imgCode.parent().hasClass('error-status') || ($('#yzmError').is(':visible') && errorArr.indexOf($('#yzmError').html())>=0)){
                    return false;
                }
                if( $.trim(imgCode.val()).length==6 ){
                    $.ajax({
                        url:'/account/accountSetting/verifyImgCode',
                        method:'POST',
                        async:false,
                        data:{
                            imgCode:$.trim(imgCode.val())
                        },
                        success:function(response){
                            if( !response.success ){
                                imgCode.trigger('input');
                                imgCode.parent().addClass('error-status');
                                $('#imgCodeError').html('图形验证码输入有误,请重新输入').show();
                                flag = false;
                            }else{
                                $('#yzmBtn').removeClass('disabled');
                                flag = ajaxHandler()
                            }
                        }
                    })
                }else{
                    return false;
                }
                function ajaxHandler(){
                    var flag = false;
                    $.ajax({
                        url: urlGetYzm[status]+'?imgCode='+$.trim($('#imgCode').val()),
                        method: 'GET',
                        async:false,
                        success: function (response) {
                            if(response.error=='sendAuthCode_highRate'){
                                yzm.parent().addClass('error-status');
                                $('#yzmError').html('发送频率过高, 稍后再试!').show();
                                flag = false;
                            }else if(response.error=='sendAuthCode_limitation'){
                                yzm.parent().addClass('error-status');
                                $('#yzmError').html('发送验证码条数今日已达上限,请明日再试!').show();
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
                return flag;
            }
            function changeTel(){
                // 修改电话号码
                var flag = false;
                var telImgCode = $('#telImgCode');
                if( !/^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/.test( $.trim($('#newTel').val()) ) || !($.trim($('#telImgCode').val()).length==6) ){
                    flag = false;
                }else{
                    $.ajax({
                        url:'/account/accountSetting/verifyImgCode',
                        method:'POST',
                        async:false,
                        data:{
                            imgCode:$.trim($('#telImgCode').val())
                        },
                        success:function(response){
                            if( !response.success ){
                                $('#telImgCode').parent().addClass('error-status');
                                $('#telVerifycodeError').html('图形验证码输入有误,请重新输入').show();
                                flag = false;
                            }else{
                                $('#yzmBtn').removeClass('disabled');
                                ajaxHandler()
                            }
                        }
                    })
                }
                function ajaxHandler(){
                    $.ajax({
                        url:'/account/accountSetting/sendNewPhoneAuthCode',
                        method: 'GET',
                        async:false,
                        data:{
                            newPhone:$.trim($('#newTel').val()),
                            imgCode:$.trim($("#telImgCode").val()),
                            k:globalKey
                        },
                        success: function (response) {
                            if(response.success){
                                flag = true;
                                globalKey=response.data;
                                $('#newTel').removeClass('info-border');
                                $('#newTel').removeClass('error-border');
                                $('#newTel').siblings('.right-icon').show();
                                $('#newTel').siblings('.info-border').hide();
                                $('#newTel').siblings('.error-border').hide();
                            }else{
                                flag = false;
                                $('#newTel').removeClass('info-border').addClass('error-border');
                                $('#newTel').siblings('.right-icon').hide();
                                $('#newTel').siblings('.info-border').hide();
                                $('#newTelError').html('手机号已注册').show();
                            }
                        }
                    });
                }
                return flag;
            }
            function getStaffCode(){
                var flag = false;
                var staffCode = $('#staffCode');
                var errorArr = ['发送频率过高, 稍后再试!','发送验证码条数今日已达上限,请明日再试!'];
                if( errorArr.is(':visible') && errorArr.indexOf($('#staffCode-error .error').html())>=0 ){
                    return false;
                }
                $.ajax({
                    url:'/account/sendTransferAccountAuthCode',
                    method:'GET',
                    async:false,
                    success:function(response){
                        if(response.error=='sendAuthCode_highRate'){
                            staffCode.parent().addClass('error-status');
                            if( $('#staffCode-error').length ){
                                $('#staffCode-error').html('<div class="error">发送频率过高, 稍后再试!</div>').show();
                                $('#staffCode-other').hide();
                            }else{
                                $('#staffCode-other').html('<div class="error">发送频率过高, 稍后再试!</div>').show();
                            }
                            flag = false;
                        }else if(response.error=='sendAuthCode_limitation'){
                            staffCode.parent().addClass('error-status');
                            if( $('#staffCode-error').length ){
                                $('#staffCode-error').html('<div class="error">发送验证码条数今日已达上限,请明日再试!</div>').show();
                                $('#staffCode-other').hide();
                            }else{
                                $('#staffCode-other').html('<div class="error">发送验证码条数今日已达上限,请明日再试!</div>').show();
                            }
                            flag = false;
                        }else{
                            flag = true;
                        }
                    }
                });
                return flag;
            }
            $('#yzmBtn').getYZM(urlRequestHandler);
            $('#telYzmBtn').getYZM(changeTel);
            $('#yzmStaff').getYZM(getStaffCode);
        })();

        (function(){
            // 弹窗
            var textArr    = ['密码重置','更换手机号码','支付密码重置'];
            var resetPwd   = $('#resetPwd'),
                resetPay   = $('#resetPay'),
                replaceTel = $('#replaceTel'),
                modalTitle = $('#modalTitle');
            var editTelForm= $('#editTelForm'),
                editPwdForm= $('#editPwdForm'),
                editNewTelForm = $('#editNewTelForm');
            resetPwd.on('click',function(){
                status = 0;
                beforeModalHandler(status);
                $('#modalEdit').modal('show');
            });
            resetPay.on('click',function(){
                status = 2;
                beforeModalHandler(status);
                $('#modalEdit').modal('show');
            });
            replaceTel.on('click',function(){
                status = 1;
                beforeModalHandler(status);
                $('#modalEdit').modal('show');
            });
            $('#modalEditClose').on('click',function(){
                resetModalHandler();
            });
            function beforeModalHandler(status){
                $('#verifycode').trigger('click');
                editTelForm.show();
                editPwdForm.hide();
                editNewTelForm.hide();
                modalTitle.html(textArr[status]);
                if( !status ){
                    $('#txtName').html('原登录密码');
                }else if( status == 2){
                    $('#txtName').html('原支付密码');
                }
            }
            function resetModalHandler(){
                $('#modalEdit input').removeClass('error-border info-border').val('');
                $('#modalEdit .right-icon').hide()
                $('#modalEdit .txt-warning-wrap').hide();
                $('#modalEdit .input-group').removeClass('error-status');
                $('#newPwd').attr('disabled','disabled');
                $('#confirmPwd').attr('disabled','disabled');
                $('#originPwd').removeAttr('disabled');
                $('#yzmBtn,#telYzmBtn').addClass('disabled');
                $('#yzmBtn,#telYzmBtn').html('获取验证码');
            }
        })();

        (function(){
            // 密码校验
            var yzm        = $('#yzm'),
                inputGroup = $('#yzmBtn').parent(),
                txtWarning = inputGroup.next(),
                imgCode    = $('#imgCode'),
                imgCodeInputGroup = $('#imgCode').parent(),
                imgCodeError=$('#imgCodeError'),
                stepToNext = $('#stepToNext');
            var editTelForm= $('#editTelForm'),
                editPwdForm= $('#editPwdForm'),
                editNewTelForm = $('#editNewTelForm');
            var yzmErr = [
                '请输入手机验证码',
                '手机验证码输入有误,请重新输入',
                '请输入图形验证码',
                '图形验证码输入有误,请重新输入',
            ];
            var requestUrl = [
                '/account/accountSetting/verifyResetPwdAuthCode',
                '/account/accountSetting/verifyOldPhoneAuthCode',
                '/account/accountSetting/verifyResetPayPwdAuthCode'
            ];
            yzm.on('input',function(){
                if( !$.trim(yzm.val()) || $.trim(yzm.val()).length!=6 ){
                    inputGroup.addClass('error-status');
                    txtWarning.html(yzmErr[0]).show();
                }else{
                    inputGroup.removeClass('error-status');
                    txtWarning.hide();
                }
            });
            imgCode.on('input',function(){
                if( !$.trim(imgCode.val()) || $.trim(imgCode.val()).length!=6 ){
                    imgCodeInputGroup.addClass('error-status');
                    imgCodeError.html(yzmErr[2]).show();
                    $('#yzmBtn').addClass('disabled');
                }else{
                    imgCodeInputGroup.removeClass('error-status');
                    imgCodeError.hide();
                    $('#yzmBtn').removeClass('disabled');
                }
            });
            stepToNext.on('click',function(){
                if( $.trim(imgCode.val()).length == 6 && $.trim(yzm.val()).length==6 ){
                    // 修改密码点击下一步
                    $.ajax({
                        url:requestUrl[status],
                        method:'POST',
                        data:{
                            authCode:$.trim(yzm.val()),
                            imgCode:$.trim(imgCode.val())
                        },
                        dataType:'json',
                        async:false,
                        success:function(response){
                            if( response.success ){
                                globalKey = response.data;
                                if( !status || status == 2){
                                    // status = 0 修改登录密码
                                    // status = 2 修改支付密码
                                    editTelForm.hide();
                                    editPwdForm.show();
                                    editNewTelForm.hide();
                                }else{
                                    editTelForm.hide();
                                    editPwdForm.hide();
                                    editNewTelForm.show();
                                    $('#telVerifycode').trigger('click');
                                }
                            }else{
                                if( response.error == 'imgCodeError' ){
                                    imgCode.parent().addClass('error-status');
                                    $('#imgCodeError').html('图形验证码出错').show();
                                }else if( response.error == 'Auth_CodeError' || response.error == 'Auth_CodeExpire' ){
                                    yzm.parent().addClass('error-status');
                                    $('#yzmError').html(response.error == 'Auth_CodeError'?'手机验证码不正确':'手机验证码已过期').show();
                                }
                            }
                        }
                    });
                }else{
                    yzm.trigger('input');
                    imgCode.trigger('input');
                }
            });
        })();

        function checkPwdHandler(val){
            // 密码验证是否符合规则
            if( val && val.length>=6 && val.length<=16 && (!/[\u4e00-\u9fa5]/.test(val)) ){
                if( /^[a-zA-Z]+$/.test(val) || /^\d+$/.test(val) || /^[\[\]\{\}@#$%?_~!^&\*\(\)]{6,16}$/.test(val) ){
                    return false;
                }else{
                    return  /^([\w\[\]\{\}@#$%?_~!^&\*\(\)]){6,16}$/.test(val)
                }
            }else{
                return false;
            }
        }

        (function(){
            // 新密码或新手机号修改
            var originPwd = $('#originPwd'),
                newPwd    = $('#newPwd'),
                confirmPwd= $('#confirmPwd'),
                pwdSubmit = $('#pwdSubmit'),
                newTel    = $('#newTel'),
                telYzm    = $('#telYzm'),
                telYzmBtnError = $('#telYzmBtnError');
            var telImgCode    = $('#telImgCode'),
                telImgCodeInputGroup = $('#telImgCode').parent(),
                telImgCodeError=$('#telImgCodeError');
            var requestUrl = ['/account/accountSetting/resetPwd',null,'/account/accountSetting/resetPayPwd'];
            var checkPwdEquals= ['/account/accountSetting/verifyOriginPwdEquals',null,'/account/accountSetting/verifyOriginPayPwdEquals'];
            originPwd.on('input',function(){
                var _self = $(this);
                if( $(this).attr('disabled') == 'disabled' ){
                    return false;
                }
                var val = $(this).val();
                if( checkPwdHandler(val) ){
                    // 修改(密码或支付密码)的时候判断是否和原密码相同
                    $.ajax({
                        url:checkPwdEquals[status],
                        data:{
                            pwd:_self.val()
                        },
                        method:'POST',
                        async:false,
                        success:function(response){
                            if( response ){
                                _self.attr('disabled','disabled');
                                _self.removeClass('error-border');
                                $('#originPwdError').hide();
                                newPwd.removeAttr('disabled');
                                confirmPwd.removeAttr('disabled');
                            }else{
                                _self.addClass('error-border');
                                $('#originPwdError').html('原密码错误').show();
                            }
                        }
                    });
                }else{
                    newPwd.attr('disabled','disabled');
                    confirmPwd.attr('disabled','disabled');
                    _self.addClass('error-border');
                    $('#originPwdError').html('原密码错误').show();
                }
            });
            newPwd.on('focus',function(){
                if( !$(this).siblings('.right-icon').is(':visible') ){
                    $(this).siblings('.info-border').show();
                    $(this).removeClass('error-border').addClass('info-border');
                    $(this).siblings('.right-icon').hide();
                    $('#newPwdError').hide();
                }
            }).on('input',function(){
                var rightIcon = $(this).siblings('.right-icon'),
                    infoBorder = $(this).siblings('.info-border'),
                    val = $.trim($(this).val());
                infoBorder.hide();
                $(this).removeClass('info-border');
                if( checkPwdHandler(val) ){
                    if( val == $.trim(originPwd.val()) ){
                        $(this).addClass('error-border');
                        $('#newPwdError').html('不能为原登录密码').show();
                        rightIcon.hide();
                        $(this).attr('data-flag','0');
                    }else{
                        $(this).removeClass('error-border');
                        $('#newPwdError').hide();
                        rightIcon.show();
                        $(this).attr('data-flag','1');
                    }
                }else{
                    rightIcon.hide();
                    $(this).addClass('error-border');
                    if( !$.trim( $(this).val() ) ){
                        $('#newPwdError').html('新密码不能为空').show();
                    }else{
                        $('#newPwdError').html('密码格式不正确').show();
                    }
                    $(this).attr('data-flag','0');
                }
            });
            confirmPwd.on('input',function(){
                var rightIcon = $(this).siblings('.right-icon'),
                    infoBorder = $(this).siblings('.info-border'),
                    val = $.trim($(this).val());
                infoBorder.hide();
                if( checkPwdHandler(val) && val==$.trim(newPwd.val()) ){
                    rightIcon.show();
                    $(this).removeClass('error-border');
                    $('#confirmPwdError').hide();
                    $(this).attr('data-flag','1');
                }else{
                    rightIcon.hide();
                    $(this).addClass('error-border');
                    if( !$.trim(val) ){
                        $('#confirmPwdError').html('确认密码不能为空').show();
                    }else{
                        $('#confirmPwdError').html('两次密码不一致').show();
                    }
                    $(this).attr('data-flag','0');
                }
            });
            pwdSubmit.on('click',function(){
                if( originPwd.attr('disabled') != 'disabled' ){
                    originPwd.addClass('error-border');
                    originPwd.siblings('.right-icon').hide();
                    $('#originPwdError').html('原密码错误').show();
                    return false;
                }
                newPwd.trigger('input');
                confirmPwd.trigger('input');
                if( !(newPwd.attr('data-flag')*1) ){
                    return false;
                }
                if( !(confirmPwd.attr('data-flag')*1) ){
                    return false;
                }
                // 登录密码重置或支付密码重置请求
                $.ajax({
                    url:requestUrl[status],
                    method:'POST',
                    data:{
                        k:globalKey,
                        oldPwd:originPwd.val(),
                        newPwd:newPwd.val(),
                    },
                    success:function(response){
                        if( response.success ){
                            // 成功关闭弹窗
                            $('#modalEdit').modal('hide');
                            $('#modalInfo_2').html(['登录密码',null,'支付密码'][status]+'修改成功');
                            $('#modal_2').modal('show');
                            setTimeout(function(){
                                $('#modal_2').modal('hide');
                                resetModalHandler();
                            },3000);
                        }else if( response.error == 'prevPwdEquals' ){
                            //修改登录或支付密码错误
                            newPwd.addClass('error-border').siblings('.right-icon').hide();
                            confirmPwd.addClass('error-border').siblings('.right-icon').hide();
                            $('#newPwdError').html('不能为原登录密码').show();
                            newPwd.val(oldPwd);
                        }else if( response.error == 'oldPwdError'){
                            originPwd.removeAttr('disabled').addClass('error-border');
                            $('#originPwdError').html('原登录密码错误').show();
                            $('#newPwd').attr('disabled','disabled').val('').siblings('.right-icon').hide();
                            $('#confirmPwd').attr('disabled','disabled').val('').siblings('.right-icon').hide();
                        }
                    }
                });
            });
            newTel.on('focus',function(){
                if( !$(this).siblings('.right-icon').is(':visible') ){
                    $(this).siblings('.info-border').show();
                    $(this).removeClass('error-border').addClass('info-border');
                    $(this).siblings('.right-icon').hide();
                    $('#newTelError').hide();
                }
            }).on('input',function(){
                var rightIcon = $(this).siblings('.right-icon'),
                    infoBorder = $(this).siblings('.info-border'),
                    val = $.trim($(this).val());
                infoBorder.hide();
                $(this).removeClass('info-border');
                if( /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/.test(val) ){
                    $(this).removeClass('error-border');
                    rightIcon.show();
                    $('#newTelError').hide();
                    $(this).attr('data-flag','1');
                }else{
                    rightIcon.hide();
                    $(this).addClass('error-border');
                    if( !$.trim( $(this).val() ) ){
                        $('#newTelError').html('新手机号码不能为空').show();
                    }else{
                        $('#newTelError').html('请输入正确的手机号码').show();
                    }
                    $(this).attr('data-flag','0');
                }
            });
            telImgCode.on('input',function(){
                if( !$.trim(telImgCode.val()) || $.trim(telImgCode.val()).length!=6 ){
                    telImgCodeInputGroup.addClass('error-status');
                    $('#telVerifycodeError').html('请输入图形验证码').show();
                    $('#telYzmBtn').addClass('disabled');
                    $(this).attr('data-flag','0');
                }else{
                    telImgCodeInputGroup.removeClass('error-status');
                    $('#telVerifycodeError').hide();
                    $('#telYzmBtn').removeClass('disabled');
                    $(this).attr('data-flag','1');
                }
            });
            telYzm.on('input',function(){
                if( !$.trim(telYzm.val()) || $.trim(telYzm.val()).length!=6 ){
                    $(this).parent().addClass('error-status');
                    $('#telYzmError').html('请输入6位数字手机验证码').show();
                    $(this).attr('data-flag','0');
                }else{
                    $(this).parent().removeClass('error-status');
                    $('#telYzmError').hide();
                    $(this).attr('data-flag','1');
                }
            });
            $('#telChangeHandler').on('click',function(){
                newTel.trigger('input');
                telYzm.trigger('input');
                telImgCode.trigger('input');
                if( !(newTel.attr('data-flag')*1) ){
                    return false;
                }
                if( !(telImgCode.attr('data-flag')*1) ){
                    return false;
                }
                if( !(telYzm.attr('data-flag')*1) ){
                    return false;
                }
                $.ajax({
                    url:'/checkSecurePhoneExists',
                    method:'POST',
                    data:{
                        securePhone:$.trim($('#newTel').val())
                    },
                    success:function(response){
                        if(!response){
                            newTelHandler();
                        }else{
                            newTel.addClass('error-border').siblings('.right-icon').hide();
                            $('#newTelError').html('手机号码已注册').show();
                        }
                    }
                });
                function newTelHandler(){
                    $.ajax({
                        url:'/account/accountSetting/verifyNewPhoneAuthCode',
                        method:'POST',
                        data:{
                            newPhone:$.trim(newTel.val()),
                            k:globalKey,
                            authCode:$.trim($('#telYzm').val()),
                            imgCode:$.trim($('#telImgCode').val())
                        },
                        success:function(response){
                            if( response.success){
                                // 成功关闭弹窗
                                $('#modalEdit').modal('hide');
                                $('#modalInfo_2').html('手机号码修改成功,请重新登录');
                                $('#modal_2').modal('show');
                                setTimeout(function(){
                                    $('#modal_2').modal('hide');
                                    window.location.href = '/login';
                                },2000);
                            }else{
                                //修改手机号码错误
                                if( response.error == 'Auth_PhoneIllegal' ){
                                    newTel.addClass('error-border').siblings('.right-icon').hide();
                                    $('#newTelError').html('请输入符合规范的手机号码').show();
                                }else if( response.error == 'Auth_CodeError' || response.error == 'Auth_CodeExpire'){
                                    telYzm.parent().addClass('error-status').siblings('.right-icon').hide();
                                    $('#telYzmError').html(response.error =='Auth_CodeError'?'手机验证码输入有误,请重新输入':'验证码已过期').show();
                                }else if( response.error == 'imgCodeError'){
                                    telImgCode.parent().addClass('error-status').siblings('.right-icon').hide();
                                    $('#telVerifycodeError').html('请输入符合规范的手机号码').show();
                                }
                            }
                        }
                    });
                }
            });
            function resetModalHandler(){
                $('#modalEdit input').removeClass('error-border info-border').val('');
                $('#modalEdit .right-icon').hide()
                $('#modalEdit .txt-warning-wrap').hide();
                $('#modalEdit .input-group').removeClass('error-status');
                $('#newPwd').attr('disabled','disabled');
                $('#confirmPwd').attr('disabled','disabled');
                $('#originPwd').removeAttr('disabled');
                $('#yzmBtn,#telYzmBtn').addClass('disabled');
                $('#yzmBtn,#telYzmBtn').html('获取验证码');
            }
        })();
    });
});