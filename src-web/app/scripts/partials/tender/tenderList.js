require(['jquery','bootstrap-sass','WdatePicker','common'],function($){
   $(function(){
       (function(){
           $(".searchList label").on("click",function(){
               var selectVal=$(this).data("key");
               $(this).siblings("input").val(selectVal);
               $(".myTenderListBlock").submit();
           });
       })();

       //删除撤销弹窗
       (function(){
           var thTenderId="";
            $(".delBtn").on("click",function(){
                $("#modal_0").modal("show");
                $("#modalDelBtn").attr("data-order",$(this).data("tenderid"));
            });
           $(".revokeBtn").on("click",function(){
               $("#modal_1").modal("show");
               $("#modalRevokedBtn").attr("data-order",$(this).data("tenderid"));
           });
           $("#modalDelBtn").on("click",function(){
               thTenderId=$(this).attr("data-order");
               $.ajax({
                   url:"/admin/tender/deleteTender/"+thTenderId,
                   type:'GET',
                   success:function(response){
                       if(response){
                           location.reload();
                       }
                   }
               })
           });
           $("#modalRevokedBtn").on("click",function(){
               thTenderId=$(this).attr("data-order");
               $.ajax({
                   url:"/admin/tender/backOut/"+thTenderId,
                   type:'GET',
                   success:function(response){
                       if(response){
                           location.reload();
                       }
                   }
               })
           });
       })();
   });
});