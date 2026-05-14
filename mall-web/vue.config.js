const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 9192, // 端口
    proxy: {
      // 只代理 API 请求到后端
      '/api': {
        target: 'http://localhost:9191',
        changeOrigin: true,
        ws: false,
      },
      // 代理后端接口请求
      '/file': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/avatar': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/userinfo': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/role': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/login': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/register': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/userid': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/address': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/user': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/icon': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/good': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/category': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/order': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/message': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/afterSale': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
      '/income': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
    },
    // 配置 History 模式的 fallback
    historyApiFallback: {
      disableDotRule: true,
      rewrites: [
        { from: /^\/manage/, to: '/index.html' },
        { from: /^\//, to: '/index.html' }
      ]
    }
  },
  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.template.html',
      filename: 'index.html',
      title: 'Cosplay Mall',
    },
  },
})
