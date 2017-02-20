require.config({
    baseUrl: '/scripts',
    shim: {
        'bootstrap-sass': {
            deps: [
                'jquery'
            ]
        },
        'common': {
            deps: [
                "jquery"
            ]
        },
        'datePicker': {
            deps: [
                'jquery'
            ]
        },
        'jquery-form':{
            deps: [
                'jquery'
            ]
        },
        'jquery-validation':{
            deps:[
                'jquery'
            ]
        },
        'lazyload':{
            deps:[
                'jquery'
            ]
        },
        'superslide':{
            deps:[
                'jquery'
            ]
        },
        'animateNum':{
            deps:[
                'jquery'
            ]
        },
        'select2-cn':{
            deps:[
                'jquery',
                'select2'
            ]
        },
        'mydate97':{
            deps:[
                'jquery'
            ]
        },
        'jsrender':{
            deps:[
                'jquery'
            ]
        },
        'lightbox':{
            deps:[
                'jquery'
            ]
        },
        'WdatePicker':{
            deps:[
                'jquery',
                'common'
            ]
        },
        'spinner':{
            deps:[
                'jquery'
            ]
        }
    },
    packages: [

    ],
    paths: {
        'bootstrap-sass': '../bower_components/bootstrap-sass/assets/javascripts/bootstrap.min',
        'jquery': '../bower_components/jquery/dist/jquery',
        'requirejs': '../bower_components/requirejs/require',
        'common': './common/common',
        'respond': '../bower_components/respond/dest/respond.src',
        'select2': '../bower_components/select2/dist/js/select2',
        'ueconfig': './plugins/ueditor/ueditor.config',
        'ueall': './plugins/ueditor/ueditor.all',
        'datePicker': './plugins/jQuery.fn.datePicker',
        'jquery-form':'../bower_components/jquery-form/jquery.form',
        'jquery-validation': '../bower_components/jquery-validation/dist/jquery.validate',
        'lazyload' : '../bower_components/jquery_lazyload/jquery.lazyload',
        'superslide' : '../bower_components/superslide/jquery.SuperSlide.2.1.1',
        'animateNum' : '../bower_components/jquery-animateNumber/jquery.animateNumber',
        'select2-cn': '../bower_components/select2/dist/js/i18n/zh-CN',
        'lightbox':"./plugins/lightbox2/js/lightbox",
        'jsrender' : '../bower_components/jsrender/jsrender',
        'WdatePicker':'/scripts/plugins/My97DatePicker/WdatePicker',
        'spinner':'../bower_components/jquery.spinner/dist/js/jquery.spinner.min'
    }
});

require(['jquery','common','respond','WdatePicker'],function($) {
    // 公共的只调用一次的方法
});