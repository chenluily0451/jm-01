require(['jquery','bootstrap-sass','common'],function($){
    $(function(){
        $('#md_ok_1').on('click',function(){
            $.ajax({
                url: '',
                method: 'POST',
                data: {
                    id:'11'
                },
                success:function(response){

                }
            })
        });
        //刷新
        function kyamount(){
            // 获取资金账户余额请求
            var cashCount = $('#numWrap');
            var price = null;
            cashCount.html('<img src="/images/common/numLoading.gif" alt=""/>');
            $.ajax({
                url:'/admin/finance/asset/queryAccountBalance',
                method:'GET',
                success:function(response){
                    if( response.success ){
                        price = formatPrice(response.kyAmount);
                        if( typeof price == 'string' ){
                            cashCount.html(price);
                        }
                    }
                },
                error : function(){
                    $("#numWrap").html("--");
                }
            });
            function formatPrice(val) {
                var n = Math.abs(parseFloat(val)).toFixed(2);
                return n.replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
            }
        };

        kyamount();
        $(".refresh").click(kyamount);

        (function(){
            var flag = true;
            $.ajax({
                url:'/admin/finance/asset/hasValidCard',
                method:'GET',
                async:false,
                success:function(response){
                    flag = response.succsss;
                }
            })
            $('#withDrawBtn').on('click',function(){
                if(!flag){
                    $('#modal_2').modal('show');
                    return false;
                }
            });
            $('#confirmLink').on('click',function(){
                window.location.href = '/admin/finance/asset';
            });
        })();
    });
});