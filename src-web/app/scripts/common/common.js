define(['jquery','bootstrap-sass','jquery-validation','lazyload'],function($){

    $(function () {
        (function(){
            if (!Array.prototype.indexOf){
                // ie8 新增 indexOf 方法
                Array.prototype.indexOf = function(elt){
                    var len = this.length >>> 0;
                    var from = Number(arguments[1]) || 0;
                    from = (from < 0)
                        ? Math.ceil(from)
                        : Math.floor(from);
                    if (from < 0)
                        from += len;
                    for (; from < len; from++)
                    {
                        if (from in this &&
                            this[from] === elt)
                            return from;
                    }
                    return -1;
                };
            }
        })();

        (function(){
            // ajax 请求设置
            $.ajaxSetup({
                global: true,
                // dataType: "json",
                beforeSend: function (jqXHR, settings) {
                    jqXHR.$setting = settings;
                },
                error: function (jqXHR, textStatus, exception) {
                    var XHRstatus = jqXHR.status;
                    var errorStatus = [400, 403, 404, 500,503];
                    if (XHRstatus == 401) {
                        if(JSON.parse(jqXHR.responseText).error=="userUnauthorized"){
                            $("#login-btn").attr("data-modal",true);
                        }
                        if(JSON.parse(jqXHR.responseText).error=="adminUnauthorized"){
                            $("#frontInfo").hide();
                        }
                        $("#modal-log-error").modal("show");
                    } else if (XHRstatus == 409) {
                        var message = JSON.parse(jqXHR.responseText).error;
                        bgError("",message,"noBtn");
                    } else if (XHRstatus == 403) {
                        var message = JSON.parse(jqXHR.responseText).error;
                        if(message=="waitImprove"){
                           bgError("","抱歉,您公司信息还未完善,请先完善之后再使用!");
                        }else if(message=="checkPending"){
                            bgError("","抱歉,您公司信息正在审核,请耐心等待!");
                        }else if(message=="unApproved"){
                            bgError("","抱歉,您公司信息审核未通过,请核对之后再次审请!");
                        }else {
                            bgError("","抱歉,您还没有相关权限！","noBtn");
                        }

                    } else if ($.inArray(XHRstatus, errorStatus) >= 0) {
                        showServerError(XHRstatus, "");
                    }
                }
            });
            var showServerError = function (status, message) {
                var errorMsg = {
                    401: "请重新登录...",
                    403: message ||"您没有权限访问该网页...",
                    404: "您访问的网页不存在...",
                    400: "参数传入错误，请稍后重试...",
                    500: "服务器出错，请稍后重试...",
                    503: "银行网络异常，请稍后重试...",
                    409: message || '服务器正忙，请稍后重试!'
                };
                $("#server-error-msg").text(errorMsg[status]);
                $('#modal-server-error').modal('show');
                $("#serviceErrorConfirm").off("click").on("click",function(){
                    $(".modal").modal("hide");
                });
            };
            function bgError(btnHref,popTxt,ifBtn){
                $("#modal-server-error").modal("show");
                $("#server-error-msg").html(popTxt);
                if(ifBtn!="noBtn" || ifBtn==undefined){
                    $("#serviceErrorConfirm").off("click").on("click",function(){
                        location.href=btnHref;
                    });
                }else{
                    $("#serviceErrorConfirm").off("click").on("click",function(){
                        $(".modal").modal("hide");
                    });

                }

            }
        })();

        (function(){
            // 全局 loading 效果
            var loading = $('#loading'),
                mask = $('#mask');
            var startTime = 0,endTime = 0;
            $(document).ajaxStart(function () {
                startTime = new Date().getTime();
            });
            $(document).ajaxStop(function() {
                endTime = new Date().getTime() - startTime;
                if( endTime - startTime < 500){
                    return false;
                }else{
                    mask.show();
                    loading.show();
                }
                var timer = null;
                timer = setTimeout(function () {
                    mask.hide();
                    loading.hide();
                    clearTimeout(timer);
                }, 600);
            });
        })();

        (function(){
            // 倒计时功能
            $.fn.deadLine = function (options) {
                var defaults = {
                    time: '2017/08/04 00:00:00'
                };
                var options = options ? options : defaults;
                this.timer = null;
                var _self = this;
                if( $('#serverDataTime').length == 0 ){
                    throw new Error('id:serverDataTime can not find');
                }
                var curTime = null;
                var curTimeTmp = null;
                this.calTime = function () {
                    if( !curTimeTmp ){
                        curTimeTmp = new Date(options.time).getTime();
                    }else{
                        curTimeTmp = curTimeTmp-1000;
                    }
                    curTime = (curTimeTmp - (new Date( $('#serverDataTime').val() ).getTime()) ) / 1000;
                    var str = '';
                    if ( curTime > 0) {
                        var second = Math.floor(curTime % 60);
                        var minute = Math.floor((curTime / 60) % 60);
                        var hour = Math.floor((curTime / 3600) % 24);
                        var day = Math.floor((curTime / 3600) / 24);
                        if( !options.showSeconds ){
                            str = formatTime(day) + '天' + formatTime(hour) + '时' + formatTime(minute) + '分';
                        }else{
                            str = formatTime(day) + '天' + formatTime(hour) + '时' + formatTime(minute) + '分' + formatTime(second) + '秒';
                        }
                        _self.html(str);
                    } else {
                        if( !options.showSeconds){
                            _self.html('<span class="text-warning">00</span> 天 <span class="text-warning">00</span> 时 <span class="text-warning">00</span> 分');
                        }else{
                            _self.html('<span class="text-warning">00</span> 天 <span class="text-warning">00</span> 时 <span class="text-warning">00</span> 分 <span class="text-warning">00</span> 秒');
                        }
                        _self.hide();
                        clearInterval(_self.timer);
                    }
                };
                this.timer = setInterval(_self.calTime, 1000);
                function formatTime(time) {
                    if (time < 10) {
                        return '<span class="text-warning">0' + time + '</span>';
                    } else {
                        return '<span class="text-warning">' + time + '</span>';
                    }
                }
            };
        })();

        (function(){
            // 全局去除 表单 自动提示功能
            $('input:not([autocomplete]),textarea:not([autocomplete]),select:not([autocomplete])').attr('autocomplete', 'off');
            $('[data-toggle=tooltip]').each(function(){
                if( $(this).html() ){
                    $(this).tooltip();
                }
            });
        })();

        (function(){
            // 手机号码中间添加*号
            $.fn.formatTel = function(){
                var val = null;
                if( !this.length ){ return this }
                if( this[0].tagName.toLowerCase() =='input'){
                    val = this.val();
                    if( val && !/^\s*$/.test(val) ){
                        this.val( val.replace(/(\d{3})\d{4}(\d{4})/,'$1****$2') );
                    }
                }else{
                    val = this.html();
                    if( val && !/^\s*$/.test(val) ){
                        this.html( val.replace(/(\d{3})\d{4}(\d{4})/,'$1****$2') );
                    }
                }
            };
            $.fn.priceToLarger = function(val){
                // 金钱转大写
                this.html( switchTxt( val ) );
                function switchTxt(n) {
                    if (!/^(0|[1-9]\d*)(\.\d+)?$/.test(n))
                        return "数据非法";
                    if(n==0){
                        return  "零元整" ;
                    }
                    var unit = "千百拾亿千百拾万千百拾元角分", str = "";
                    n += "00";
                    var p = n.indexOf('.');
                    if (p >= 0)
                        n = n.substring(0, p) + n.substr(p+1, 2);
                    unit = unit.substr(unit.length - n.length);
                    for (var i=0; i < n.length; i++){
                        str += '零壹贰叁肆伍陆柒捌玖'.charAt(n.charAt(i)) + unit.charAt(i);
                    }
                    return str.replace(/零(千|百|拾|角)/g, "零").replace(/(零)+/g, "零").replace(/零(万|亿|元)/g, "$1").replace(/(亿)万|壹(拾)/g, "$1$2").replace(/^元零?|零分/g, "").replace(/元$/g, "元整");
                }
            };
            if( jQuery.validator ){
                jQuery.validator.addMethod("telPattern", function (value, element) {
                    if( $.trim(value) ){
                        return /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/.test($.trim(value));
                    }else{
                        return false;
                    }
                },'输入格式不正确');
            }
        })();

        (function(){
            // 分页跳转到第几页input校验
            var pTxt = $('#J_ym-pagination-input');
            if( !pTxt.length ){
                return false;
            }
            var oldVal = null;
            pTxt.on('keydown',function(){
                oldVal = $(this).val();
            });
            pTxt.on('keyup',function(event){
                var keyCode = event.keyCode;
                if( keyCode == 190 ){
                    $(this).val( oldVal );
                }
                if( (keyCode<48 || keyCode>57) && keyCode != 8 ){
                    $(this).val('1');
                }
                if( keyCode == 13 ){
                    $(this).val( $(this).val().replace(/[^\d]/g,'') );
                }
            });
            pTxt.on('paste',function(){
                return false;
            });
        })();

        (function(){
            $.fn.getYZM = function(beforeFn,endFn,tel){
                return this.each(function(){
                    var $this = $(this);
                    $this.maxTime = 120;
                    $this.runMaxTime = $this.maxTime;
                    $this.info = ['重新获取','重新发送'];
                    $this.spaceTime = 1000;
                    $this.active = false;
                    var _self = $this;
                    $this.on('click',function(){
                        var $curThis = $(this);
                        if( _self.active || $curThis.hasClass('disabled') ){ return false; }
                        if( beforeFn ){
                            if( !beforeFn(tel?tel:'') ){
                                return false;
                            }
                        }
                        _self.active = true;
                        $curThis.html( '<span class="txt-warning">'+_self.runMaxTime+'</span>s后'+_self.info[1]);
                        $curThis.addClass('active');
                        _self.runMaxTime--;
                        _self.timer = setInterval(function(){
                            if( _self.runMaxTime >= 0 && !$curThis.hasClass('disabled') ){
                                $curThis.html('<span class="txt-warning">'+_self.runMaxTime+'</span>s后'+_self.info[1]);
                                _self.runMaxTime--;
                            }else{
                                $curThis.html( _self.info[0] );
                                $curThis.removeClass('active');
                                if( endFn ){
                                    endFn(tel?tel:'');
                                }
                                _self.active = false;
                                _self.runMaxTime = _self.maxTime;
                                clearInterval(_self.timer);
                            }
                        },_self.spaceTime);
                    });
                });
            };
        })();

        $(function(){
            function errorMsg(val,err){
                var partten = /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/,
                    v=val.val(),
                    id=val.attr("id")=="user-name" ? true : false;

                if(val.val() ==''){
                    val.parent().next(".errorMsg")
                        .css({visibility:'visible'})
                        .children("i").text(err);

                    val.addClass("err");

                    return false;
                }else if(val !="" && !partten.test(v) && id){
                    val.parent().next(".errorMsg")
                        .css({visibility:'visible'})
                        .children("i").text("请输入正确格式的账户名");

                    val.addClass("err");

                    return false;
                }
                else{
                    val.parent().next(".errorMsg")
                        .css({visibility:'hidden'})
                        .children("i").text(err);

                    val.removeClass("err");
                    return true;
                }
            }
            $('.input-wrap input').on("blur",function(){
                errorMsg($("#user-name"), "请输入账户名");
                errorMsg($("#user-pwd"), "请输入登录密码");
            });

            //登录
            $('#login-btn').on("click", function () {
                var $logBtn=$(this);
                var frontValidate = errorMsg($("#user-name"), "请输入账户名") &
                    errorMsg($("#user-pwd"), "请输入登录密码");
                if(!!frontValidate){
                    var ajaxUrl=document.location.href.indexOf("admin")>-1 ? '/admin/login' : '/login',
                        jumpUrl='/redirectOriginPage';
                    $.ajax({
                        url:ajaxUrl,
                        type:'POST',
                        data:{
                            userName:$("#user-name").val(),
                            plainPassword:$("#user-pwd").val()
                        },
                        success: function(response){
                            if(response=="userNotExists"){
                                $(".errorMsg").eq(0).css({visibility:'visible'}).children("i").text("账户名不存在，请重新输入");
                                $("#user-name").addClass("err");
                            }else if(response=="passwordError"){
                                $(".errorMsg").eq(1).css({visibility:'visible'}).children("i").text("账户名与密码不匹配，请重新输入");
                                $("#user-pwd").addClass("err");
                            }else if(response=="accountLocked"){
                                $(".errorMsg").eq(0).css({visibility:'visible'}).children("i").text("账户名已被锁定!");
                                $("#user-name").addClass("err");
                            } else if(response=="success"){
                                $("#login-btn").text("登录中...");
                                if($logBtn.parents("#modal-log-error").length){
                                    $("#modal-log-error").modal("hide");
                                }else{
                                    location.href=jumpUrl;
                                }
                            }
                        }
                    });

                }else{
                    $(".err").eq(0).focus();
                }
            });

            //回车事件绑定
            $('.input-wrap input').bind('keydown', function(event) {
                if (event.keyCode == "13") {
                    $('#login-btn').css({
                        opacity:'.8'
                    });
                }
            });
            $('.input-wrap input').bind('keyup', function(event) {
                if (event.keyCode == "13") {
                    $('#login-btn').click();
                    $('#login-btn').css({
                        opacity:'1'
                    });
                }
            });

        });

        // 日期设置
        $(function(){
            var startDate = $('#startTime'),
                endDate = $('#endTime');
            startDate.on('focus',function(){
                endDate.val("");
                WdatePicker({
                    firstDayOfWeek:0,
                    dateFmt:'yyyy-MM-dd',
                    position:{left:0,top:1},
                    autoPickDate:true
                });
            });
            endDate.on('focus',function() {
                WdatePicker({
                    firstDayOfWeek: 0,
                    dateFmt: 'yyyy-MM-dd',
                    position: {left: 0, top: 1},
                    minDate: '#F{$dp.$D(\'startTime\',{d:0});}',
                    autoPickDate: true
                });
            });
        });
    });
});
