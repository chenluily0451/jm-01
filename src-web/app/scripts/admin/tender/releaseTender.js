require(['jquery','bootstrap-sass','common','select2','select2-cn','ueconfig','ueall','jquery-validation'],function($){

    $(document).ready(function(){
        var indicators,qualification,marginsCon,tenderRules;
        var releaseTender={
            "selectInit" : function(){
                $("#coalTypeId").select2({
                    minimumResultsForSearch: Infinity,
                    placeholder: "请选择煤炭品种",
                    language: "zh-CN"
                });
                $("#coalZoneId").select2({
                    minimumResultsForSearch: Infinity,
                    placeholder: "请选择矿别",
                    language: "zh-CN"
                });
                $("#forwardStationId").select2({
                    minimumResultsForSearch: Infinity,
                    placeholder: "请选择始发站",
                    language: "zh-CN"
                });
                $("#transportModeId").select2({
                    minimumResultsForSearch: Infinity,
                    placeholder: "请选择运输方式",
                    language: "zh-CN"
                });
                $("#PS").select2({
                    minimumResultsForSearch: Infinity,
                    placeholder: "请选择颗粒度",
                    language: "zh-CN"
                });
            },
            "date" : function(){
                // 日期设置
                var startDate = $('#startTime'),
                    endDate = $('#endTime');
                startDate.off();
                endDate.off();
                startDate.on('focus',function(){
                    endDate.val("");
                    WdatePicker({
                        firstDayOfWeek:0,
                        dateFmt:'yyyy-MM-dd HH:mm',
                        position:{left:0,top:1},
                        minDate:'%y-%M-%d',
                        autoPickDate:true
                    });
                });
                endDate.on('focus',function() {
                    WdatePicker({
                        firstDayOfWeek: 0,
                        dateFmt: 'yyyy-MM-dd HH:mm',
                        position: {left: 0, top: 1},
                        minDate: '#F{$dp.$D(\'startTime\',{d:0});}',
                        autoPickDate: true
                    });
                });
            },
            "editInit" : function(){
                var $KPIStr=$(".KPIStr"),
                    $qualificationStr=$(".qualificationStr"),
                    $marginsStr=$(".marginsStr"),
                    $saleRuleStr=$(".saleRuleStr");

                indicators = UE.getEditor('indicators', {
                    toolbars: [],
                    serverUrl:'',
                    initialFrameWidth:940,
                    initialFrameHeight:110,
                    maximumWords:1000,
                    saveInterval: (60*60*24),
                    enableContextMenu:false,
                    autoSyncData:false,
                    elementPathEnabled:false,
                    wordCount:true,
                    initialContent:$KPIStr.children().length<1 ? '质量和数量的验收标准及方法，数量和质量均以卖方计量和检验结果为准；验收方执行《GB/T18666-2002商品煤质量抽查与验收方法》和《煤炭送货办法》以及《煤炭送货办法实施细则》的有关规定；如果存在数量质量异议，双方协商解决；协商不成的，以双方确认同意的第三方化验机构化验结果为准（费用由与第三方化验结果偏差较大者承担）。发运数量以车或列为单位组织，合同兑现数量误差不超过10%。' : $KPIStr.html()
                });


                qualification = UE.getEditor('qualification', {
                    toolbars: [],
                    serverUrl:'',
                    initialFrameWidth:940,
                    initialFrameHeight:70,
                    maximumWords:1000,
                    saveInterval: (60*60*24),
                    enableContextMenu:false,
                    autoSyncData:false,
                    elementPathEnabled:false,
                    wordCount:true,
                    initialContent:$qualificationStr.children().length<1 ? '需竞买方提供营业执照和银行开户许可证，并在电商平台上传资质，经审核确认后才能进行提报竞价。竞价人委托他人代理的，应提交授权委托书及委托理人的身份证和复印件。' : $qualificationStr.html()
                });

                marginsCon = UE.getEditor('marginsCon', {
                    toolbars: [],
                    serverUrl:'',
                    initialFrameWidth:940,
                    initialFrameHeight:70,
                    maximumWords:1000,
                    saveInterval: (60*60*24),
                    enableContextMenu:false,
                    autoSyncData:false,
                    elementPathEnabled:false,
                    wordCount:true,
                    initialContent:$marginsStr.children().length<1 ? '为保证交易正常进行，竞买方需在报名截止时间前，一次性缴纳交易保证金，逾期未足额缴纳交易保证金的企业所提交的报价视为无效，缴纳方式为线下缴纳。' : $marginsStr.html()
                });
                tenderRules = UE.getEditor('tender-rules', {
                    toolbars: [],
                    serverUrl:'',
                    initialFrameWidth:940,
                    initialFrameHeight:110,
                    maximumWords:1000,
                    saveInterval: (60*60*24),
                    enableContextMenu:false,
                    autoSyncData:false,
                    elementPathEnabled:false,
                    wordCount:true,
                    initialContent:$saleRuleStr.children().length<1 ? ' 1、参与竞价的客户必须缴纳保证金，取得报价资格后才能进行报价,价格必须大于公布的底价，采购量不得低于所规定的最低竞买量； 2、单个客户每次报价时价格必须不低于当前所有报价最高价，采购量不低于本客户上次所提报数量； 3、竞买方可以按报价原则多次报价，系统采集最高报价（最后一次报价）为有效报价， 4、每次加价幅度为最小变动单位的整倍数， 5、实行公开竞价交易，在系统规定的时间内，所有竞买方在报价时，只可在电商平台查看底价、其他竞买方当前报价，其他信息无权查阅。 6、成交价按照“价格优先”的原则确定，若出现相同报价者，系统将按时间先后排序，先报价者成交，成家量按竞买方出价高低关联的竞拍量依次匹配' : $saleRuleStr.html()
                });
                var saveInner1="",saveInner2="",saveInner3="",saveInner4="";
                indicators.on("keydown",function(){
                    var contentLen = indicators.getContentLength(true);
                    if(contentLen<=1000){
                        saveInner1=indicators.getContent();
                    }
                });
                indicators.on("keyup",function(){
                    var contentLen = indicators.getContentLength(true);
                    if(contentLen>1000){
                        indicators.setContent(saveInner1);
                    }
                });
                qualification.on("keydown",function(){
                    var contentLen = qualification.getContentLength(true);
                    if(contentLen<=1000){
                        saveInner2=qualification.getContent();
                    }
                });
                qualification.on("keyup",function(){
                    var contentLen = qualification.getContentLength(true);
                    if(contentLen>1000){
                        qualification.setContent(saveInner2);
                    }
                });
                marginsCon.on("keydown",function(){
                    var contentLen = marginsCon.getContentLength(true);
                    if(contentLen<=1000){
                        saveInner3=marginsCon.getContent();
                    }
                });
                marginsCon.on("keyup",function(){
                    var contentLen = marginsCon.getContentLength(true);
                    if(contentLen>1000){
                        marginsCon.setContent(saveInner3);
                    }
                });
                tenderRules.on("keydown",function(){
                    var contentLen = tenderRules.getContentLength(true);
                    if(contentLen<=1000){
                        saveInner4=tenderRules.getContent();
                    }
                });
                tenderRules.on("keyup",function(){
                    var contentLen = tenderRules.getContentLength(true);
                    if(contentLen>1000){
                        tenderRules.setContent(saveInner4);
                    }
                });
            },
            "btnSelect": function(){
                $(".release-btn").on("click",function(){
                    $(".release-btn").removeClass("selected");
                   $(this).addClass("selected");
                });
            },
            "validate": function(){
                jQuery.validator.addMethod("oneDecimal", function (value, element) {
                    return this.optional(element) || /^(\d+(\.\d)?)$/.test(value);
                }, "只能输入一位小数");
                jQuery.validator.addMethod("twoDecimal", function (value, element) {
                    return this.optional(element) || (/^\d+(\.\d{0,2})?$/).test(value);
                }, "最多为两位小数");
                jQuery.validator.addMethod("chineseCheck", function (value, element) {
                    return /^[\u4E00-\u9FA5|\w|\d]{1,50}$/.test(value);
                }, "请输入中文");
                jQuery.validator.addMethod("compare", function (value, element) {
                    var thisEle=$(element).parents(".release-input-wrap").find("input").eq(0).val(),
                        otherEle=$(element).parents(".release-input-wrap").find("input").eq(1).val();
                    if(thisEle && otherEle){
                        return thisEle*1 <= otherEle*1
                    }else if((!thisEle && otherEle) || (thisEle && !otherEle)){
                        return false;
                    }
                    else{
                        return true;
                    }


                }, "请输入正确的区间");
                jQuery.validator.addMethod("amountCompare", function (value, element) {
                    var $minimumSaleAmount=$(element).val()*1,
                        $saleAmount=$("#saleAmount").val()*1;
                    if($saleAmount !=''){
                        return $minimumSaleAmount <= $saleAmount;
                    }else{
                        return true;
                    }
                }, "最低竞买量不得超过总吨数");
                jQuery.validator.addMethod("thousandInt", function (value, element) {
                    var thisVal=$(element).val()*1;
                    if(thisVal % 1000!==0){
                        return false;
                    }else{
                        return true;
                    }
                }, "请输入1000的倍数");

                $("#formTender").validate({
                    onsubmit: true,
                    rules: {
                        tenderTitle:{
                            required:true,
                            maxlength:50
                        },
                        coalTypeId:{
                            required:true
                        },
                        coalZoneId:{
                            required:true
                        },
                        forwardStationId:{
                            required:true
                        },
                        transportModeId:{
                            required:true
                        },
                        saleAmount:{
                            required:true,
                            range:[1,999999],
                            digits:true,
                            thousandInt:true
                        },
                        margins:{
                            required:true,
                            range:[0.01,99],
                            twoDecimal:true
                        },
                        finalPrice:{
                            required:true,
                            chineseCheck:true,
                            maxlength:10
                        },
                        saleBasePrice:{
                            required:true,
                            range:[0.01,1500],
                            twoDecimal:true
                        },
                        saleIncreasePriceStep:{
                            required:true,
                            range:[1,100],
                            digits:true
                        },
                        minimumSaleAmount:{
                            required:true,
                            range:[1,999999],
                            digits:true,
                            amountCompare:true,
                            thousandInt:true
                        },
                        settlementModeStr:{
                            required:true,
                            maxlength:80
                        },
                        tenderStartDate:{
                            required:true
                        },
                        tenderEndDate:{
                            required:true
                        },
                        NCV:{
                            required:true,
                            digits: true,
                            range: [1, 7500],
                            compare:true
                        },
                        NCV2:{
                            required:true,
                            digits: true,
                            compare:true,
                            range: [1, 7500]
                        },
                        RS:{
                            range: [0.01, 10],
                            twoDecimal:true,
                            compare:true
                        },
                        RS2:{
                            range: [0.01, 10],
                            twoDecimal:true,
                            compare:true
                        },
                        ADV:{
                            range: [0.01, 50],
                            twoDecimal:true,
                            compare:true
                        },
                        ADV2:{
                            range: [0.01, 50],
                            twoDecimal:true,
                            compare:true
                        },
                        TM:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        TM2:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        ASH:{
                            range: [0.01,50],
                            oneDecimal:true,
                            compare:true
                        },
                        ASH2:{
                            range: [0.01,50],
                            oneDecimal:true,
                            compare:true
                        },
                        ADS:{
                            range: [0.01,10],
                            twoDecimal:true,
                            compare:true
                        },
                        ADS2:{
                            range: [0.01,10],
                            twoDecimal:true,
                            compare:true
                        },
                        RV:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        RV2:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        IM:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        IM2:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        YV:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        YV2:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        GV:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        GV2:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        FC:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        FC2:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        CRC:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        CRC2:{
                            digits: true,
                            range: [1, 100],
                            compare:true
                        },
                        HGI:{
                            digits: true,
                            range: [1, 99],
                            compare:true
                        },
                        HGI2:{
                            digits: true,
                            range: [1, 99],
                            compare:true
                        },
                        VDAF:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        VDAF2:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        STD:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        STD2:{
                            range: [0.01,50],
                            twoDecimal:true,
                            compare:true
                        },
                        AFT:{
                            range: [900,1800],
                            digits: true,
                            compare:true
                        },
                        AFT2:{
                            range: [900,1800],
                            digits: true,
                            compare:true
                        }
                    },
                    messages: {
                        tenderTitle: {
                            required:"请输入公告名称",
                            maxlength:"公告名称最多50个字"
                        },
                        coalTypeId: {
                            required:"请选择煤炭品种"
                        },
                        coalZoneId: {
                            required:"请选择矿别"
                        },
                        forwardStationId:{
                            required:"请选择发站"
                        },
                        transportModeId:{
                            required:"请选择运输方式"
                        },
                        saleAmount:{
                            required:"请输入吨数",
                            range:"吨数为1-999999之间的整数值",
                            digits:"吨数必须为整数值",
                            thousandInt:"吨数必须为1000的整数倍"
                        },
                        margins:{
                            required:"请输入保证金",
                            range:"保证金为0-99之间的值",
                            twoDecimal:"保证金最多为两位小数"
                        },
                        finalPrice:{
                            required:"请输入成交价格",
                            chineseCheck:"成交价格必须为中文",
                            maxlength:"成交价格最多为10个字"
                        },
                        saleBasePrice:{
                            required:"请输入竞买底价",
                            range:"竞买底价为0-1500元/吨",
                            twoDecimal:"竞买底价最多为两位小数"
                        },
                        saleIncreasePriceStep:{
                            required:"请输入加价幅度",
                            range:"加价幅度为1-100元/吨",
                            digits:"加价幅度只能为整数"

                        },
                        minimumSaleAmount:{
                            required:"请输入最低竞买量",
                            range:"最低竞买量为1-999999之间的整数值",
                            digits:"最低竞买量必须为整数值",
                            thousandInt:"最低竞买量必须为1000的整数倍"
                        },
                        settlementModeStr:{
                            required:"请输入结算方式",
                            maxlength:"结算方式最多为80个字"
                        },
                        tenderStartDate:{
                            required:"请输入竞价开始时间"
                        },
                        tenderEndDate:{
                            required:"请输入竞价结束时间"
                        },
                        NCV:{
                            required:"请输入发热量",
                            digits: "发热量请输入整数",
                            range: "发热量为整数1-7500之间的整数!",
                            compare:"发热量请输入正确的区间"
                        },
                        NCV2:{
                            required:"请输入发热量",
                            digits: "发热量为请输入整数",
                            compare:"发热量请输入正确的区间",
                            range: "发热量为整数1-7500之间的整数!"
                        },
                        RS:{
                            range: "收到基硫分为0.01-10的值",
                            twoDecimal:"收到基硫分最多为两位小数",
                            compare:"收到基硫分请输入正确的区间"
                        },
                        RS2:{
                            range: "收到基硫分为0.01-10的值",
                            twoDecimal:"收到基硫分最多为两位小数",
                            compare:"收到基硫分请输入正确的区间"
                        },
                        ADV:{
                            range:"空干基挥发分为0.01-50的值 ",
                            twoDecimal:"空干基挥发分为最多为两位小数",
                            compare:"空干基挥发分为请输入正确的区间"
                        },
                        ADV2:{
                            range:"空干基挥发分为0.01-50的值",
                            twoDecimal:"空干基挥发分为最多为两位小数",
                            compare:"空干基挥发分为请输入正确的区间"
                        },
                        TM:{
                            range: "全水分为0.01-50的值",
                            twoDecimal:"全水分最多保留两位小数",
                            compare:"全水分请输入正确的区间"
                        },
                        TM2:{
                            range: "全水分为0.01-50的值",
                            twoDecimal:"全水分最多保留两位小数",
                            compare:"全水分请输入正确的区间"
                        },
                        ASH:{
                            range: "灰分为0.01-50的值",
                            oneDecimal:"灰分最多保留一位小数",
                            compare:"灰分请输入正确的区间"
                        },
                        ASH2:{
                            range: "灰分为0.01-50的值",
                            oneDecimal:"灰分最多保留一位小数",
                            compare:"灰分请输入正确的区间"
                        },
                        ADS:{
                            range:"空干基硫分为0.01-10之间的值",
                            twoDecimal:"空干基硫分最多为两位小数",
                            compare:"空干基硫分请输入正确的区间"
                        },
                        ADS2:{
                            range:"空干基硫分为0.01-10之间的值",
                            twoDecimal:"空干基硫分最多为两位小数",
                            compare:"空干基硫分请输入正确的区间"
                        },
                        RV:{
                            range:"收到基挥发分为0.01-50之间的值",
                            twoDecimal:"收到基挥发分最多为两位小数",
                            compare:"收到基挥发分请输入正确的区间"
                        },
                        RV2:{
                            range:"收到基挥发分为0.01-50之间的值",
                            twoDecimal:"收到基挥发分最多为两位小数",
                            compare:"收到基挥发分请输入正确的区间"
                        },
                        IM:{
                            range:"内水分为0.01-50之间的值",
                            twoDecimal:"内水分最多为两位小数",
                            compare:"内水分请输入正确的区间"
                        },
                        IM2:{
                            range:"内水分为0.01-50之间的值",
                            twoDecimal:"内水分最多为两位小数",
                            compare:"内水分请输入正确的区间"
                        },
                        YV:{
                            digits: "Y值为整数",
                            range: "Y值为1-100之间的数值",
                            compare:"Y值请输入正确的区间"
                        },
                        YV2:{
                            digits: "Y值为整数",
                            range: "Y值为1-100之间的数值",
                            compare:"Y值请输入正确的区间"
                        },
                        GV:{
                            digits: "G值为整数",
                            range: "G值为1-100之间的数值",
                            compare:"G值请输入正确的区间"
                        },
                        GV2:{
                            digits: "G值为整数",
                            range: "G值为1-100之间的数值",
                            compare:"G值请输入正确的区间"
                        },
                        FC:{
                            digits: "固定碳为整数",
                            range: "固定碳为1-100之间的数值",
                            compare:"固定碳请输入正确的区间"
                        },
                        FC2:{
                            digits: "固定碳为整数",
                            range: "固定碳为1-100之间的数值",
                            compare:"固定碳请输入正确的区间"
                        },
                        CRC:{
                            digits: "焦渣特征为整数",
                            range: "焦渣特征为1-100之间的数值",
                            compare:"焦渣特征请输入正确的区间"
                        },
                        CRC2:{
                            digits: "焦渣特征为整数",
                            range: "焦渣特征为1-100之间的数值",
                            compare:"焦渣特征请输入正确的区间"
                        },
                        VDAF:{
                            range:"干燥无灰基挥发分为0.01-50之间的值",
                            twoDecimal:"干燥无灰基挥发分最多为两位小数",
                            compare:"干燥无灰基挥发分请输入正确的区间"
                        },
                        VDAF2:{
                            range:"干燥无灰基挥发分为0.01-50之间的值",
                            twoDecimal:"干燥无灰基挥发分最多为两位小数",
                            compare:"干燥无灰基挥发分请输入正确的区间"
                        },
                        STD:{
                            range:"干燥基硫分为0.01-50之间的值",
                            twoDecimal:"干燥基硫分最多为两位小数",
                            compare:"干燥基硫分请输入正确的区间"
                        },
                        STD2:{
                            range:"干燥基硫分为0.01-50之间的值",
                            twoDecimal:"干燥基硫分最多为两位小数",
                            compare:"干燥基硫分请输入正确的区间"
                        },
                        HGI:{
                            digits: "哈氏可磨为整数",
                            range: "哈氏可磨为1-99之间的整数值",
                            compare:"哈氏可磨请输入正确的区间"
                        },
                        HGI2:{
                            digits: "哈氏可磨为整数",
                            range: "哈氏可磨为1-99之间的整数值",
                            compare:"哈氏可磨请输入正确的区间"
                        },
                        AFT:{
                            digits: "灰熔点为整数",
                            range: "灰熔点为900-1800之间的数值",
                            compare:"灰熔点请输入正确的区间"
                        },
                        AFT2:{
                            digits: "灰熔点为整数",
                            range: "灰熔点为900-1800之间的数值",
                            compare:"灰熔点请输入正确的区间"
                        }
                    },
                    groups:{
                        NCV:"NCV NCV2",
                        RS:"RS RS2",
                        ADV:"ADV ADV2",
                        TM:"TM TM2",
                        ASH:"ASH ASH2",
                        ADS:"ADS ADS2",
                        RV:"RV RV2",
                        IM:"IM IM2",
                        YV:"YV YV2",
                        GV:"GV GV2",
                        FC:"FC FC2",
                        CRC:"CRC CRC2",
                        VDAF:"VDAF VDAF2",
                        STD:"STD STD2",
                        HGI:"HGI HGI2",
                        AFT:"AFT AFT2"
                    },
                    errorPlacement:function(error,element) {
                        var parentIndex=$(element).parents(".release-tender").data("order"),
                            errorPlace1=$(".error-p1"),
                            errorPlace2=$(".error-p2");
                        if(parentIndex=="1") {
                            error.appendTo(errorPlace1);
                        }
                        else if(parentIndex=="2"){
                            error.appendTo(errorPlace2);
                        }

                    },
                    submitHandler:function() {
                        //四个富文本变量，获取字数,判断是否为空
                        var arrTxt=[indicators,qualification,marginsCon,tenderRules],
                            arrLen="";
                        for(var i=0;i<arrTxt.length;i++){
                            arrLen = arrTxt[i].getContentLength(true);
                            if(arrLen==0){
                                $(".errorWrap").eq(i).text("该字段不得为空");
                                return false;
                            }else{
                                $(".errorWrap").eq(i).text("")
                            }
                        }
                        $("#KPIStr").val(indicators.getContent());
                        $("#qualificationStr").val(qualification.getContent());
                        $("#marginsStr").val(marginsCon.getContent());
                        $("#saleRuleStr").val(tenderRules.getContent());

                        //数据集合
                        var coalBaseDTO={},
                            tenderData={"coalBaseDTO":coalBaseDTO},
                            $releaseBtn=$(".release-btn"),
                            btnJudge=$releaseBtn.eq(0).hasClass("selected") ? true : false,
                            tenderId=$("#tenderId"),
                            ajaxUrl;
                        if(tenderId.length){
                            ajaxUrl=btnJudge==true ? '/admin/tender/updateForRealRelease/'+tenderId.val() : '/admin/tender/updateForTempSave/'+tenderId.val();
                        }else{
                            ajaxUrl=btnJudge==true ? '/admin/tender/releaseTender' : '/admin/tender/tempSaveTender';
                        }


                        $(".release-input-wrap").each(function(){
                            var $this=$(this).parents(".release-tender");
                            $(this).find("input[type='text']").not(":disabled").each(function(){
                                if($this.data("order")=="2"){
                                    coalBaseDTO[$(this).attr('name')] = $(this).val();
                                }else{
                                    if($(this).attr('name')=='margins'){
                                        tenderData[$(this).attr('name')] = ($(this).val()*1*10000).toFixed(2);
                                    }else{
                                        tenderData[$(this).attr('name')] = $(this).val();
                                    }

                                }
                            });
                            $(this).find('select').each(function(){
                                if($this.data("order")=="2"){
                                    coalBaseDTO[$(this).attr('name')] = $(this).val();
                                }else{
                                    tenderData[$(this).attr('name')] = $(this).val();
                                }
                            });
                            $(this).find('textarea').each(function(){
                                tenderData[$(this).attr('name')] = $(this).val();
                            });

                        });
                        $.ajax({
                            url:ajaxUrl,
                            type:'POST',
                            contentType:"application/json;charSet=UTF-8",
                            data:JSON.stringify(tenderData),
                            success:function(){
                                if(btnJudge){
                                    $("#modalInfo_0").text("本次竞价发布成功！")

                                }else{
                                    $("#modalInfo_0").text("本次竞价已暂存在竞价记录！")
                                }
                                $("#modal_0").modal("show");
                                $("#modalImg_0").addClass("yes");
                                $(".close_modal").hide();
                                $("#jumpBtn").on("click",function(){
                                    location.href='/admin/tender/myTenderList';
                                });
                            }
                        });
                    }

                });
                $("#coalTypeId,#coalZoneId,#forwardStationId,#transportModeId").change(function(){
                    $(this).valid();
                });

                $(document).on("blur","input,textarea",function(){
                    var trimVal=$.trim($(this).val());
                    $(this).val(trimVal)
                });

                //运输方式联动
                $("#transportModeId").change(function(){
                    var $this=$(this).val();
                    if($this=="2"){
                        $(".deliveryMethod").val("卖方矿场车板交货，卖方代办铁路运输，运费、货物保险费等费用均由竞价方承担");
                    }
                    else if($this=="1"){
                        $(".deliveryMethod").val("卖方矿场交货，竞价方自行组织车辆拉运");
                    }

                });

            },

            "init" : function(){
                this.selectInit();
                this.date();
                this.editInit();
                this.validate();
                this.btnSelect();
            }

    };
    releaseTender.init();
    });
});