require(['jquery','bootstrap-sass','datePicker','common','select2','select2-cn','ueconfig','ueall','jquery-validation'],function($){
    $(function(){
        var releaseNotice={
            "init" : function(){
                this.editInit();
                this.selectInit();
                this.validate();
                $("input").on("blur",function(){
                    $(this).val($.trim($(this).val()));
                })
            },
            "selectInit" : function(){
                $("#projectName").select2({
                    minimumResultsForSearch: Infinity,
                    placeholder: "请选择项目名称",
                    language: "zh-CN"
                });
                $("#projectName").on("select2:select",function(){
                    var thVal=$(this).val(),
                        status=$('#projectName option:checked').attr('data-status');
                    $("#projectNum").val(thVal);
                    if(status==4){
                        $("#noticeType").text("流标公示");
                    }
                    if(status==5){
                        $("#noticeType").text("中标公示");
                    }
                });


            },
            "editInit" : function(){
                var saveInner,contentLen;
                ue = UE.getEditor('notice-inner', {
                    toolbars: [
                        ['fontsize','underline','inserttitle','forecolor','bold','indent','justifyleft','justifycenter','justifyright','justifyjustify','inserttable','backcolor']
                    ],
                    serverUrl:'',
                    autoHeightEnabled: false,
                    autoFloatEnabled: true,
                    enableAutoSave:false,
                    initialFrameWidth:953,
                    initialFrameHeight:432,
                    fontsize:[12,16,24,30,45,60],
                    maximumWords:1000,
                    maxUndoCount:5,
                    range:1,
                    saveInterval: (60*60*24),
                    enableContextMenu:false,
                    allHtmlEnabled:true,
                    autoSyncData:false,
                    elementPathEnabled:false,
                    initialContent:$("#contentStr1").text()
                });
                ue.on("keydown",function(){
                    contentLen = ue.getContentLength(true);
                    if(contentLen<=1000){
                        saveInner=ue.getContent();
                    }
                });
                ue.on("keyup",function(){
                    contentLen = ue.getContentLength(true);
                    if(contentLen>1000){
                        ue.setContent(saveInner);
                    }
                });
            },
            "validate" : function(){
                var btnJudge;
                $(".btn-control").on("click",function(){
                    btnJudge=$(this).attr("id")=="releaseBtn" ? true : false;
                });
                jQuery.validator.addMethod("mobile", function (value, element) {
                    return this.optional(element) || /^(0[0-9]{2,3}\-)?([2-9][0-9]{6,7})+(\-[0-9]{1,4})?$|(^(13[0-9]|15[0|2|3|6|7|8|9]|18[0-9])\d{8}$)/.test(value);
                }, "请输入正确格式的手机号/电话号码");

                $("#validateWrap").validate({
                    onsubmit: true,
                    rules: {
                        projectName:{
                            required:true
                        },
                        noticeName:{
                            required:true,
                            maxlength:50
                        },
                        noticeStartDate:{
                            required:true
                        },
                        noticeEndDate:{
                            required:true
                        },
                        servicePhone:{
                            required:true,
                            mobile:true
                        },
                        serviceDepartment:{
                            required:true
                        }
                    },
                    messages: {
                        projectName: {
                            required:"项目名称不能为空!"
                        },
                        noticeName: {
                            required:"请输入公示名称!",
                            maxlength:"公示名称最多50个字!"
                        },
                        noticeStartDate:{
                            required:"请输入公示起始日期！"
                        },
                        noticeEndDate:{
                            required:"请输入公示结束日期！"
                        },
                        servicePhone:{
                            required:"请输入正确格式的手机号/电话号码",
                            mobile:"请输入正确格式的手机号/电话号码"
                        },
                        serviceDepartment:{
                            required:"请输入监督部门！",
                            maxlength:"监督部门最多50个字！"
                        }
                    },
                    errorPlacement:function(error,element) {
                        error.appendTo($(".errorWrap"));

                    },
                    submitHandler:function() {
                        if(btnJudge){
                            var ueLen=ue.getContentLength(true);
                            if(ueLen==0){
                                $(".errorWrap1").text("公示内容不能为空！");
                                return false;
                            }
                            if(ueLen>1000){
                                $(".errorWrap1").text("公示内容不能超过1000字！");
                                return false;
                            }
                        }

                        $("#contentStr").val(ue.getContent());
                        $("#tenderId").val($('#projectName option:checked').attr('data-id'));
                        var ajaxUrl=btnJudge==true ? '/admin/notice/releaseTenderNotice' : '/admin/notice/setNoticePreview';
                                $.ajax({
                                    url:ajaxUrl,
                                    type:'POST',
                                    data:{
                                        "tenderId":$("input[name='tenderId']").val(),
                                        "noticeName":$("input[name='noticeName']").val(),
                                        "contentStr":$("input[name='contentStr']").val(),
                                        'noticeStartDate':$("input[name='noticeStartDate']").val(),
                                        'noticeEndDate':$("input[name='noticeEndDate']").val(),
                                        'servicePhone':$("input[name='servicePhone']").val(),
                                        'serviceDepartment':$("input[name='serviceDepartment']").val(),
                                        'tenderCode':$("#projectNum").val(),
                                        'hasWinBid':$("#projectName option:selected").attr("data-status")==4?1:0
                                    },
                            success:function(res){
                                if(res.success){
                                    if(!btnJudge){
                                        location.href='/admin/notice/openNoticePreview';
                                        return false;
                                    }
                                    $("#modal_0").modal("show");
                                    $(".bg_img").addClass("yes");
                                    $("#jumpBtn").on("click",function(){
                                        $(".errorWrap1").text("");
                                        location.href='/admin/notice';
                                    });
                                }
                            }

                        });

                    }

                });
                $("#projectName").on("change",function(){$(this).valid()})
            }
        };
        releaseNotice.init();
    });
});