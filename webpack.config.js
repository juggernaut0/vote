const path = require('path');

module.exports = {
    entry: './ui/build/kotlin-js-min/main/ui.js',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'ui/build/web/js')
    },
    mode: 'development'
};
