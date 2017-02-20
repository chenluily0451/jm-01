/*
*   Created by Edward 10/8/2016
* */

var gulp         = require('gulp');
var spritesmith  = require('gulp.spritesmith');
var sass         = require('gulp-sass');
var autoprefixer = require('gulp-autoprefixer');
var rev          = require('gulp-rev');
var runSequence  = require('run-sequence');
var filter       = require('gulp-filter');

var sourcePath = {
    'images'         : 'app/images/**/*',
    'imagesSprites'  : 'app/images/sprite/**/*.png',
    'scss'           : 'app/scss/**/*.scss',
    'styles'         : 'app/styles/',
    'spriteImgName'  : 'app/images/sprite/auto-sprite.png',
    'spriteImgPath'  : '/images/sprite/auto-sprite.png',
    'spriteScssName' : 'app/scss/helpers/_auto-sprite.scss'
};

var distPath = {
    'styles'         : 'dist/styles',
    'images'         : 'dist/images',
    'imagesSprite'   : 'dist/images/sprite',
    'manifest'       : 'dist/rev/'
};

gulp.task('images',function() {
    var f = filter(['app/images/**/*','!app/images/sprite/**']);
    gulp.src(sourcePath.images)
        .pipe(f)
        .pipe(gulp.dest(distPath.images));
    gulp.src(sourcePath.spriteImgName)
        .pipe(gulp.dest(distPath.imagesSprite));
});

gulp.task('sprite',function(){
    var spriteData = gulp.src(sourcePath.imagesSprites)
        .pipe(spritesmith({
            imgName   : sourcePath.spriteImgName,
            imgPath   : sourcePath.spriteImgPath,
            cssName   : sourcePath.spriteScssName,
            algorithm : 'alt-diagonal',
            cssFormat : 'scss',
            padding   : 10
        }));
    return spriteData.pipe(gulp.dest(''));
});

function sassFn(){
    gulp.src(sourcePath.scss)
        .pipe(sass({
            precision       : 10,
            outputStyle     : 'compact',
            errLogToConsole : true
        }).on('error', sass.logError))
        .pipe(autoprefixer({
            browsers: ['> 1%', 'Last 2 versions', 'IE 8'],
            cascade: false
        }))
        .pipe(gulp.dest(sourcePath.styles));
}

gulp.task('sass',['sprite'],function(){
    sassFn();
});

gulp.task('watchSassStyle',function(){
    sassFn();
});

gulp.task('sass-release',['images'],function(){
    gulp.src(sourcePath.scss)
        .pipe(sass({
            precision       : 10,
            outputStyle     : 'compressed',
            errLogToConsole : true
        }).on('error', sass.logError))
        .pipe(autoprefixer({
            browsers: ['> 1%', 'Last 2 versions', 'IE 8'],
            cascade: false
        }))
        //.pipe(rev())
        .pipe(gulp.dest(distPath.styles));
        //.pipe(rev.manifest('rev-manifest-css.json'))
        //.pipe(gulp.dest(distPath.manifest) );
});

gulp.task('watchSass',function(){
    gulp.watch(sourcePath.scss,['watchSassStyle']);
});
