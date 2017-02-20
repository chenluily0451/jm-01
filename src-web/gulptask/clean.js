/*
*   Created by Edward 10/8/2016
* */

var gulp = require('gulp');
var del  = require('del');

var sourcePath = {
    'styles'     : 'app/styles',
    'spriteImg'  : 'app/images/sprite/auto-sprite.png',
    'spriteScss' : 'app/scss/components/_auto-sprite.scss'
};

var distPath = {
    'dist'       : 'dist'
}

gulp.task('clean-dev',function(){
    del.sync([sourcePath.styles,sourcePath.spriteImg,sourcePath.spriteScss]);
});

gulp.task('clean-build',function(){
    del.sync([distPath.dist]);
});