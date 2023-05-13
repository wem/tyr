// React browser router
config.devServer.historyApiFallback = true

config.devServer.proxy = {
    '/creator': {
        target: 'http://localhost:8887',
        secure: false,
    },
}