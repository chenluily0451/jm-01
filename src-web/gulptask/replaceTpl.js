var gulp = require('gulp');

var revCollector = require('gulp-rev-collector');

var sourcePath = {
    'layout'   : '../src/main/resources/templates/layout/**/*',
    'partials' : '../src/main/resources/templates/partials/**/*',
    'index'    : '../src/main/resources/templates/test.html',
    'manifest' : 'dist/rev/*.json'
};

var distPath = {
    'html'     : '../src/main/resourceTest/templates'
};

gulp.task('replaceTpl',function(){
    setTimeout(function(){
        gulp.src([sourcePath.manifest,sourcePath.index])
            .pipe( revCollector() )
            .pipe(gulp.dest(distPath.html));
    },3000);
});