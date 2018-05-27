var path = require("path");
var HtmlWebpackPlugin = require('html-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    mode: "development",
    entry: {
        "dependencies": ["./client-fastopt-entrypoint.js"],
        "client-fastopt": ["./hot-launcher.js"]
    },
    output: {
        path: __dirname,
        filename: "[name]-library.js",
        library: "appLibrary",
        libraryTarget: "var"
    },
    devtool: "source-map",
    devServer: {
        proxy: [
            {
                context: ["/auth", "/api"],
                target: "http://localhost:9000"
            }
        ],
        historyApiFallback: true
    },
    resolve: {
        alias: {
            "resources": path.resolve(__dirname, "../../../../src/main/resources")
        }
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [ 'style-loader', 'css-loader' ]
            },
            // "file" loader for svg
            {
                test: /\.svg$/,
                use: [
                    {
                        loader: 'file-loader',
                        query: {
                            name: 'static/media/[name].[hash:8].[ext]'
                        }
                    }
                ]
            }
        ],
        noParse: (content) => {
        return content.endsWith("-fastopt.js");
}
},
plugins: [
    new CopyWebpackPlugin([
        { from: path.resolve(__dirname, "../../../../public") }
    ]),
    new HtmlWebpackPlugin({
        template: path.resolve(__dirname, "../../../../public/index-fastopt.html"),
        inject: false
    })
]
}
