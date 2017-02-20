/*
*   Created by Edward 11/8/2016
* */

var gulp   = require('gulp');
var eslint = require('gulp-eslint');
var uglify = require('gulp-uglify');
var rev    = require('gulp-rev');

var sourcePath = {
    'scripts'   : 'app/scripts/**/*.js',
    'plugins'   : 'app/scripts/plugins/**/*',
    'components': 'bower_components/**/*'
};

var distPath = {
    'scripts'   : 'dist/scripts/',
    'plugins'   : 'dist/scripts/plugins',
    'components': 'dist/bower_components',
    'manifest'  : 'dist/rev/'
};

gulp.task('eslint',function(){
   return gulp.src('app/scripts/particals/**/*.js')
       .pipe(eslint({
           "env": {
               "browser": true
           },
           "extends": "eslint:recommended",
           "rules": {
               "no-console":0,
               "indent"          : ["error", 4],
               "linebreak-style" : ["error", "unix"],
               "quotes"          : ["error", "single"],
               "semi"            : ["error", "always", { "omitLastInOneLineBlock": true}],
               "no-extra-semi"   : ["error"],
               "comma-dangle"    : ["error", "never"]
           },
           "parserOptions": {
               "ecmaVersion": 6,
               "sourceType": "module"
            }
       }))
       .pipe(eslint.format())
       .pipe(eslint.failOnError());
});

gulp.task('components',function(){
    gulp.src(sourcePath.components)
        .pipe(gulp.dest(distPath.components));
});

gulp.task('plugins',function(){
    gulp.src(sourcePath.plugins)
        .pipe(gulp.dest(distPath.plugins));
});


gulp.task('js-release',['components','plugins'],function(){
    gulp.src(sourcePath.scripts)
        .pipe(uglify())
        //.pipe(rev())
        .pipe(gulp.dest(distPath.scripts));
        //.pipe(rev.manifest('rev-manifest-js.json'))
        //.pipe(gulp.dest(distPath.manifest) );
});

