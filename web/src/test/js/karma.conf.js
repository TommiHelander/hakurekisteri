module.exports = function(config) {
    config.set({
        basePath: '../../',
        preprocessors: {
            "main/webapp/coffee/*.coffee": ["coffee"],
            "test/coffee/*.coffee": ["coffee"]
        },
        coffeePreprocessor: {
            // options passed to the coffee compiler
            options: {
                bare: true,
                sourceMap: false
            },
            // transforming the filenames
            transformPath: function(path) {
                return path.replace(/\.coffee$/, '.js');
            }
        },
        frameworks: ['jasmine'],
        plugins: [
            // these plugins will be require() by Karma
            'karma-coffee-preprocessor',
            'karma-jasmine',
            'karma-junit-reporter',
            'karma-requirejs',
            'karma-phantomjs-launcher'
        ],
        files: [
            'test/js/default.js',
            'main/webapp/static/js/jquery-2.1.3.min.js',
            'main/webapp/static/js/jquery.fileDownload.js',
            'main/webapp/static/js/bootstrap-3.3.1.min.js',
            'main/webapp/static/js/Chart.min.js',
            'main/webapp/static/js/angular/angular.min.js',
            'main/webapp/static/js/angular/angular-resource.min.js',
            'main/webapp/static/js/angular/angular-route.min.js',
            'main/webapp/static/js/angular/angular-sanitize.min.js',
            'main/webapp/static/js/angular/angular-cookies.min.js',
            'main/webapp/static/js/angular-locale/angular-locale_fi.js',
            'main/webapp/static/js/angular-ui/ui-bootstrap-tpls-0.12.0.min.js',
            'main/webapp/static/js/ng-upload.js',

            'main/webapp/coffee/app.coffee',
            'main/webapp/coffee/oph-msg-directive.coffee',
            'main/webapp/coffee/common-functions.coffee',
            'main/webapp/coffee/controller-*.coffee',
            'main/webapp/coffee/configure.coffee',

            'test/js/angular-mocks.js',
            'test/coffee/**/*.coffee'
        ],
        exclude: [
        ],
        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['progress','junit'],
        junitReporter: {
            outputFile: '../target/surefire-reports/coffee-test-results.xml'
        },
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,
        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],
        captureTimeout: 60000,
        singleRun: true
    });
};