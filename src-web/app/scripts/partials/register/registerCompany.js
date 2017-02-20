require(['jquery','common','jquery-validation','jquery-form'],function($){
    $(function(){
        (function(){
            jQuery.validator.addMethod("number", function (value, element,params) {
                if( params[0] == 50) {
                    return /^[\[\]\{\}@#$%?_~!^&\*\(\)\（\）\w\u4E00-\u9FA5]{5,50}$/.test($.trim(value));
                }else if( params[0] == 60 ){
                    return /^[\[\]\{\}@#$%?_~!^&\*\(\)\w\u4E00-\u9FA5]{5,60}$/.test($.trim(value));
                }else if( params[0] == 30 ){
                    return /^[\[\]\{\}@#$%?_~!^&\*\(\)\w\u4E00-\u9FA5]{5,20}$/.test($.trim(value));
                }else if( params[0] == 20 ){
                    return /^[\[\]\{\}@#$%?_~!^&\*\(\)\w\u4E00-\u9FA5]{2,20}$/.test($.trim(value));
                }else{
                    return /^[\u4e00-\u9fa5\w]{1,6}$/.test($.trim(value));
                }
            },'输入格式不正确');
            jQuery.validator.addMethod("telPhone", function (value, element,params) {
                if( /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/.test($.trim(value))){
                    return true;
                }else if( /^((0\d{2,3})-)(\d{7,8})(-(\d{3,}))?$/.test($.trim(value))){
                    return true;
                }else{
                    return false;
                }
            },'输入格式不正确');
            jQuery.validator.addMethod("companyNameExists", function (value, element) {
                var flag = null;
                $.ajax({
                    global:false,
                    url:'/checkCompanyNameExists',
                    method:'POST',
                    async:false,
                    data:{
                        companyName:$.trim($('#name').val())
                    },
                    success:function(response){
                        if( response ){
                            flag = false;
                        }else{
                            flag = true;
                        }
                    }
                });
                return flag;
            },'输入格式不正确');
        })();

        (function(){
            var registerForm = $('#registerForm'),
                formValidate = null,
                registerBtn  = $('#registerBtn');
            var inputList    = $('#name,#registerAddress,#registerCode,#legalPerson,#proxyPerson,#companyPhone');
            formValidate = registerForm.validate({
                rules:{
                    name:{
                        required:true,
                        number:[50],
                        companyNameExists:true,
                    },
                    registerAddress:{
                        required:true,
                        number:[60]
                    },
                    registerCode:{
                        required:true,
                        number:[30]
                    },
                    legalPerson:{
                        required:true,
                        number:[20]
                    },
                    proxyPerson:{
                        required:true,
                        number:[20]
                    },
                    companyPhone:{
                        required:true,
                        telPhone:true
                    }
                },
                messages:{
                    name:{
                        required:'<div class="error">请输入公司名称</div>',
                        number:'<div class="error">请输入5-50个字符以内，文字，英文，数字，符号</div>',
                        companyNameExists:'<div class="error">公司名称已被注册</div>'
                    },
                    registerAddress:{
                        required:'<div class="error">请输入注册地址</div>',
                        number:'<div class="error">请输入5-60个字符以内，文字，英文，数字，符号</div>'
                    },
                    registerCode:{
                        required:'<div class="error">请输入注册号码</div>',
                        number:'<div class="error">请输入5-20个字符以内，文字，英文，符号</div>'
                    },
                    legalPerson:{
                        required:'<div class="error">请输入企业法人</div>',
                        number:'<div class="error">请输入2-20个字符以内，文字，英文</div>'
                    },
                    proxyPerson:{
                        required:'<div class="error">请输入委托人</div>',
                        number:'<div class="error">请输入2-20个字符以内，文字，英文</div>'
                    },
                    companyPhone:{
                        required:'<div class="error">请输入联系方式</div>',
                        telPhone:'<div class="error">请输入正确的联系方式</div>'
                    }
                },
                focusInvalid:false,
                errorElement:'div',
                errorClass:'error-wrap',
                errorPlacement:function(error,element){
                    $(element).siblings('.error-height').append(error);
                },
                success:function(label){
                    label.html('<div class="success"></div>');
                },
                onsubmit:true,
                submitHandler:function(form){
                    var flag = formValidate.valid();
                    var dataObj = {};
                    var keyName = [
                        'businessLicensePic',
                        'taxRegistrationPic',
                        'organizationCodePic',
                        'openingLicensePic',
                        'creditCodePic',
                        'proxyPic',
                        'idCardPic'
                    ];
                    if( flag ){
                        if( !uploadArr[6] ){
                            $('#idCardPicErr').show();
                            return false;
                        }else{
                            $('#idCardPicErr').hide();
                        }
                        dataObj = {
                            name:$.trim($('#name').val()),
                            registerAddress:$.trim($('#registerAddress').val()),
                            registerCode:$.trim($('#registerCode').val()),
                            legalPerson:$.trim($('#legalPerson').val()),
                            proxyPerson:$.trim($('#proxyPerson').val()),
                            companyPhone:$.trim($('#companyPhone').val())
                        };
                        if( uploadArr.length>0 ){
                            for( var i=0,len=uploadArr.length;i<len;i++ ){
                                if( uploadArr[i] ){
                                    dataObj[keyName[i]] = uploadArr[i];
                                }
                            }
                        }
                        $.ajax({
                            url:'/registerCompany',
                            method:'POST',
                            contentType:'application/json;charset=UTF-8',
                            data:JSON.stringify(dataObj),
                            success:function(response){
                                if( response == 'success' ){
                                    window.location.href = '/registerSuccess';
                                }else if( response == 'companyNameExists' ){
                                    formValidate.showErrors({
                                        'name':'公司名称已被注册'
                                    });
                                }
                            }
                        })
                    }else{
                        return false;
                    }
                    return false;
                }
            })
            inputList.on('focus',function(){
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
                var wrapper = obj.siblings('.form-info');
                var formValidate = obj.siblings('.error-height');
                return {
                    infoWrap:wrapper.find('.info-wrap'),
                    errorWrap:formValidate.find('.error'),
                    successWrap:formValidate.find('.success')
                }
            }
            registerBtn.on('click',function(){
                if( !uploadArr[6] ){
                    $('#imgCheck').show();
                }else{
                    $('#imgCheck').hide();
                }
                registerForm.submit();
            });
        })();

        var uploadArr = [];
        (function(){
            // 图片上传
            var uploadList  = $('#uploadList'),
                files       = $('#uploadList input[name=file],#identifyCard input[name=file]'),
                uploadError = $('#uploadError'),
                aDelete     = $('#uploadList .delete,#identifyCard .delete'),
                maxSize     = 5,
                fixedWidth  = 123,
                fixedHeight = 148;
            var errorTxt    = [
                '营业执照',
                '税务登记证',
                '组织机构代码证',
                '开户许可证',
                '信用代码证',
                '委托书',
                '身份证'
            ];
            var modalImg    = $('#modalImg'),
                modalImgModal = $('#modalImgModal');
            files.each(function(index,item){
                (function(index,_self){
                    var eqIndex = index;
                    _self.on('change',function(){
                        var form = $(this).parents('form');
                        var uploadImg = form.find('.upload-img'),
                            addBtn = form.find('.add-btn'),
                            operateBtn = form.find('.operate-btn'),
                            shadow = form.find('.shadow');
                        var curFile = form.find('input[type=file]');
                        form.ajaxSubmit({
                            beforeSubmit:function(data,form,options){
                                if(navigator.userAgent.indexOf('compatible')>-1){
                                    uploadError.hide();
                                }else{
                                    if( data && data[0] ){
                                        var size = data[0].value.size;
                                        if(size && size/1000/1024>maxSize){
                                            uploadError.html(errorTxt[eqIndex]+'不能超过'+maxSize+'M').show();
                                            return false;
                                        }else{
                                            uploadError.hide();
                                        }
                                    }
                                }
                            },
                            success:function(response){
                                var response = JSON.parse(response);
                                uploadImg.attr('src',response.filePath).load(function(){


                                    var width = $(this)[0].width;
                                    var height = $(this)[0].height;
                                    if( width/fixedWidth >= height/fixedHeight){
                                        $(this).attr('height',fixedHeight);
                                    }else{
                                        $(this).attr('width',fixedWidth);
                                    }
                                    addBtn.hide();
                                    operateBtn.show();
                                    shadow.show();
                                    uploadArr[eqIndex] = response.filePath;
                                    if( eqIndex == 6 ){
                                        $('#imgCheck').hide();
                                    }
                                    curFile.addClass('active');
                                    form.clearForm();
                                }).show();
                            }
                        });
                    });
                    _self.hover(function(){
                        var form = $(this).parents('form');
                        var operateBtn = form.find('.operate-btn');
                        operateBtn.find('a:first').addClass('active');
                    },function(){
                        var form = $(this).parents('form');
                        var operateBtn = form.find('.operate-btn');
                        operateBtn.find('a:first').removeClass('active');
                    });
                })(index,$(this));
            });
            var _selfDelete = null;
            aDelete.on('click',function(){
                $('#modalInfo_1').html('您确认删除《'+errorTxt[$(this).attr('data-index')*1]+'》吗?');
                $('#modal_1').modal('show');
                _selfDelete = $(this);
                return false;
            });
            $('#md_ok_1').on('click',function(){
                deleteCb(_selfDelete);
            });
            function deleteCb(_self){
                var index = _self.attr('data-index');
                var form = _self.parents('form');
                var uploadImg = form.find('.upload-img'),
                    addBtn = form.find('.add-btn'),
                    operateBtn = form.find('.operate-btn'),
                    shadow = form.find('.shadow'),
                    file = form.find('input[type=file]');
                uploadArr[index] = '';
                uploadImg.hide();
                addBtn.show();
                operateBtn.hide();
                shadow.hide();
                file.removeClass('active');
                $('#modal_1').modal('hide');
            }
            $('#uploadList,#identifyCard').on('click','.upload-img',function(){
                modalImg.attr('src',$(this).attr('src'));
                modalImgModal.modal('show');
            });
            $('#modalClose').click(function(){
                modalImgModal.modal('hide');
            });
            $('.operate-btn a:first').on('click',function(){
                $(this).parent().siblings('input').trigger('click');
            });
        })();
    });
})