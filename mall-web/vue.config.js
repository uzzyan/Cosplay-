const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 9192, // 端口
    proxy: {
      '/': {
        target: 'http://localhost:9191',
        changeOrigin: true,
        ws: false,
        headers: {
          'Accept-Charset': 'utf-8'
        }
      }
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
