var gulp = require('gulp');
var requireDir = require('require-dir');

requireDir('./gulptask',{ recurse: true });

gulp.task('watch',['watchSass']);

gulp.task('dev',['clean-dev','sass','watch']);

gulp.task('build',['clean-build','sass-release','js-release']);

gulp.task('default',['dev']);